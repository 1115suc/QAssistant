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
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final AiProperties aiProperties;
    private final MongoTemplate mongoTemplate;
    private final RedisComponent redisComponent;
    private final MinIOFileService minioFileService;
    private final MiniofileMapper miniofileMapper;
    private final UserAiModelMapper userAiModelMapper;
    private final UserAiPreferenceMapper userAiPreferenceMapper;
    private final ChatSessionService chatSessionService;
    private final ChatSessionRepository chatSessionRepository;

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final StreamingChatLanguageModel defaultStreamingChatLanguageModel;

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
            }else  {
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

        String uid = tokenUserDTO.getUid();
        List<SessionSummaryResponse> sessionSummaries = chatSessionService.getUserSessions(uid);
        List<ChatSessionRespVO> chatSessionRespVOS = sessionSummaries.stream().map(sessionSummaryResponse -> {
            ChatSessionRespVO respVO = new ChatSessionRespVO();
            respVO.setSessionId(sessionSummaryResponse.getId());
            respVO.setTitle(sessionSummaryResponse.getTitle());
            respVO.setAiModelId(sessionSummaryResponse.getAiModelId());
            respVO.setCreatedAt(sessionSummaryResponse.getCreatedAt());
            respVO.setUpdatedAt(sessionSummaryResponse.getUpdatedAt());
            return respVO;
        }).collect(Collectors.toList());

        return R.ok(chatSessionRespVOS);
    }

    @Override
    public Flux<ServerSentEvent<String>> streamChat(ChatRequestVO chatRequestVO,
                                                    String authorization, String loginType) {
        Sinks.Many<ServerSentEvent<String>> sink =
                Sinks.many().unicast().onBackpressureBuffer();

        try {
            // ① 鉴权 & 会话校验
            TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(authorization, loginType);

            ChatSession session = chatSessionRepository
                    .findByIdAndUserUid(chatRequestVO.getSessionId(), tokenUserDTO.getUid())
                    .orElse(null);

            if (session == null) {
                return errorFlux("Session not found or unauthorized");
            }

            // ② 构建 StreamingChatLanguageModel
            StreamingChatLanguageModel chatModel = buildChatModel(session);

            // ③ 加载上下文记忆
            ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(20);
            for (ChatMessageDocument msg : session.getMessages()) {
                if ("USER".equals(msg.getRole())) {
                    chatMemory.add(new UserMessage(msg.getContent()));
                } else if ("AI".equals(msg.getRole())) {
                    chatMemory.add(new AiMessage(msg.getContent()));
                }
            }

            // ④ 根据 ragEnabled 决定是否挂载 RAG，分支构建 Assistant
            ContentRetriever retriever = null;
            if (Boolean.TRUE.equals(chatRequestVO.getRagEnabled())) {
                retriever = buildRetriever(session.getId());
                if (retriever != null) {
                    log.info("RAG 已启用, sessionId: {}", session.getId());
                } else {
                    log.warn("RAG 检索器构建失败，降级为普通对话, sessionId: {}", session.getId());
                }
            }

            // 根据是否有 retriever 走不同分支
            Assistant assistant;
            if (retriever != null) {
                assistant = AiServices.builder(Assistant.class)
                        .streamingChatLanguageModel(chatModel)
                        .chatMemory(chatMemory)
                        .contentRetriever(retriever)
                        .build();
            } else {
                assistant = AiServices.builder(Assistant.class)
                        .streamingChatLanguageModel(chatModel)
                        .chatMemory(chatMemory)
                        .build();
            }

            // ⑤ 持久化用户消息
            pushMessage(session.getId(), "USER", chatRequestVO.getMessage());

            // ⑥ 发起流式请求，将回调推入 Sink
            assistant.chat(chatRequestVO.getMessage())
                    .onNext(chunk -> sink.tryEmitNext(
                            ServerSentEvent.<String>builder()
                                    .data(chunk)
                                    .build()
                    ))
                    .onComplete(response -> {
                        try {
                            pushMessage(session.getId(), "AI", response.content().text());
                        } catch (Exception e) {
                            log.error("Failed to save AI message", e);
                        }
                        sink.tryEmitNext(ServerSentEvent.<String>builder()
                                .event("finish").data("[DONE]").build());
                        sink.tryEmitComplete();
                    })
                    .onError(e -> {
                        log.error("Streaming error", e);
                        sink.tryEmitNext(ServerSentEvent.<String>builder()
                                .event("error").data("Error: " + e.getMessage()).build());
                        sink.tryEmitError(e);
                    })
                    .start();

        } catch (Exception e) {
            log.error("streamChat setup error", e);
            return errorFlux(e.getMessage());
        }

        return sink.asFlux()
                .doOnCancel(() -> log.info("Client disconnected, sessionId: {}",
                        chatRequestVO.getSessionId()));
    }

    @Override
    public R ingestDocument(RagUploadVO ragUploadVO, String token, String loginType) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);

        try {
            ChatSession session = chatSessionRepository
                    .findByIdAndUserUid(ragUploadVO.getSessionId(), tokenUserDTO.getUid())
                    .orElseThrow(() -> new QAWebException("Session not found or unauthorized"));

            Miniofile minioFile = miniofileMapper.selectOne(
                    new LambdaQueryWrapper<Miniofile>()
                            .eq(Miniofile::getId, ragUploadVO.getFileId())
                            .eq(Miniofile::getUid, tokenUserDTO.getUid())
            );
            if (ObjectUtil.isNull(minioFile)) {
                throw new QAWebException("指定文件不存在");
            }

            ingestToEmbeddingStore(minioFile, session.getId());

            return R.ok("文档上传成功，可在发送消息时传 ragEnabled=true 开启检索");

        } catch (QAWebException e) {
            return R.error(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to ingest document", e);
            return R.error("文档处理失败: " + e.getMessage());
        }
    }


    /**
     * 文档解析 + 向量化入库
     */
    private void ingestToEmbeddingStore(Miniofile minioFile, String sessionId) {
        InputStream inputStream = minioFileService.downloadFile(
                minioFile.getBucket(), minioFile.getObjectName()
        );

        Document document = new TextDocumentParser().parse(inputStream);
        document.metadata().put("sessionId", sessionId);
        document.metadata().put("fileId", String.valueOf(minioFile.getId()));

        EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(500, 50))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build()
                .ingest(document);
    }

    /**
     * 构建 RAG 检索器，构建失败时返回 null 降级为普通对话
     */
    private ContentRetriever buildRetriever(String sessionId) {
        try {
            Filter filter = MetadataFilterBuilder.metadataKey("sessionId").isEqualTo(sessionId);
            return EmbeddingStoreContentRetriever.builder()
                    .embeddingStore(embeddingStore)
                    .embeddingModel(embeddingModel)
                    .filter(filter)
                    .maxResults(3)
                    .minScore(0.7)
                    .build();
        } catch (Exception e) {
            log.warn("RAG 检索器构建失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 构建 StreamingChatLanguageModel
     * customModel 不为空取自定义，否则取系统默认
     * preference  不为空取自定义，否则取系统默认
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

        String baseUrl   = ObjectUtil.isNotNull(customModel) ? customModel.getBaseUrl()   : aiProperties.getBaseUrl();
        String apiKey    = ObjectUtil.isNotNull(customModel) ? customModel.getApiKey()    : aiProperties.getApiKey();
        String modelName = ObjectUtil.isNotNull(customModel) ? customModel.getModelName() : aiProperties.getModelName();

        Double temperature = ObjectUtil.isNotNull(preference) ? Convert.toDouble(preference.getTemperature()) : aiProperties.getTemperature();
        Double topP        = ObjectUtil.isNotNull(preference) ? Convert.toDouble(preference.getTopP())        : aiProperties.getTopP();

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
     * push 消息到 MongoDB messages 数组 + 更新 updatedAt
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
     * 统一错误响应
     */
    private Flux<ServerSentEvent<String>> errorFlux(String message) {
        return Flux.just(ServerSentEvent.<String>builder()
                .event("error")
                .data("Error: " + message)
                .build());
    }
}
