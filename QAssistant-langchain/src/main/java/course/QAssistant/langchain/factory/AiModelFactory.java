package course.QAssistant.langchain.factory;

import course.QAssistant.langchain.entity.UserAiConfig;
import course.QAssistant.langchain.service.IChatAgent;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * AI 模型构建工厂
 * 负责根据用户配置动态创建 ChatLanguageModel 和 AiService
 */
@Component
public class AiModelFactory {

    private final ChatMemoryProvider chatMemoryProvider;

    public AiModelFactory(ChatMemoryProvider chatMemoryProvider) {
        this.chatMemoryProvider = chatMemoryProvider;
    }

    /**
     * 根据配置创建 AI 代理服务
     *
     * @param config 用户 AI 配置
     * @return IChatAgent 实例
     */
    public IChatAgent createChatAgent(UserAiConfig config) {
        ChatLanguageModel model = createChatLanguageModel(config);

        return AiServices.builder(IChatAgent.class)
                .chatLanguageModel(model)
                .chatMemoryProvider(chatMemoryProvider) // 使用全局配置的 MemoryProvider，基于 userUid+aiUid 隔离
                .build();
    }

    /**
     * 创建 ChatLanguageModel 实例
     *
     * @param config 用户 AI 配置
     * @return ChatLanguageModel
     */
    public ChatLanguageModel createChatLanguageModel(UserAiConfig config) {
        // 这里以 OpenAI 为例，实际可扩展支持更多模型
        return OpenAiChatModel.builder()
                .apiKey(config.getApiKey())
                .baseUrl(config.getBaseUrl())
                .modelName(config.getModelName())
                .temperature(config.getTemperature() != null ? config.getTemperature() : 0.7)
                .topP(config.getTopP() != null ? config.getTopP() : 1.0)
                .maxTokens(config.getMaxTokens() != null ? config.getMaxTokens() : 2048)
                .timeout(Duration.ofSeconds(30))
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
