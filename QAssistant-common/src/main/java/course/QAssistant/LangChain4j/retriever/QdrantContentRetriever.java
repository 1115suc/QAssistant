package course.QAssistant.LangChain4j.retriever;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 自定义 Qdrant ContentRetriever。
 *
 * <p>修复 LangChain4j 旧版本 QdrantEmbeddingStore.search() 未设置
 * with_vectors=true，导致返回向量数组为空、CosineSimilarity 抛出
 * "Length of vector a (0) != b (1024)" 的 Bug。
 *
 * <p>直接通过 QdrantClient 原生 gRPC API 搜索，显式携带向量数据。
 */
@Slf4j
public class QdrantContentRetriever implements ContentRetriever {

    private final QdrantClient qdrantClient;
    private final EmbeddingModel embeddingModel;
    private final String collectionName;
    private final String sessionId;
    private final int maxResults;
    private final float minScore;

    // 使用普通构造器，避免 Lombok @Builder + final 字段的兼容性问题
    private QdrantContentRetriever(QdrantClient qdrantClient,
                                   EmbeddingModel embeddingModel,
                                   String collectionName,
                                   String sessionId,
                                   int maxResults,
                                   float minScore) {
        this.qdrantClient   = qdrantClient;
        this.embeddingModel = embeddingModel;
        this.collectionName = collectionName;
        this.sessionId      = sessionId;
        this.maxResults     = maxResults;
        this.minScore       = minScore;
    }

    // ==================== Builder ====================

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private QdrantClient  qdrantClient;
        private EmbeddingModel embeddingModel;
        private String         collectionName;
        private String         sessionId;
        private int            maxResults = 5;
        private float          minScore   = 0.6f;

        public Builder qdrantClient(QdrantClient qdrantClient) {
            this.qdrantClient = qdrantClient;
            return this;
        }

        public Builder embeddingModel(EmbeddingModel embeddingModel) {
            this.embeddingModel = embeddingModel;
            return this;
        }

        public Builder collectionName(String collectionName) {
            this.collectionName = collectionName;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder maxResults(int maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        public Builder minScore(float minScore) {
            this.minScore = minScore;
            return this;
        }

        public QdrantContentRetriever build() {
            if (qdrantClient == null)   throw new IllegalStateException("qdrantClient 不能为 null");
            if (embeddingModel == null) throw new IllegalStateException("embeddingModel 不能为 null");
            if (collectionName == null) throw new IllegalStateException("collectionName 不能为 null");
            if (sessionId == null)      throw new IllegalStateException("sessionId 不能为 null");
            return new QdrantContentRetriever(
                    qdrantClient, embeddingModel, collectionName, sessionId, maxResults, minScore);
        }
    }

    // ==================== 核心检索逻辑 ====================

    @Override
    public List<Content> retrieve(Query query) {
        log.debug("[RAG] 开始检索 session={}, query={}", sessionId,
                query.text().length() > 50 ? query.text().substring(0, 50) + "…" : query.text());
        try {
            // ① 查询文本向量化
            Embedding queryEmbedding = embeddingModel.embed(query.text()).content();
            List<Float> queryVector  = toFloatList(queryEmbedding.vector());
            log.debug("[RAG] 查询向量维度={}", queryVector.size());

            // ② 构建 sessionId 过滤条件（只检索本会话上传的文档）
            Points.Filter filter = Points.Filter.newBuilder()
                    .addMust(
                            Points.Condition.newBuilder()
                                    .setField(
                                            Points.FieldCondition.newBuilder()
                                                    .setKey("sessionId")
                                                    .setMatch(Points.Match.newBuilder()
                                                            .setKeyword(sessionId)
                                                            .build())
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            // ③ 搜索请求
            //    ✅ 核心修复：setWithVectors(enable=true) 确保响应携带向量数据
            //    LangChain4j 旧版 QdrantEmbeddingStore 缺少此设置，导致向量为空数组崩溃
            Points.SearchPoints searchRequest = Points.SearchPoints.newBuilder()
                    .setCollectionName(collectionName)
                    .addAllVector(queryVector)
                    .setLimit(maxResults)
                    .setScoreThreshold(minScore)
                    .setFilter(filter)
                    .setWithVectors(
                            Points.WithVectorsSelector.newBuilder().setEnable(true).build()
                    )
                    .setWithPayload(
                            Points.WithPayloadSelector.newBuilder().setEnable(true).build()
                    )
                    .build();

            // ④ 执行搜索（同步等待）
            List<Points.ScoredPoint> scoredPoints = qdrantClient.searchAsync(searchRequest).get();
            log.debug("[RAG] Qdrant 返回 {} 条结果", scoredPoints == null ? 0 : scoredPoints.size());

            if (scoredPoints == null || scoredPoints.isEmpty()) {
                log.debug("[RAG] session={} 无命中内容", sessionId);
                return Collections.emptyList();
            }

            // ⑤ ScoredPoint → LangChain4j Content
            List<Content> contents = new ArrayList<>();
            for (Points.ScoredPoint point : scoredPoints) {
                String text = extractText(point);
                if (text == null || text.isBlank()) {
                    log.warn("[RAG] 某条结果 text 字段为空，已跳过。payload keys={}",
                            point.getPayloadMap().keySet());
                    continue;
                }
                Metadata metadata = extractMetadata(point);
                contents.add(Content.from(TextSegment.from(text, metadata)));
                log.debug("[RAG] 命中 score={} | {}",
                        point.getScore(),
                        text.length() > 60 ? text.substring(0, 60) + "…" : text);
            }

            log.debug("[RAG] session={} 最终命中 {} 条有效内容", sessionId, contents.size());
            return contents;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[RAG] session={} 检索被中断", sessionId, e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("[RAG] session={} 检索异常: {}", sessionId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // ==================== 私有工具方法 ====================

    /**
     * 从 payload 提取文本内容。
     * LangChain4j EmbeddingStoreIngestor 入库时固定以 "text" 为 key 存储分块文本。
     * payload value 类型为 {@link JsonWithInt.Value}。
     */
    private String extractText(Points.ScoredPoint point) {
        Map<String, JsonWithInt.Value> payloadMap = point.getPayloadMap();
        if (payloadMap == null || payloadMap.isEmpty()) return null;

        JsonWithInt.Value textValue = payloadMap.get("text");
        if (textValue == null) return null;

        if (textValue.getKindCase() == JsonWithInt.Value.KindCase.STRING_VALUE) {
            return textValue.getStringValue();
        }
        return null;
    }

    /**
     * 从 payload 提取元数据（排除 "text" 字段本身）
     */
    private Metadata extractMetadata(Points.ScoredPoint point) {
        Metadata metadata = new Metadata();
        Map<String, JsonWithInt.Value> payloadMap = point.getPayloadMap();
        if (payloadMap == null) return metadata;

        for (Map.Entry<String, JsonWithInt.Value> entry : payloadMap.entrySet()) {
            if ("text".equals(entry.getKey())) continue;
            JsonWithInt.Value value = entry.getValue();
            if (value.getKindCase() == JsonWithInt.Value.KindCase.STRING_VALUE) {
                metadata.put(entry.getKey(), value.getStringValue());
            }
        }
        return metadata;
    }

    /**
     * float[] → List&lt;Float&gt;（Qdrant gRPC API 参数类型要求）
     */
    private List<Float> toFloatList(float[] arr) {
        List<Float> list = new ArrayList<>(arr.length);
        for (float v : arr) list.add(v);
        return list;
    }
}