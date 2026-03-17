package course.QAssistant.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "获取文件URL请求参数")
public class FileUrlVO {

    @NotNull(message = "id不能为空")
    @Schema(description = "minioFile记录id")
    private Long id;

    @NotBlank(message = "urlType不能为空")
    @Schema(description = "URL类型: preview=临时签名URL, public=永久公开URL")
    private String urlType;
}
