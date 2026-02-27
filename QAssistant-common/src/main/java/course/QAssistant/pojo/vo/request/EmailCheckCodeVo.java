package course.QAssistant.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "邮箱验证码请求类")
public class EmailCheckCodeVo {
    @NotBlank(message = "邮箱不能为空")
    @Schema(description = "邮箱")
    private String email;
    @NotBlank(message = "验证码不能为空")
    @Schema(description = "图形验证码")
    private String CheckCode;
    @NotBlank(message = "sessionId不能为空")
    @Schema(description = "图形验证码Id")
    private String sessionId;
}
