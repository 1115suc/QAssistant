import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import course.QAssistant.LangChain4j.repository.MongoDBChatMemoryStore;
import course.QAssistant.LangChain4j.service.ConsultantService;
import course.QAssistant.LangChain4j.service.RagAssistant;
import course.QAssistant.QAssistantBackendApplication;
import course.QAssistant.exception.QAWebException;
import course.QAssistant.mapper.MiniofileMapper;
import course.QAssistant.minio.service.MinIOFileService;
import course.QAssistant.pojo.po.ChatSession;
import course.QAssistant.pojo.po.Miniofile;
import course.QAssistant.repository.ChatSessionRepository;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.loader.UrlDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.apache.poi.ApachePoiDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Slf4j
@SpringBootTest(classes = QAssistantBackendApplication.class)
public class AIChatTest {
//
//    @Resource
//    private ConsultantService consultantService;
//    @Resource
//    private ChatSessionRepository chatSessionRepository;
//    @Resource
//    private MongoDBChatMemoryStore store;
//    @Resource
//    private MiniofileMapper miniofileMapper;
//    @Resource
//    private MinIOFileService minioFileService;
//
//    private static final String SESSION_ID = "69ce1ebea1eaca05bd0d13df";
//
//    @Resource
//    private EmbeddingStore<TextSegment> chromaEmbeddingStore;
//
//    @Resource
//    private EmbeddingModel embeddingModel;
//
//    @Resource
//    private OpenAiChatModel openAiChatModel;
//
//    @Resource
//    private ChatMemoryProvider chatMemoryProvider;
//
//    private static final int    MEMORY_WINDOW_SIZE = 20;
//    private static final int    RAG_MAX_RESULTS    = 5;
//    private static final double RAG_MIN_SCORE      = 0.6;
//    private static final int    CHUNK_SIZE         = 500;
//    private static final int    CHUNK_OVERLAP      = 100;
//
//    // ==================== 测试方法 ====================
//    @Test
//    public void testChat() {
//        String message = "你是谁";
//        Flux<String> chat = consultantService.chat(message, SESSION_ID);
//
//        // 方式 1: 阻塞获取完整结果 (适合测试)
//        String result = chat.collectList().block().stream().reduce("", (a, b) -> a + b);
//        System.out.println(result);
//
//        // 方式 2: 逐块输出流式响应
//        // chat.doOnNext(System.out::println).blockLast();
//
//        // 方式 3: 简单阻塞获取所有响应
//        // chat.blockLast();
//    }
//
//
//    @Test
//    public void getMessages() {
//        Optional<ChatSession> byId =
//                chatSessionRepository.findByIdWithMessagesSortedDesc(SESSION_ID);
//        JSON parse = JSONUtil.parse(byId.get().getMessages());
//        System.out.println(parse);
//    }
//
//    /**
//     * 文档向量化写入 Chroma
//     */
//    @Test
//    public void ingestDocument() {
//        String fileId    = "2039610612220432384";
//        String sessionId = "69ce1ebca1eaca05bd0d13de";
//        String uid       = "U667192869";
//
//        ChatSession session = chatSessionRepository
//                .findByIdAndUserUid(sessionId, uid)
//                .orElseThrow(() -> new QAWebException("会话不存在"));
//
//        Miniofile minioFile = miniofileMapper.selectOne(
//                new LambdaQueryWrapper<Miniofile>()
//                        .eq(Miniofile::getId, fileId)
//                        .eq(Miniofile::getUid, uid)
//        );
//        if (ObjectUtil.isNull(minioFile)) {
//            throw new QAWebException("指定文件不存在或无权访问");
//        }
//
//        ingestToEmbeddingStore(minioFile, session.getId());
//        log.info("[RAG] 文件 {} 向量化完成，session={}", minioFile.getFileName(), session.getId());
//    }
//
//    /**
//     * 核心：向量化并写入 Chroma
//     * 与之前逻辑完全相同，只是 embeddingStore 底层实现换成了 Chroma
//     */
//    private void ingestToEmbeddingStore(Miniofile minioFile, String sessionId) {
//
//        // -------- 1. 加载文档 --------
//        String previewUrl = minioFileService.getPreviewUrl(
//                minioFile.getBucket(), minioFile.getObjectName());
//
//        DocumentParser parser = resolveParser(minioFile.getFileName());
//        Document document = UrlDocumentLoader.load(previewUrl, parser);
//
//        if (document.text() == null || document.text().isBlank()) {
//            log.warn("[RAG] 文件 {} 内容为空，跳过向量化", minioFile.getFileName());
//            return;
//        }
//
//        // -------- 2. 分段 --------
//        DocumentSplitter splitter = DocumentSplitters.recursive(CHUNK_SIZE, CHUNK_OVERLAP);
//        List<TextSegment> segments = splitter.split(document);
//
//        if (segments.isEmpty()) {
//            log.warn("[RAG] 文件 {} 分段结果为空，跳过", minioFile.getFileName());
//            return;
//        }
//
//        // -------- 3. 构建带 metadata 的分段 + 生成 embedding --------
//        List<TextSegment> finalSegments = new ArrayList<>(segments.size());
//        List<Embedding>   embeddings    = new ArrayList<>(segments.size());
//
//        for (int i = 0; i < segments.size(); i++) {
//            TextSegment segment = segments.get(i);
//
//            if (segment.text() == null || segment.text().isBlank()) {
//                log.debug("[RAG] 第 {} 段为空，跳过", i);
//                continue;
//            }
//
//            // ⬇️ Chroma metadata：值只支持 String / int / float / boolean
//            //    不要放 null，否则 Chroma 会报错
//            Metadata metadata = new Metadata();
//            metadata.put("sessionId", sessionId);
//            metadata.put("index", String.valueOf(i));  // Chroma 支持 int 类型
//
//            TextSegment newSegment = TextSegment.from(segment.text(), metadata);
//            Embedding embedding = embeddingModel.embed(newSegment).content();
//
//            if (i == 0) {
//                log.info("[RAG] 模型输出向量维度: {}", embedding.vector().length);
//            }
//            if (embedding.vector().length == 0) {
//                log.error("[RAG] 第 {} 段生成的向量维度为 0，跳过！", i);
//                continue;
//            }
//
//            finalSegments.add(newSegment);
//            embeddings.add(embedding);
//        }
//
//        if (finalSegments.isEmpty()) {
//            log.warn("[RAG] 文件 {} 没有有效分段可存储", minioFile.getFileName());
//            return;
//        }
//
//        // -------- 4. 批量存入 Chroma（API 接口与 Qdrant 一致） --------
//        chromaEmbeddingStore.addAll(embeddings, finalSegments);
//
//        log.info("[RAG] ✅ 文件 {} 向量化完成, session={}, 有效chunks={}",
//                minioFile.getFileName(), sessionId, finalSegments.size());
//    }
//
//    /**
//     * 手动检索测试（调试用）
//     */
//    @Test
//    public void testRetrieveOnly_debug() {
//        String sessionId = "69ce1ebca1eaca05bd0d13de";
//        String queryText = "检索我的文档它里面说了什么";
//
//        Embedding queryEmbedding = embeddingModel.embed(queryText).content();
//
//        System.out.println("embeddingModel 类型: " + embeddingModel.getClass().getName());
//        System.out.println("查询向量维度: " + queryEmbedding.vector().length);
//
//        if (queryEmbedding.vector().length == 0) {
//            System.err.println("❌ embeddingModel 返回了空向量！");
//            return;
//        }
//
//        // 直接搜索（Chroma 的 search API 与 Qdrant 接口一致）
//        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
//                .queryEmbedding(queryEmbedding)
//                .maxResults(5)
//                .minScore(0.0)
//                // ⬇️ Chroma 的 metadata filter 语法（可选）
//                .filter(metadataKey("sessionId").isEqualTo(sessionId))
//                .build();
//
//        List<EmbeddingMatch<TextSegment>> matches =
//                chromaEmbeddingStore.search(searchRequest).matches();
//
//        System.out.println("======== 检索结果命中数量: " + matches.size() + " ========");
//
//        for (EmbeddingMatch<TextSegment> match : matches) {
//            TextSegment seg = match.embedded();
//            System.out.println("---- hit ----");
//            System.out.println("score    = " + match.score());
//            System.out.println("sessionId= " + (seg.metadata() != null
//                    ? seg.metadata().getString("sessionId") : null));
//            System.out.println("fileName = " + (seg.metadata() != null
//                    ? seg.metadata().getString("fileName") : null));
//            System.out.println("text     = " + seg.text());
//            System.out.println();
//        }
//    }
//
//    @Test
//    public void testEmbeddingModel() {
//        Embedding embedding = embeddingModel.embed("测试文本").content();
//        System.out.println("embeddingModel 类型: " + embeddingModel.getClass().getName());
//        System.out.println("向量维度: " + embedding.vector().length);
//    }
//
//    // ==================== 文件解析器（不变） ====================
//
//    private static final DocumentParser TEXT_PARSER   = new TextDocumentParser();
//    private static final DocumentParser PDF_PARSER    = new ApachePdfBoxDocumentParser();
//    private static final DocumentParser OFFICE_PARSER = new ApachePoiDocumentParser();
//    private static final Set<String> OFFICE_EXTENSIONS = Set.of(
//            "doc", "docx", "ppt", "pptx", "xls", "xlsx"
//    );
//
//    private DocumentParser resolveParser(String fileName) {
//        String ext = getExtension(fileName);
//        if (ext == null) throw new QAWebException("不支持的文件格式");
//        if ("pdf".equals(ext)) return PDF_PARSER;
//        if (OFFICE_EXTENSIONS.contains(ext)) return OFFICE_PARSER;
//        return TEXT_PARSER;
//    }
//
//    private String getExtension(String fileName) {
//        if (fileName == null || fileName.isBlank()) return null;
//        int lastDot = fileName.lastIndexOf('.');
//        if (lastDot < 0 || lastDot == fileName.length() - 1) return null;
//        return fileName.substring(lastDot + 1).toLowerCase();
//    }
//
//    @Resource
//    private OpenAiChatModel chatModel;
//    @Resource
//    private ContentRetriever contentRetriever;
//
//    @Test
//    public void test003() {
//        String sessionId = "69ce1ebca1eaca05bd0d13de";
//        String queryText = "检索我的文档它里面说了什么";
//
//        Embedding queryEmbedding = embeddingModel.embed(queryText).content();
//
//        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
//                .queryEmbedding(queryEmbedding)
//                .maxResults(5)
//                .minScore(0.0)
//                .filter(metadataKey("sessionId").isEqualTo(sessionId))  // ✅ 升版本后生效
//                .build();
//
////        RagAssistant assistant = AiServices.builder(RagAssistant.class)
////                .chatLanguageModel(chatModel)
////                .contentRetriever(contentRetriever)      // 注入带 dynamicFilter 的 retriever
////                .chatMemoryProvider(memoryId ->           // 按 sessionId 隔离对话记忆
////                        MessageWindowChatMemory.withMaxMessages(10))
////                .build();
////
////        String answer = assistant.chat("69ce1ebca1eaca05bd0d13de", "检索我的文档它里面说了什么");
////        System.out.println(answer);
//
//        List<EmbeddingMatch<TextSegment>> matches =
//                chromaEmbeddingStore.search(searchRequest).matches();
//
//        System.out.println("命中数: " + matches.size());
//        matches.forEach(match -> {
//            System.out.println("score=" + match.score());
//            System.out.println("sessionId=" + match.embedded().metadata().getString("sessionId"));
//            System.out.println("text=" + match.embedded().text());
//        });
//    }
//
//    @Resource
//    private EmbeddingStore<TextSegment> embeddingStore;
//
//    @Test
//    public void test004() {
//        TextSegment segment1 = TextSegment.from("I like football.", Metadata.metadata("userId", "1"));
//        Embedding embedding1 = embeddingModel.embed(segment1).content();
//        embeddingStore.add(embedding1, segment1);
//        TextSegment segment2 = TextSegment.from("I like basketball.", Metadata.metadata("userId", "2"));
//        Embedding embedding2 = embeddingModel.embed(segment2).content();
//        embeddingStore.add(embedding2, segment2);
//
//        Embedding queryEmbedding = embeddingModel.embed("What is your favourite sport?").content();
//
//        // search for user 1
//
//        Filter onlyForUser1 = metadataKey("userId").isEqualTo("1");
//
//        EmbeddingSearchRequest embeddingSearchRequest1 = EmbeddingSearchRequest
//                .builder()
//                .queryEmbedding(queryEmbedding)
//                .filter(onlyForUser1)
//                .build();
//
//        EmbeddingSearchResult<TextSegment> embeddingSearchResult1 = embeddingStore.search(embeddingSearchRequest1);
//        EmbeddingMatch<TextSegment> embeddingMatch1 = embeddingSearchResult1.matches().get(0);
//
//        System.out.println(embeddingMatch1.score());
//        System.out.println(embeddingMatch1.embedded().text());
//
//    }
//
//    @Test
//    public void test005() {
//        String fileId    = "2039610612220432384";
//        String sessionId = "69ce1ebca1eaca05bd0d13de";
//        String uid       = "U667192869";
//
//        ChatSession session = chatSessionRepository
//                .findByIdAndUserUid(sessionId, uid)
//                .orElseThrow(() -> new QAWebException("会话不存在"));
//
//        Miniofile minioFile = miniofileMapper.selectOne(
//                new LambdaQueryWrapper<Miniofile>()
//                        .eq(Miniofile::getId, fileId)
//                        .eq(Miniofile::getUid, uid)
//        );
//        if (ObjectUtil.isNull(minioFile)) {
//            throw new QAWebException("指定文件不存在或无权访问");
//        }
//
//        ingestToEmbeddingStore(minioFile, session.getId());
//        log.info("[RAG] 文件 {} 向量化完成，session={}", minioFile.getFileName(), session.getId());
//
//    }

}