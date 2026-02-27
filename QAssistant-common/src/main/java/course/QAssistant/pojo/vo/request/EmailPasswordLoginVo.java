package course.QAssistant.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "邮箱密码登录值对象类")
@Data
public class EmailPasswordLoginVo {
    // 用户邮箱地址
    @NotBlank(message = "邮箱地址不能为空")
    @Schema(description = "用户邮箱地址")
    private String email;
    
    // 用户密码
    @NotBlank(message = "密码不能为空")
    @Schema(description = "用户密码")
    private String password;

    // 图形验证码
    @NotBlank(message = "图形验证码不能为空")
    @Schema(description = "图形验证码")
    private String checkCode;

    // 图形验证码的Id值
    @NotBlank(message = "验证码ID不能为空")
    @Schema(description = "图形验证码的Id值")
    private String sessionId;
}