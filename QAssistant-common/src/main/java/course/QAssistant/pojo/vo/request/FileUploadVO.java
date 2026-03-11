package course.QAssistant.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "文件上传请求参数")
public class FileUploadVO {

    @NotNull(message = "file不能为空")
    @Schema(description = "上传文件")
    private MultipartFile file;

    @Schema(description = "文件类型(如:PPT、DOC、MarkDown、md等)")
    private String fileType;
}

