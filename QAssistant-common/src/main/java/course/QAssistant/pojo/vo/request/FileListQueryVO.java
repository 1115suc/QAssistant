package course.QAssistant.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Schema(description = "查询用户文件列表请求参数")
public class FileListQueryVO {

    @Schema(description = "文件名模糊搜索（可选）")
    private String fileName;

    @Min(value = 1, message = "页码最小为1")
    @Schema(description = "页码（可选，默认1）")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    @Schema(description = "每页数量（可选，默认20，最大100）")
    private Integer pageSize = 20;
}
