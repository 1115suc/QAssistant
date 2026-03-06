package course.QAssistant.langchain.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 用户自定义 AI 模型配置实体
 */
@Data
public class UserAiConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String userUid;
    private String aiUid;
    private String modelName;
    private String baseUrl;
    private String apiKey;
    private String provider;
    
    // 个性化偏好
    private Double temperature;
    private Double topP;
    private Integer maxTokens;
    private String systemPrompt;
}
