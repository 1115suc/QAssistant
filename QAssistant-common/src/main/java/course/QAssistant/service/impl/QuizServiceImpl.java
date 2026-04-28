package course.QAssistant.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import course.QAssistant.LangChain4j.config.RetrieverFactory;
import course.QAssistant.exception.QAWebException;
import course.QAssistant.handler.RedisComponent;
import course.QAssistant.mapper.UserAiModelMapper;
import course.QAssistant.mapper.UserAiPreferenceMapper;
import course.QAssistant.pojo.dto.TokenUserDTO;
import course.QAssistant.pojo.po.Miniofile;
import course.QAssistant.pojo.po.UserAiModel;
import course.QAssistant.pojo.po.UserAiPreference;
import course.QAssistant.pojo.quiz.QuizQuestion;
import course.QAssistant.pojo.quiz.QuizResult;
import course.QAssistant.pojo.vo.request.GenerateQuestionsRequestVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.service.QuizService;
import course.QAssistant.util.QAssistantLangChainUtil;
import course.QAssistant.util.QAssistantLangChainUtil.QuestionType;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private static final Double DEFAULT_TEMPERATURE = 0.7;
    private static final Double DEFAULT_TOP_P = 1.0;
    private static final Integer DEFAULT_MAX_TOKENS = 2048 * 8;
    private static final int PARALLEL_TIMEOUT_SECONDS = 60 * 10;
    // RAG 检索内容注入到 prompt 的最大字符数，防止挤占题目生成空间
    private static final int RAG_CONTEXT_MAX_LENGTH = 2000;

    private final RedisComponent redisComponent;
    private final UserAiModelMapper userAiModelMapper;
    private final UserAiPreferenceMapper userAiPreferenceMapper;
    private final QAssistantLangChainUtil generateUtil;
    private final EmbeddingStore<TextSegment> chromaEmbeddingStore;
    private final OpenAiChatModel defaultChatModel;
    private final OpenAiStreamingChatModel defaultStreamingChatModel;
    private final RetrieverFactory retrieverFactory;

    private ExecutorService quizExecutor;

    @PostConstruct
    public void initExecutor() {
        quizExecutor = new ThreadPoolExecutor(
                3, 9,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(50),
                r -> {
                    Thread t = new Thread(r, "quiz-gen-" + System.nanoTime());
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        log.info("[QuizExecutor] 线程池初始化完成");
    }

    @PreDestroy
    public void shutdownExecutor() {
        quizExecutor.shutdown();
        try {
            if (!quizExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                quizExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            quizExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // ================================================================
    // 对外接口
    // ================================================================

    @Override
    public String generateQuiz(GenerateQuestionsRequestVO requestVO, String token, String loginType) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);
        generateUtil.validateRequest(requestVO);

        UserAiPreference preference = resolvePreference(tokenUserDTO.getUid(), requestVO);
        OpenAiChatModel chatModel = generateModel(tokenUserDTO.getUid(), preference);

        List<QuestionType> activeTypes = getActiveTypes(requestVO);

        log.debug("[QuizGen] uid={} | 题型={} | topic={} | difficulty={}",
                tokenUserDTO.getUid(), activeTypes, requestVO.getTopic(), requestVO.getDifficulty());

        if (activeTypes.size() == 1) {
            return generateSingle(chatModel, requestVO, activeTypes.get(0));
        }

        return generateParallel(chatModel, requestVO, activeTypes);
    }

    // ================================================================
    // 并行生成
    // ================================================================

    private String generateParallel(OpenAiChatModel chatModel,
                                    GenerateQuestionsRequestVO requestVO,
                                    List<QuestionType> activeTypes) {

        Map<QuestionType, CompletableFuture<QuizResult>> futures = new LinkedHashMap<>();

        for (QuestionType type : activeTypes) {
            CompletableFuture<QuizResult> future = CompletableFuture
                    .supplyAsync(() -> generateForType(chatModel, requestVO, type), quizExecutor)
                    .exceptionally(ex -> {
                        log.error("[QuizGen] 题型 {} 生成失败: {}", type, ex.getMessage(), ex);
                        return buildEmptyResult(type);
                    });
            futures.put(type, future);
        }

        try {
            CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0]))
                    .get(PARALLEL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.error("[QuizGen] 并行生成超时 ({}s)，返回已完成部分", PARALLEL_TIMEOUT_SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new QAWebException("题目生成被中断");
        } catch (ExecutionException e) {
            throw new QAWebException("题目生成异常: " + e.getCause().getMessage());
        }

        QuizResult merged = mergeResults(futures);
        log.info("[QuizGen] 并行生成完成，合并 {} 个题型", activeTypes.size());

        return JSONUtil.toJsonStr(merged);
    }

    // ================================================================
    // 单题型生成（同步）
    // ================================================================

    private String generateSingle(OpenAiChatModel chatModel,
                                  GenerateQuestionsRequestVO requestVO,
                                  QuestionType type) {
        QuizResult result = generateForType(chatModel, requestVO, type);
        return JSONUtil.toJsonStr(result);
    }

    private QuizResult generateForType(OpenAiChatModel chatModel,
                                       GenerateQuestionsRequestVO requestVO,
                                       QuestionType type) {
        log.debug("[QuizGen] 开始生成题型={} | thread={}", type, Thread.currentThread().getName());
        long start = System.currentTimeMillis();

        // ✅ 第一步：先做 RAG 检索（需要在构建 ChatRequest 之前完成）
        String ragContext = null;
        if (CollectionUtil.isNotEmpty(requestVO.getFileIds())) {
            log.debug("[QuizGen] 启用 RAG 检索 | 题型={} | 文件={}", type, requestVO.getFileIds());
            ragContext = retrieveRagContext(requestVO.getFileIds(), requestVO.getTopic(), type);
        }

        // ✅ 第二步：根据是否有 RAG 上下文选择不同的 UserMessage
        UserMessage userMessage = (ragContext != null)
                ? generateUtil.buildUserMessageForTypeWithContext(requestVO, type, ragContext)
                : generateUtil.buildUserMessageForType(requestVO, type);

        // ✅ 第三步：把带上下文的 userMessage 放进 ChatRequest
        ChatRequest chatRequest = ChatRequest.builder()
                .responseFormat(generateUtil.buildResponseFormat())
                .messages(generateUtil.buildSystemMessage(), userMessage)
                .build();

        ChatResponse chatResponse = chatModel.chat(chatRequest);
        String rawJson = chatResponse.aiMessage().text();

        log.debug("[QuizGen] 题型={} 完成，耗时={}ms | RAG={}",
                type, System.currentTimeMillis() - start, ragContext != null);

        return generateUtil.parseResult(rawJson);
    }

    // ================================================================
    // RAG 检索
    // ================================================================

    /**
     * 执行 RAG 检索，将结果拼接为字符串注入 prompt
     * 检索失败时自动降级返回 null，不阻断题目生成流程
     */
    private String retrieveRagContext(List<Long> fileIds, String topic, QuestionType type) {
        try {
            // 用"主题 + 题型"组合 query，让召回更精准
            String query = "%s %s 相关知识点".formatted(topic, typeLabel(type));

            ContentRetriever contentRetriever = retrieverFactory.buildFileRetriever(fileIds);
            List<Content> contents = contentRetriever.retrieve(Query.from(query));

            if (contents.isEmpty()) {
                log.debug("[RAG] 题型={} 检索无结果", type);
                return null;
            }

            String ragContext = contents.stream()
                    .map(c -> c.textSegment().text())
                    .collect(Collectors.joining("\n\n---\n\n"));

            // 截断防止 token 溢出
            if (ragContext.length() > RAG_CONTEXT_MAX_LENGTH) {
                ragContext = ragContext.substring(0, RAG_CONTEXT_MAX_LENGTH) + "\n...(内容已截断)";
            }

            log.debug("[RAG] 题型={} 检索到 {} 个片段，上下文长度={}",
                    type, contents.size(), ragContext.length());
            return ragContext;

        } catch (Exception e) {
            // RAG 失败不中断流程，降级为无上下文出题
            log.warn("[RAG] 检索失败，降级为无上下文生成: {}", e.getMessage());
            return null;
        }
    }

    private String typeLabel(QuestionType type) {
        return switch (type) {
            case CHOICE -> "选择题";
            case FILL   -> "填空题";
            case QA     -> "问答题";
        };
    }

    // ================================================================
    // 结果合并
    // ================================================================

    private QuizResult mergeResults(Map<QuestionType, CompletableFuture<QuizResult>> futures) {
        QuizResult merged = new QuizResult();
        List<QuizQuestion> allQuestions = new ArrayList<>();

        for (Map.Entry<QuestionType, CompletableFuture<QuizResult>> entry : futures.entrySet()) {
            QuestionType type = entry.getKey();
            try {
                QuizResult partial = entry.getValue().getNow(buildEmptyResult(type));
                if (partial != null && partial.getQuestions() != null) {
                    allQuestions.addAll(partial.getQuestions());
                }
            } catch (Exception e) {
                log.warn("[QuizGen] 合并题型 {} 失败，跳过", type);
            }
        }

        futures.values().stream()
                .map(f -> f.getNow(null))
                .filter(r -> r != null && r.getTitle() != null)
                .findFirst()
                .ifPresent(r -> {
                    merged.setTitle(r.getTitle());
                    merged.setTopic(r.getTopic());
                });

        merged.setQuestions(allQuestions);
        return merged;
    }

    private QuizResult buildEmptyResult(QuestionType type) {
        QuizResult empty = new QuizResult();
        empty.setQuestions(Collections.emptyList());
        return empty;
    }

    // ================================================================
    // 辅助方法
    // ================================================================

    private List<QuestionType> getActiveTypes(GenerateQuestionsRequestVO requestVO) {
        List<QuestionType> types = new ArrayList<>();
        if (generateUtil.safeInt(requestVO.getChoiceCount()) > 0) types.add(QuestionType.CHOICE);
        if (generateUtil.safeInt(requestVO.getFillCount())   > 0) types.add(QuestionType.FILL);
        if (generateUtil.safeInt(requestVO.getQaCount())     > 0) types.add(QuestionType.QA);
        return types;
    }

    private UserAiPreference resolvePreference(String uid, GenerateQuestionsRequestVO requestVO) {
        if (ObjectUtil.isNull(requestVO.getAiModelId())) {
            return new UserAiPreference();
        }
        return userAiPreferenceMapper.selectOne(
                new LambdaQueryWrapper<UserAiPreference>()
                        .eq(UserAiPreference::getUserUid, uid)
                        .eq(UserAiPreference::getAiModelId, requestVO.getAiModelId())
        );
    }

    private OpenAiChatModel generateModel(String uid, UserAiPreference preference) {
        if (ObjectUtil.isNull(preference) || ObjectUtil.isNull(preference.getAiModelId())) {
            return defaultChatModel;
        }

        UserAiModel customModel = userAiModelMapper.selectOne(
                new LambdaQueryWrapper<UserAiModel>()
                        .eq(UserAiModel::getId, preference.getAiModelId())
                        .eq(UserAiModel::getUserUid, uid)
        );

        if (ObjectUtil.isNull(customModel)) {
            log.warn("[QuizGen] 用户 {} 的模型 {} 不存在，回退默认模型", uid, preference.getAiModelId());
            return defaultChatModel;
        }

        Double temperature = Convert.toDouble(preference.getTemperature(), DEFAULT_TEMPERATURE);
        Double topP        = Convert.toDouble(preference.getTopP(),        DEFAULT_TOP_P);
        Integer maxTokens  = Convert.toInt(preference.getMaxTokens(),      DEFAULT_MAX_TOKENS);

        return OpenAiChatModel.builder()
                .baseUrl(customModel.getBaseUrl())
                .apiKey(customModel.getApiKey())
                .modelName(customModel.getModelName())
                .temperature(temperature)
                .topP(topP)
                .maxTokens(maxTokens)
                .supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}