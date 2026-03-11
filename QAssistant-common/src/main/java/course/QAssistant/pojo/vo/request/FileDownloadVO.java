package course.QAssistant.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "文件下载请求参数")
public class FileDownloadVO {

    @NotNull(message = "id不能为空")
    @Schema(description = "minioFile记录id")
    private Long id;
}

