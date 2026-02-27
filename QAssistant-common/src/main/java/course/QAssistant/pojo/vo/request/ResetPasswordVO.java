package course.QAssistant.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "重置密码值对象类")
@Data
public class ResetPasswordVO {
    // 用户邮箱地址
    @NotBlank(message = "邮箱地址不能为空")
    @Schema(description = "用户邮箱地址")
    private String email;
    
    // 新密码
    @NotBlank(message = "新密码不能为空")
    @Schema(description = "新密码")
    private String newPassword;
    
    // 邮箱验证码
    @NotBlank(message = "邮箱验证码不能为空")
    @Schema(description = "邮箱验证码")
    private String emailCode;
}