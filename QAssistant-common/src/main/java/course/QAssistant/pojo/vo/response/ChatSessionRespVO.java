package course.QAssistant.pojo.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "聊天会话响应VO")
public class ChatSessionRespVO {

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "关联的自定义模型ID，系统默认模型为null")
    private Long aiModelId;

    @Schema(description = "创建时间")
    private Date createdAt;

    @Schema(description = "最近更新时间")
    private Date updatedAt;
}
