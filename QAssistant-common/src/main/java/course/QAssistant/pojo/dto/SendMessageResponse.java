package course.QAssistant.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 发送消息后的响应体
 * 包含本次新增消息的详情 + 所属会话的更新状态
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageResponse {

    // ==================== 消息信息 ====================

    /**
     * 新增消息的 ID（ObjectId）
     */
    private String messageId;

    /**
     * 消息角色：USER / AI / SYSTEM
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息创建时间
     */
    private Date createdAt;

    // ==================== 所属会话信息 ====================

    /**
     * 所属会话 ID
     */
    private String sessionId;

    /**
     * 会话标题
     */
    private String sessionTitle;

    /**
     * 会话最后更新时间（追加消息后同步刷新）
     */
    private Date sessionUpdatedAt;

    /**
     * 当前会话的消息总条数
     */
    private Integer messageCount;
}