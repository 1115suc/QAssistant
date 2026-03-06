package course.QAssistant.langchain.dto;

import java.io.Serializable;

/**
 * AI 服务事件传输类
 * 用于在流式调用或异步处理中传输事件通知
 */
public class AiEventResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件类型
     * 取值范围：STARTED, PROCESSING, COMPLETED, FAILED, STREAM_CHUNK
     * 单位：枚举字符串
     * 是否可空：否
     */
    private String eventType;

    /**
     * 事件携带的数据负载
     * 取值范围：任意序列化对象
     * 单位：对象
     * 是否可空：是
     */
    private Object payload;

    /**
     * 事件发生时间戳
     * 取值范围：Unix 时间戳
     * 单位：毫秒
     * 是否可空：否
     */
    private Long timestamp;

    /**
     * 关联的会话 ID
     * 取值范围：UUID 字符串
     * 单位：无
     * 是否可空：否
     */
    private String conversationId;

    public AiEventResponse() {
    }

    public AiEventResponse(String eventType, Object payload, Long timestamp, String conversationId) {
        this.eventType = eventType;
        this.payload = payload;
        this.timestamp = timestamp;
        this.conversationId = conversationId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    @Override
    public String toString() {
        return "AiEventResponse{" +
                "eventType='" + eventType + '\'' +
                ", payload=" + payload +
                ", timestamp=" + timestamp +
                ", conversationId='" + conversationId + '\'' +
                '}';
    }
}
