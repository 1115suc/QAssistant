package course.QAssistant.pojo.vo.request;

import dev.langchain4j.service.MemoryId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "聊天请求VO")
public class ChatRequestVO {

    @NotNull(message = "Session ID cannot be null")
    @Schema(description = "会话ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sessionId;

    @NotBlank(message = "Message cannot be empty")
    @Schema(description = "用户发送的消息文本", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    @Schema(description = "可选，使用的自定义AI模型ID，为空则使用系统默认模型")
    private Long aiModelId;

    @Schema(description = "可选，开启RAG功能，默认为false")
    private Boolean ragEnabled = false;
}
