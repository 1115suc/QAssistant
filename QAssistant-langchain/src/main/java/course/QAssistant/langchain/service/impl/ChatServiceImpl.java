package course.QAssistant.langchain.service.impl;

import course.QAssistant.langchain.service.ChatService;
import course.QAssistant.langchain.service.IChatAgent;
import course.QAssistant.langchain.context.ContextValidator;
import course.QAssistant.langchain.context.ConversationContextHolder;
import course.QAssistant.langchain.entity.UserAiConfig;
import course.QAssistant.langchain.factory.AiModelFactory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天服务实现类
 *
 * @author 1115suc
 * @date 2026-03-06
 * @description 使用 LangChain4j 模型实现 ChatService 接口。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatLanguageModel defaultChatLanguageModel;
    private final StreamingChatLanguageModel defaultStreamingChatLanguageModel;
    private final AiModelFactory aiModelFactory;
    private final UserAiConfigService userAiConfigService;

    // 简单的本地缓存，实际生产应使用 Caffeine 或 Redis
    private final Map<String, IChatAgent> agentCache = new ConcurrentHashMap<>();

    /**
     * 同步发送提示到 AI 模型并返回响应。
     *
     * @param userId 用户的唯一标识符。
     * @param prompt 用户的输入提示。
     * @return AI 模型的响应字符串。
     * @throws RuntimeException 如果交互失败或模型未配置。
     */
    @Override
    public String sendPrompt(String userId, String prompt) {
        // 获取当前 AI UID 并校验上下文
        String aiUid = ConversationContextHolder.getAiUid();
        ContextValidator.assertContext(userId, aiUid);

        log.info("正在为用户 {} 发送提示，AI UID: {}", userId, aiUid);
        
        try {
            // 1. 如果指定了 aiUid，尝试加载用户自定义配置
            if (aiUid != null && !aiUid.isEmpty()) {
                IChatAgent agent = getOrCreateAgent(aiUid);
                if (agent != null) {
                    return agent.chat(prompt);
                }
            }

            // 2. 降级使用系统默认模型
            if (defaultChatLanguageModel == null) {
                throw new IllegalStateException("未配置默认 ChatLanguageModel。");
            }
            // 注意：直接使用 defaultChatLanguageModel 不会自动管理 ChatMemory，
            // 建议系统默认 AI 也通过 AiServices 封装，这里仅作演示
            return defaultChatLanguageModel.generate(prompt);

        } catch (Exception e) {
            log.error("为用户 {} 发送提示时出错：{}", userId, e);
            throw new RuntimeException("发送提示失败", e);
        }
    }

    private IChatAgent getOrCreateAgent(String aiUid) {
        return agentCache.computeIfAbsent(aiUid, key -> {
            // 从数据库加载配置
            UserAiConfig config = userAiConfigService.getByAiUid(key);
            if (config == null) {
                log.warn("未找到 AI 配置：{}，将使用默认模型", key);
                return null;
            }
            // 动态构建代理
            return aiModelFactory.createChatAgent(config);
        });
    }

    @Override
    public TokenStream streamPrompt(String userId, String prompt) {
        return null;
    }
}
