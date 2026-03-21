package course.QAssistant.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import course.QAssistant.LangChain4j.assistant.Assistant;
import course.QAssistant.exception.QAWebException;
import course.QAssistant.handler.RedisComponent;
import course.QAssistant.mapper.MiniofileMapper;
import course.QAssistant.mapper.UserAiModelMapper;
import course.QAssistant.mapper.UserAiPreferenceMapper;
import course.QAssistant.minio.service.MinIOFileService;
import course.QAssistant.pojo.dto.CreateSessionRequest;
import course.QAssistant.pojo.dto.SessionSummaryResponse;
import course.QAssistant.pojo.dto.TokenUserDTO;
import course.QAssistant.pojo.po.ChatMessageDocument;
import course.QAssistant.pojo.po.ChatSession;
import course.QAssistant.pojo.po.Miniofile;
import course.QAssistant.pojo.po.UserAiModel;
import course.QAssistant.pojo.po.UserAiPreference;
import course.QAssistant.pojo.vo.request.ChatRequestVO;
import course.QAssistant.pojo.vo.request.CreateSessionRequestVO;
import course.QAssistant.pojo.vo.request.RagUploadVO;
import course.QAssistant.pojo.vo.response.ChatSessionRespVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.properties.AiProperties;
import course.QAssistant.repository.ChatSessionRepository;
import course.QAssistant.service.ChatService;
import course.QAssistant.service.ChatSessionService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.apache.poi.ApachePoiDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final int    MEMORY_WINDOW_SIZE = 20;
    private static final int    RAG_MAX_RESULTS    = 5;
    private static final double RAG_MIN_SCORE      = 0.6;
    private static final int    CHUNK_SIZE         = 500;
    private static final int    CHUNK_OVERLAP      = 50;

    private final QdrantClient                   qdrantClient;
    private final AiProperties                   aiProperties;
    private final MongoTemplate                  mongoTemplate;
    private final RedisComponent                 redisComponent;
    private final MinIOFileService               minioFileService;
    private final MiniofileMapper                miniofileMapper;
    private final UserAiModelMapper              userAiModelMapper;
    private final UserAiPreferenceMapper         userAiPreferenceMapper;
    private final ChatSessionService             chatSessionService;
    private final ChatSessionRepository          chatSessionRepository;
    private final EmbeddingModel                 embeddingModel;
    private final EmbeddingStore<TextSegment>    embeddingStore;
    private final StreamingChatLanguageModel     defaultStreamingChatLanguageModel;

    // ==================== 对外接口 ====================

    @Override
    public R<ChatSessionRespVO> createSession(CreateSessionRequestVO sessionCreateVO, String token, String loginType) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);

        CreateSessionRequest request = CreateSessionRequest.builder()
                .userUid(tokenUserDTO.getUid())
                .title(sessionCreateVO.getTitle())
                .build();

        if (ObjectUtil.isNotNull(sessionCreateVO.getAiModelId())) {
            UserAiModel userAiModel = userAiModelMapper.selectById(sessionCreateVO.getAiModelId());
            if (ObjectUtil.isNotNull(userAiModel)) {
                request.setAiModelId(sessionCreateVO.getAiModelId());
            } else {
                throw new QAWebException("该AI模型不存在");
            }
        }

        SessionSummaryResponse sessionResponse = chatSessionService.createSession(request);
        log.debug(sessionResponse.toString());

        ChatSessionRespVO respVO = new ChatSessionRespVO();
        respVO.setSessionId(sessionResponse.getId());
        respVO.setTitle(sessionResponse.getTitle());
        respVO.setAiModelId(sessionResponse.getAiModelId());
        respVO.setCreatedAt(sessionResponse.getCreatedAt());
        respVO.setUpdatedAt(sessionResponse.getUpdatedAt());
        return R.ok(respVO);
    }

    @Override
    public R<List<ChatSessionRespVO>> getUserSessions(String token, String loginType) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);

        List<SessionSummaryResponse> summaries = chatSessionService.getUserSessions(tokenUserDTO.getUid());
        List<ChatSessionRespVO> result = summaries.stream().map(s -> {
            ChatSessionRespVO vo = new ChatSessionRespVO();
            vo.setSessionId(s.getId());
            vo.setTitle(s.getTitle());
            vo.setAiModelId(s.getAiModelId());
            vo.setCreatedAt(s.getCreatedAt());
            vo.setUpdatedAt(s.getUpdatedAt());
            return vo;
        }).collect(Collectors.toList());

        return R.ok(result);
    }

    @Override
    public Flux<ServerSentEvent<String>> streamChat(ChatRequestVO chatRequestVO,
                                                    String authorization, String loginType) {
        Sinks.Many<ServerSentEvent<String>> sink =
                Sinks.many().unicast().onBackpressureBuffer();

        try {
            // ① 鉴权 & 会话归属校验
            TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(authorization, loginType);
            ChatSession session = chatSessionRepository
                    .findByIdAndUserUid(chatRequestVO.getSessionId(), tokenUserDTO.getUid())
                    .orElse(null);

            if (session == null) {
                sink.tryEmitNext(buildSseEvent("error", "会话不存在或无权访问"));
                sink.tryEmitComplete();
                return sink.asFlux();
            }

            // ② 重建历史记忆
            ChatMemory chatMemory = rebuildChatMemory(session);

            // ③ 构建模型
            StreamingChatLanguageModel chatModel = buildChatModel(session);

            // ④ 构建最终发送给模型的消息
            //    若开启 RAG，先检索相关文档片段，拼接到用户消息前作为上下文
            String finalMessage = buildFinalMessage(
                    chatRequestVO.getMessage(),
                    chatRequestVO.getRagEnabled(),
                    session.getId(),
                    sink
            );

            // ⑤ 构建 Assistant（不使用 LangChain4j RAG 管道）
            Assistant assistant = AiServices.builder(Assistant.class)
                    .streamingChatLanguageModel(chatModel)
                    .chatMemory(chatMemory)
                    .build();

            // ⑥ 持久化用户原始消息（存原始问题，不存拼接后的 prompt）
            pushMessage(session.getId(), "USER", chatRequestVO.getMessage());

            // ⑦ 流式推送
            StringBuilder aiResponseBuilder = new StringBuilder();
            assistant.chat(finalMessage)
                    .onNext(token -> {
                        aiResponseBuilder.append(token);
                        sink.tryEmitNext(buildSseEvent("message", token));
                    })
                    .onComplete(response -> {
                        String fullReply = aiResponseBuilder.toString();
                        pushMessage(session.getId(), "AI", fullReply);
                        log.debug("[Chat] session={} 回复完毕，共 {} 字符",
                                session.getId(), fullReply.length());
                        sink.tryEmitNext(buildSseEvent("done", "[DONE]"));
                        sink.tryEmitComplete();
                    })
                    .onError(error -> {
                        log.error("[Chat] session={} 流式对话异常", session.getId(), error);
                        sink.tryEmitNext(buildSseEvent("error", "AI 响应异常：" + error.getMessage()));
                        sink.tryEmitComplete();
                    })
                    .start();

        } catch (Exception e) {
            log.error("[Chat] streamChat 初始化异常", e);
            sink.tryEmitNext(buildSseEvent("error", "服务异常：" + e.getMessage()));
            sink.tryEmitComplete();
        }

        return sink.asFlux();
    }

    @Override
    public R ingestDocument(RagUploadVO ragUploadVO, String token, String loginType) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);

        try {
            ChatSession session = chatSessionRepository
                    .findByIdAndUserUid(ragUploadVO.getSessionId(), tokenUserDTO.getUid())
                    .orElseThrow(() -> new QAWebException("会话不存在或无权访问"));

            Miniofile minioFile = miniofileMapper.selectOne(
                    new LambdaQueryWrapper<Miniofile>()
                            .eq(Miniofile::getId, ragUploadVO.getFileId())
                            .eq(Miniofile::getUid, tokenUserDTO.getUid())
            );
            if (ObjectUtil.isNull(minioFile)) {
                throw new QAWebException("指定文件不存在或无权访问");
            }

            ingestToEmbeddingStore(minioFile, session.getId());
            log.info("[RAG] 文件 {} 向量化完成，session={}", minioFile.getFileName(), session.getId());

            return R.ok("文档上传并向量化成功");

        } catch (QAWebException e) {
            log.warn("[RAG] 业务校验失败：{}", e.getMessage());
            return R.error(e.getMessage());
        } catch (Exception e) {
            log.error("[RAG] 文档向量化入库失败", e);
            return R.error("文档向量化失败：" + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    private String buildFinalMessage(String userMessage,
                                     Boolean ragEnabled,
                                     String sessionId,
                                     Sinks.Many<ServerSentEvent<String>> sink) {
        if (!Boolean.TRUE.equals(ragEnabled)) {
            return userMessage;
        }

        try {
            // Step1：用户问题向量化
            Embedding queryEmbedding = embeddingModel.embed(userMessage).content();
            List<Float> queryVector = new ArrayList<>();
            for (float v : queryEmbedding.vector()) queryVector.add(v);

            // Step2：构建 sessionId 过滤条件
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

            // Step3：直接用 QdrantClient 搜索
            //   ✅ 关键：setWithVectors(true) 确保返回向量，避免 CosineSimilarity 崩溃
            Points.SearchPoints searchRequest = Points.SearchPoints.newBuilder()
                    .setCollectionName("QAssistant")   // 替换为你的 collection 名称
                    .addAllVector(queryVector)
                    .setLimit(RAG_MAX_RESULTS)
                    .setScoreThreshold((float) RAG_MIN_SCORE)
                    .setFilter(filter)
                    .setWithVectors(Points.WithVectorsSelector.newBuilder().setEnable(true).build())
                    .setWithPayload(Points.WithPayloadSelector.newBuilder().setEnable(true).build())
                    .build();

            List<Points.ScoredPoint> scoredPoints = qdrantClient.searchAsync(searchRequest).get();

            if (scoredPoints == null || scoredPoints.isEmpty()) {
                log.debug("[RAG] session={} 未检索到相关内容，使用原始消息", sessionId);
                sink.tryEmitNext(buildSseEvent("warn", "未检索到相关文档内容，已使用普通对话模式"));
                return userMessage;
            }

            // Step4：命中片段拼接为上下文 prompt
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append("请根据以下参考资料回答用户的问题。\n\n");
            contextBuilder.append("【参考资料】\n");
            int index = 1;
            for (Points.ScoredPoint point : scoredPoints) {
                JsonWithInt.Value textValue = point.getPayloadMap().get("text");
                if (textValue == null) continue;
                if (textValue.getKindCase() != JsonWithInt.Value.KindCase.STRING_VALUE) continue;
                String text = textValue.getStringValue();
                if (text == null || text.isBlank()) continue;
                contextBuilder.append(index++).append(". ").append(text).append("\n\n");
            }
            contextBuilder.append("【用户问题】\n").append(userMessage);

            String finalMessage = contextBuilder.toString();
            log.debug("[RAG] session={} 命中 {} 条，prompt 长度={}",
                    sessionId, scoredPoints.size(), finalMessage.length());
            return finalMessage;

        } catch (Exception e) {
            log.error("[RAG] session={} 检索异常，降级为普通对话: {}", sessionId, e.getMessage(), e);
            sink.tryEmitNext(buildSseEvent("warn", "RAG 检索失败，已切换为普通对话模式"));
            return userMessage;
        }
    }

    /**
     * 从 MongoDB 历史消息重建 ChatMemory
     */
    private ChatMemory rebuildChatMemory(ChatSession session) {
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(MEMORY_WINDOW_SIZE);

        List<ChatMessageDocument> messages = session.getMessages();
        if (messages == null || messages.isEmpty()) {
            return chatMemory;
        }

        for (ChatMessageDocument msg : messages) {
            if (msg.getContent() == null || msg.getContent().isBlank()) continue;
            switch (msg.getRole().toUpperCase()) {
                case "USER":
                    chatMemory.add(UserMessage.from(msg.getContent()));
                    break;
                case "AI":
                case "ASSISTANT":
                    chatMemory.add(AiMessage.from(msg.getContent()));
                    break;
                default:
                    log.warn("[Memory] 未知消息角色：{}，已跳过", msg.getRole());
            }
        }

        log.debug("[Memory] session={} 加载 {} 条历史消息", session.getId(), messages.size());
        return chatMemory;
    }

    /**
     * 文档解析 → 分块 → 打 sessionId 元数据标签 → 向量化入库
     */
    private void ingestToEmbeddingStore(Miniofile minioFile, String sessionId) {
        InputStream inputStream = minioFileService.downloadFile(
                minioFile.getBucket(), minioFile.getObjectName());

        DocumentParser parser = resolveParser(minioFile.getFileName());
        Document document = parser.parse(inputStream);

        Metadata metadata = document.metadata();
        metadata.put("sessionId", sessionId);
        metadata.put("fileId", String.valueOf(minioFile.getId()));
        metadata.put("fileName", minioFile.getFileName() != null ? minioFile.getFileName() : "unknown");

        DocumentSplitter splitter = DocumentSplitters.recursive(CHUNK_SIZE, CHUNK_OVERLAP);

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(document);
        log.debug("[RAG] 文件 {} 分块向量化完毕，session={}", minioFile.getFileName(), sessionId);
    }

    /**
     * 根据扩展名选择文档解析器
     */
    private DocumentParser resolveParser(String fileName) {
        if (fileName == null) return new TextDocumentParser();
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf"))                              return new ApachePdfBoxDocumentParser();
        if (lower.endsWith(".doc") || lower.endsWith(".docx"))  return new ApachePoiDocumentParser();
        return new TextDocumentParser();
    }

    /**
     * 构建 StreamingChatLanguageModel（自定义模型 > 系统默认）
     */
    private StreamingChatLanguageModel buildChatModel(ChatSession session) {
        UserAiModel customModel = null;
        UserAiPreference preference = null;

        if (session.getAiModelId() != null) {
            customModel = userAiModelMapper.selectById(session.getAiModelId());
            preference = userAiPreferenceMapper.selectOne(
                    new LambdaQueryWrapper<UserAiPreference>()
                            .eq(UserAiPreference::getUserUid, session.getUserUid())
                            .eq(UserAiPreference::getAiModelId, session.getAiModelId())
            );
        }

        String baseUrl    = ObjectUtil.isNotNull(customModel) ? customModel.getBaseUrl()   : aiProperties.getBaseUrl();
        String apiKey     = ObjectUtil.isNotNull(customModel) ? customModel.getApiKey()    : aiProperties.getApiKey();
        String modelName  = ObjectUtil.isNotNull(customModel) ? customModel.getModelName() : aiProperties.getModelName();
        Double temperature = ObjectUtil.isNotNull(preference)
                ? Convert.toDouble(preference.getTemperature()) : aiProperties.getTemperature();
        Double topP = ObjectUtil.isNotNull(preference)
                ? Convert.toDouble(preference.getTopP()) : aiProperties.getTopP();

        return OpenAiStreamingChatModel.builder()
                .timeout(aiProperties.getTimeout())
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .topP(topP)
                .build();
    }

    /**
     * 持久化一条消息到 MongoDB
     */
    private void pushMessage(String sessionId, String role, String content) {
        ChatMessageDocument message = ChatMessageDocument.builder()
                .id(new ObjectId().toHexString())
                .role(role)
                .content(content)
                .createdAt(new Date())
                .build();

        mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(sessionId)),
                new Update()
                        .push("messages", message)
                        .set("updated_at", new Date()),
                ChatSession.class
        );
    }

    /**
     * 构建 SSE 事件
     */
    private ServerSentEvent<String> buildSseEvent(String event, String data) {
        return ServerSentEvent.<String>builder()
                .event(event)
                .data(data)
                .build();
    }
}