package course.QAssistant.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "RAG文件上传请求VO")
public class RagUploadVO {

    @NotBlank(message = "会话 ID 不能为空")
    @Schema(description = "关联的会话ID")
    private String sessionId;

    @NotNull(message = "文件 ID 不能为空")
    @Schema(description = "文件 ID")
    private Long fileId;
}
