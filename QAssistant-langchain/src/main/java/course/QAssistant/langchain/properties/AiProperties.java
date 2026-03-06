package course.QAssistant.langchain.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AI 服务配置属性类
 * 对应配置文件前缀：qassistant.ai
 */
@Configuration
@ConfigurationProperties(prefix = "qassistant.ai")
public class AiProperties {

    /**
     * 温度参数，控制生成文本的随机性
     * 系统默认值：0.7
     * 用户可覆盖范围：0.0 - 2.0
     * 是否热刷新：是
     */
    private Double temperature = 0.7;

    /**
     * 核采样参数，控制生成文本的多样性
     * 系统默认值：1.0
     * 用户可覆盖范围：0.0 - 1.0
     * 是否热刷新：是
     */
    private Double topP = 1.0;

    /**
     * 最大生成 Token 数
     * 系统默认值：2048
     * 用户可覆盖范围：1 - 4096
     * 是否热刷新：是
     */
    private Integer maxTokens = 2048;

    /**
     * 使用的模型名称
     * 系统默认值：gpt-3.5-turbo
     * 用户可覆盖范围：支持的模型列表
     * 是否热刷新：是
     */
    private String modelName = "gpt-3.5-turbo";

    /**
     * 请求超时时间
     * 系统默认值：30秒
     * 用户可覆盖范围：1秒 - 300秒
     * 是否热刷新：是
     */
    private Duration timeout = Duration.ofSeconds(30);

    /**
     * 失败重试次数
     * 系统默认值：3
     * 用户可覆盖范围：0 - 5
     * 是否热刷新：是
     */
    private Integer retryTimes = 3;

    /**
     * AI 服务的基础 URL
     * 系统默认值：https://api.openai.com
     * 用户可覆盖范围：任意合法 URL
     * 是否热刷新：否（需重启生效）
     */
    private String baseUrl = "https://api.openai.com";

    /**
     * API 密钥
     * 系统默认值：空
     * 用户可覆盖范围：必须提供有效密钥
     * 是否热刷新：否（需重启生效）
     */
    private String apiKey;

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getTopP() {
        return topP;
    }

    public void setTopP(Double topP) {
        this.topP = topP;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(Integer retryTimes) {
        this.retryTimes = retryTimes;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
