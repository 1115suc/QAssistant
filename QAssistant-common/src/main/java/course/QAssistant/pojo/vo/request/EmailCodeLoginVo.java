package course.QAssistant.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "邮箱验证码登录值对象类")
@Data
public class EmailCodeLoginVo {
    // 用户邮箱地址
    @NotBlank(message = "邮箱地址不能为空")
    @Schema(description = "用户邮箱地址")
    private String email;
    
    // 邮箱验证码
    @NotBlank(message = "邮箱验证码不能为空")
    @Schema(description = "邮箱验证码")
    private String emailCode;
}