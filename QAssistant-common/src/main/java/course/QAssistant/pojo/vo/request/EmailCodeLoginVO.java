package course.QAssistant.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "邮箱验证码登录值对象类")
@Data
public class EmailCodeLoginVO {
    // 用户邮箱地址
    @NotBlank(message = "邮箱地址不能为空")
    @Schema(description = "用户邮箱地址")
    private String email;
    
    // 邮箱验证码
    @NotBlank(message = "邮箱验证码不能为空")
    @Schema(description = "邮箱验证码")
    private String emailCode;

    // 登录位置来源
    @NotNull(message = "登录方式不能为空")
    @Min(value = 1, message = "登录方式错误")
    @Max(value = 3, message = "登录方式错误")
    @Schema(description = "登录方式(1.Web 2.Android 3.ios)")
    private Integer loginWhere;
}