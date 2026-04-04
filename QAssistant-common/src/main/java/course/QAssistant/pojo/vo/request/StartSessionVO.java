package course.QAssistant.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "开始学习会话请求VO")
public class StartSessionVO {
    @NotBlank(message = "学习标签不能为空")
    @Schema(description = "学习标签 eg.语文/数学", requiredMode = Schema.RequiredMode.REQUIRED)
    private String category;
}