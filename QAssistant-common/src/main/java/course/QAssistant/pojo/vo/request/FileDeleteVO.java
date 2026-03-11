package course.QAssistant.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "文件删除请求参数")
public class FileDeleteVO {

    @NotNull(message = "id不能为空")
    @Schema(description = "minioFile记录id")
    private Long id;
}

