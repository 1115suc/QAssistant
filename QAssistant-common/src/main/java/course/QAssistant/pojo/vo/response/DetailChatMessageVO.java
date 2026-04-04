package course.QAssistant.pojo.vo.response;

import course.QAssistant.pojo.po.ChatMessageDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Schema(description = "聊天详情消息")
public class DetailChatMessageVO {
    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "关联的自定义模型ID，系统默认模型为null")
    private Long aiModelId;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "最近更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "消息列表")
    private List<ChatMessageDocument> messages;
}
