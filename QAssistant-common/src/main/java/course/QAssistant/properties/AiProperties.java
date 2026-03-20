package course.QAssistant.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AI 服务配置属性类
 * 对应配置文件前缀：qassistant.aiconfig
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "qassistant.aiconfig")
public class AiProperties {
    // 温度参数，控制生成文本的随机性
    private Double temperature = 0.7;
    // 核采样参数，控制生成文本的多样性
    private Double topP = 1.0;

    private Integer maxTokens = 2048;

    private String modelName = "deepseek-chat";

    private String baseUrl = "https://api.deepseek.com";

    private String apiKey;

    private String embeddingModelName;

    private String embeddingModelUrl;

    private String embeddingApiKey;

    private Duration timeout = Duration.ofSeconds(30);

    private Integer retryTimes = 3;
}
