package course.QAssistant.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import course.QAssistant.LangChain4j.config.RetrieverFactory;
import course.QAssistant.LangChain4j.service.ConsultantService;
import course.QAssistant.LangChain4j.service.RagAssistant;
import course.QAssistant.exception.QAWebException;
import course.QAssistant.handler.RedisComponent;
import course.QAssistant.mapper.MiniofileMapper;
import course.QAssistant.mapper.UserAiModelMapper;
import course.QAssistant.mapper.UserAiPreferenceMapper;
import course.QAssistant.minio.service.MinIOFileService;
import course.QAssistant.pojo.dto.CreateSessionRequest;
import course.QAssistant.pojo.dto.SessionSummaryResponse;
import course.QAssistant.pojo.dto.TokenUserDTO;
import course.QAssistant.pojo.po.ChatSession;
import course.QAssistant.pojo.po.Miniofile;
import course.QAssistant.pojo.po.UserAiModel;
import course.QAssistant.pojo.po.UserAiPreference;
import course.QAssistant.pojo.vo.request.ChatRequestVO;
import course.QAssistant.pojo.vo.request.CreateSessionRequestVO;
import course.QAssistant.pojo.vo.request.RagUploadVO;
import course.QAssistant.pojo.vo.response.ChatSessionRespVO;
import course.QAssistant.pojo.vo.response.DetailChatMessageVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.repository.ChatSessionRepository;
import course.QAssistant.service.ChatService;
import course.QAssistant.service.ChatSessionService;
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
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.sql.Struct;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final Double DEFAULT_TEMPERATURE = 0.7;
    private static final Double DEFAULT_TOP_P = 1.0;
    private static final Integer DEFAULT_MAX_TOKENS = 2048;
    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 50;

    private final RedisComponent redisComponent;
    private final MiniofileMapper miniofileMapper;
    private final MinIOFileService minioFileService;
    private final UserAiModelMapper userAiModelMapper;
    private final UserAiPreferenceMapper userAiPreferenceMapper;
    private final ChatSessionService chatSessionService;
    private final ChatSessionRepository chatSessionRepository;

    private final RetrieverFactory retrieverFactory;
    private final ChatMemoryProvider chatMemoryProvider;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> chromaEmbeddingStore;
    private final OpenAiStreamingChatModel defaultStreamingChatModel;

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

    // TODO
    @Override
    public R<DetailChatMessageVO> getSessionDetail(String sessionId, String token, String loginType) {
        try {
            TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);

            // 使用自定义 Repository 方法查询，包含 messages 且按时间倒序
            ChatSession session = chatSessionRepository
                    .findByIdWithMessagesSortedDesc(sessionId)
                    .orElseThrow(() -> new QAWebException("会话不存在"));

            // 鉴权：确保会话属于当前用户
            if (!session.getUserUid().equals(tokenUserDTO.getUid())) {
                throw new QAWebException("无权访问该会话");
            }

            DetailChatMessageVO detailChatMessageVO = new DetailChatMessageVO();
            detailChatMessageVO.setMessages(session.getMessages());
            detailChatMessageVO.setSessionId(session.getId());
            detailChatMessageVO.setTitle(session.getTitle());
            detailChatMessageVO.setAiModelId(session.getAiModelId());
            detailChatMessageVO.setCreatedAt(session.getCreatedAt());
            detailChatMessageVO.setUpdatedAt(session.getUpdatedAt());

            return R.ok(detailChatMessageVO);
        } catch (QAWebException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取会话详情失败：{}", e.getMessage());
            throw new QAWebException("获取会话失败");
        }
    }

    @Override
    public R ingestDocument(RagUploadVO ragUploadVO, String token, String loginType) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);

        try {
            ChatSession session = chatSessionRepository
                    .findByIdAndUserUid(ragUploadVO.getSessionId(), tokenUserDTO.getUid())
                    .orElseThrow(() -> new QAWebException("会话不存在"));

            Miniofile minioFile = miniofileMapper.selectOne(
                    new LambdaQueryWrapper<Miniofile>()
                            .eq(Miniofile::getId, ragUploadVO.getFileId())
                            .eq(Miniofile::getUid, tokenUserDTO.getUid())
            );
            if (ObjectUtil.isNull(minioFile)) {
                throw new QAWebException("指定文件不存在");
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

    /**
     * 根据用户 uid 和 fileIds 批量向量化文档（无会话上下文，用于出题 RAG 场景）
     *
     * @param fileIds   文件 ID 列表
     * @param token     用户 token
     * @param loginType 登录类型
     */
    // @Override
    public R ingestDocumentsByFileIds(List<Long> fileIds, String token, String loginType) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);

        if (CollectionUtil.isEmpty(fileIds)) {
            return R.error("文件 ID 列表不能为空");
        }

        // 查询属于该用户的所有文件
        List<Miniofile> minioFiles = miniofileMapper.selectList(
                new LambdaQueryWrapper<Miniofile>()
                        .in(Miniofile::getId, fileIds)
                        .eq(Miniofile::getUid, tokenUserDTO.getUid())
        );

        // 校验：是否所有 fileId 都能找到
        if (minioFiles.size() != fileIds.size()) {
            List<Long> foundIds = minioFiles.stream()
                    .map(Miniofile::getId)
                    .toList();
            List<Long> missingIds = fileIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            log.warn("[RAG] 部分文件不存在或无权限，missingIds={}", missingIds);
            return R.error("以下文件不存在或无权访问：" + missingIds);
        }

        // 批量向量化，逐个处理，单个失败不中断整体
        List<String> successList = new ArrayList<>();
        List<String> failList    = new ArrayList<>();

        for (Miniofile minioFile : minioFiles) {
            try {
                // ✅ 无会话场景：用 uid 作为 namespace 隔离向量数据
                ingestToEmbeddingStore(minioFile, null);
                successList.add(minioFile.getFileName());
                log.info("[RAG] 文件向量化完成 | file={} | uid={}",
                        minioFile.getFileName(), tokenUserDTO.getUid());
            } catch (Exception e) {
                failList.add(minioFile.getFileName());
                log.error("[RAG] 文件向量化失败 | file={} | uid={}",
                        minioFile.getFileName(), tokenUserDTO.getUid(), e);
            }
        }

        // 构建响应
        if (failList.isEmpty()) {
            return R.ok("全部文件向量化成功，共 " + successList.size() + " 个");
        } else if (successList.isEmpty()) {
            return R.error("全部文件向量化失败：" + failList);
        } else {
            // 部分成功：返回 ok 但携带失败信息，让调用方决定是否重试
            Map<String, Object> result = new HashMap<>();
            result.put("success", successList);
            result.put("failed", failList);
            return R.ok("文件向量化成功", result);
        }
    }

    @Override
    public Flux<String> streamChat(ChatRequestVO chatRequestVO,
                                   String authorization, String loginType) {
        // 1. 鉴权 & 会话归属校验
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(authorization, loginType);
        ChatSession session = chatSessionRepository
                .findByIdAndUserUid(chatRequestVO.getSessionId(), tokenUserDTO.getUid())
                .orElse(null);

        if (session == null) {
            throw new QAWebException("会话不存在");
        }

        UserAiPreference preference = userAiPreferenceMapper.selectOne(
                new LambdaQueryWrapper<UserAiPreference>()
                        .eq(UserAiPreference::getUserUid, session.getUserUid())
                        .eq(UserAiPreference::getAiModelId, session.getAiModelId())
        );

        String prompt = ObjectUtil.isNotNull(preference) && StrUtil.isNotBlank(preference.getSystemPrompt())
                ? preference.getSystemPrompt()
                : "你是一个专业的文档助手。请根据提供的上下文信息，准确回答用户问题。";

        // 2. 构建模型
        OpenAiStreamingChatModel chatModel = buildChatModel(session, preference);

        try {
            // 3. 构建 Assistant
            Flux<String> responseFlux;
            if (chatRequestVO.getRagEnabled()) {
                ContentRetriever sessionContentRetriever = retrieverFactory.buildSessionOnlyRetriever(session.getId());
                RagAssistant assistant = AiServices.builder(RagAssistant.class)
                        .streamingChatLanguageModel(chatModel)
                        .contentRetriever(sessionContentRetriever)
                        .chatMemoryProvider(chatMemoryProvider)
                        .build();

                responseFlux = assistant.chat(session.getId(), chatRequestVO.getMessage(), prompt);

            } else {
                ConsultantService consultantService = AiServices.builder(ConsultantService.class)
                        .streamingChatLanguageModel(chatModel)
                        .chatMemoryProvider(chatMemoryProvider)
                        .build();

                responseFlux = consultantService.chat(chatRequestVO.getMessage(), session.getId(), prompt);
            }

            // 5. 流式响应处理：拼接完整回复并持久化
            StringBuilder aiResponseBuilder = new StringBuilder();

            return responseFlux
                    .doOnNext(token -> aiResponseBuilder.append(token))
                    .doOnComplete(() -> {
                    })
                    .doOnError(error -> {
                        log.error("[Chat] session={} 流式对话异常", session.getId(), error);
                    })
                    .onErrorResume(error -> {
                        return Flux.just("AI 响应异常：" + error.getMessage());
                    });
        } catch (Exception e) {
            log.error("[Chat] session={} 构建模型异常", session.getId(), e);
            throw new QAWebException("构建模型异常");
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 核心：向量化并写入 Chroma
     */
    private void ingestToEmbeddingStore(Miniofile minioFile, String sessionId) {

        // -------- 1. 加载文档 --------
        String previewUrl = minioFileService.getPreviewUrl(
                minioFile.getBucket(), minioFile.getObjectName());

        DocumentParser parser = resolveParser(minioFile.getFileName());
        Document document = UrlDocumentLoader.load(previewUrl, parser);

        if (document.text() == null || document.text().isBlank()) {
            log.warn("[RAG] 文件 {} 内容为空，跳过向量化", minioFile.getFileName());
            return;
        }

        // -------- 2. 分段 --------
        DocumentSplitter splitter = DocumentSplitters.recursive(CHUNK_SIZE, CHUNK_OVERLAP);
        List<TextSegment> segments = splitter.split(document);

        if (segments.isEmpty()) {
            log.warn("[RAG] 文件 {} 分段结果为空，跳过", minioFile.getFileName());
            return;
        }

        // -------- 3. 构建带 metadata 的分段 + 生成 embedding --------
        List<TextSegment> finalSegments = new ArrayList<>(segments.size());
        List<Embedding> embeddings = new ArrayList<>(segments.size());

        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);

            if (segment.text() == null || segment.text().isBlank()) {
                log.debug("[RAG] 第 {} 段为空，跳过", i);
                continue;
            }

            //  Chroma metadata：值只支持 String / int / float / boolean
            Metadata metadata = new Metadata();
            if (StrUtil.isNotBlank(sessionId)) {
                metadata.put("sessionId", sessionId);
            }
            metadata.put("fileId", minioFile.getId());
            metadata.put("index", String.valueOf(i));

            TextSegment newSegment = TextSegment.from(segment.text(), metadata);
            Embedding embedding = embeddingModel.embed(newSegment).content();

            if (i == 0) {
                log.info("[RAG] 模型输出向量维度: {}", embedding.vector().length);
            }
            if (embedding.vector().length == 0) {
                log.error("[RAG] 第 {} 段生成的向量维度为 0，跳过！", i);
                continue;
            }

            finalSegments.add(newSegment);
            embeddings.add(embedding);
        }

        if (finalSegments.isEmpty()) {
            log.warn("[RAG] 文件 {} 没有有效分段可存储", minioFile.getFileName());
            return;
        }

        // -------- 4. 批量存入 Chroma --------
        chromaEmbeddingStore.addAll(embeddings, finalSegments);

        log.info("[RAG] ✅ 文件 {} 向量化完成, session={}, 有效chunks={}",
                minioFile.getFileName(), sessionId, finalSegments.size());
    }

    // ==================== 文件解析器 ====================
    private static final DocumentParser TEXT_PARSER = new TextDocumentParser();
    private static final DocumentParser PDF_PARSER = new ApachePdfBoxDocumentParser();
    private static final DocumentParser OFFICE_PARSER = new ApachePoiDocumentParser();

    private static final Set<String> OFFICE_EXTENSIONS = Set.of(
            "doc", "docx", "ppt", "pptx", "xls", "xlsx"
    );

    private DocumentParser resolveParser(String fileName) {
        String ext = getExtension(fileName);
        if (ext == null) throw new QAWebException("不支持的文件格式");
        if ("pdf".equals(ext)) return PDF_PARSER;
        if (OFFICE_EXTENSIONS.contains(ext)) return OFFICE_PARSER;
        return TEXT_PARSER;
    }

    private String getExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) return null;
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == fileName.length() - 1) return null;
        return fileName.substring(lastDot + 1).toLowerCase();
    }

    //  构建 StreamingChatLanguageModel（自定义模型 > 系统默认）
    private OpenAiStreamingChatModel buildChatModel(ChatSession session, UserAiPreference preference) {
        if (session.getAiModelId() == null) {
            return defaultStreamingChatModel;
        }

        UserAiModel customModel = userAiModelMapper.selectById(session.getAiModelId());
        if (ObjectUtil.isNull(customModel)) {
            log.debug("[Chat] 模型 {} 不存在，使用默认模型", session.getAiModelId());
            throw new QAWebException("模型不存在，使用默认模型");
        }

        String baseUrl = customModel.getBaseUrl();
        String apiKey = customModel.getApiKey();
        String modelName = customModel.getModelName();

        Double temperature = ObjectUtil.isNotNull(preference)
                ? Convert.toDouble(preference.getTemperature())
                : DEFAULT_TEMPERATURE;
        Double topP = ObjectUtil.isNotNull(preference)
                ? Convert.toDouble(preference.getTopP())
                : DEFAULT_TOP_P;
        Integer maxTokens = ObjectUtil.isNotNull(preference)
                ? Convert.toInt(preference.getMaxTokens())
                : DEFAULT_MAX_TOKENS;

        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .topP(topP)
                .maxTokens(maxTokens)
                .build();
    }
}