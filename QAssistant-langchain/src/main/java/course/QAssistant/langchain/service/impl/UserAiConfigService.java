package course.QAssistant.langchain.service.impl;

import course.QAssistant.langchain.entity.UserAiConfig;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户 AI 配置服务 Mock 实现
 * 模拟从数据库获取用户 AI 配置
 */
@Service
public class UserAiConfigService {

    // 模拟数据库缓存
    private static final Map<String, UserAiConfig> DB_MOCK = new HashMap<>();

    static {
        UserAiConfig config = new UserAiConfig();
        config.setUserUid("user_123");
        config.setAiUid("ai_gpt4_custom");
        config.setApiKey("sk-mock-key");
        config.setBaseUrl("https://api.openai.com/v1");
        config.setModelName("gpt-4");
        config.setTemperature(0.5);
        config.setSystemPrompt("你是一个严谨的助手");
        DB_MOCK.put("ai_gpt4_custom", config);
    }

    /**
     * 根据 AI UID 获取用户配置
     * @param aiUid AI 模型 ID
     * @return UserAiConfig
     */
    public UserAiConfig getByAiUid(String aiUid) {
        // 实际开发应查询数据库：SELECT * FROM user_ai_model WHERE id = ?
        return DB_MOCK.get(aiUid);
    }
}
