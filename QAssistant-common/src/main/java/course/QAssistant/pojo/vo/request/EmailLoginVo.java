package course.QAssistant.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "邮箱登录值对象类")
@Data
public class EmailLoginVo {
    // 用户邮箱地址
    @NotBlank(message = "邮箱地址不能为空")
    @Schema(description = "用户邮箱地址")
    private String email;
    // 用户密码
    @NotBlank(message = "密码不能为空")
    @Schema(description = "用户密码")
    private String password;
    // 用户昵称
    @NotBlank(message = "昵称不能为空")
    @Schema(description = "用户昵称")
    private String nickName;
    // 创建来源(1.web 2.android 3.ios)
    @NotNull(message = "创建来源不能为空")
    @Min(value = 1, message = "参数类型错误")
    @Max(value = 3, message = "参数类型错误")
    @Schema(description = "创建来源(1.web 2.android 3.ios)")
    private Integer createWhere;
    // 图形验证码
    @NotBlank(message = "邮箱验证码不能为空")
    @Schema(description = "邮箱验证码")
    private String EmailCode;
}
