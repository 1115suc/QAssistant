package course.QAssistant.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "创建聊天会话请求VO")
public class CreateSessionRequestVO {
    @NotBlank(message = "Session title cannot be empty")
    @Schema(description = "会话标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "可选，使用的自定义AI模型ID，为空则使用系统默认模型")
    private Long aiModelId;
}
