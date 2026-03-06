package course.QAssistant.langchain.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * AI 服务响应传输类
 * 用于封装从 AI 服务返回给调用方的响应数据
 */
public class AiChatResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话唯一标识符
     * 取值范围：UUID 字符串
     * 单位：无
     * 是否可空：否
     */
    private String conversationId;

    /**
     * AI 回复的内容
     * 取值范围：任意文本字符串
     * 单位：字符
     * 是否可空：是（当仅返回事件或状态时可为空）
     */
    private String content;

    /**
     * 响应元数据
     * 取值范围：键值对集合，包含 token 消耗、处理时间等
     * 单位：无
     * 是否可空：是
     */
    private Map<String, Object> metadata;

    /**
     * 响应状态码
     * 取值范围：200（成功），500（失败）等
     * 单位：无
     * 是否可空：否
     */
    private Integer code;

    /**
     * 错误信息或提示信息
     * 取值范围：任意描述性文本
     * 单位：字符
     * 是否可空：是
     */
    private String message;

    public AiChatResponse() {
    }

    public AiChatResponse(String conversationId, String content, Map<String, Object> metadata, Integer code, String message) {
        this.conversationId = conversationId;
        this.content = content;
        this.metadata = metadata;
        this.code = code;
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "AiChatResponse{" +
                "conversationId='" + conversationId + '\'' +
                ", content='" + content + '\'' +
                ", metadata=" + metadata +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
