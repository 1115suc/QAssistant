package course.QAssistant.controller;

import course.QAssistant.pojo.vo.request.*;
import course.QAssistant.pojo.vo.response.CheckCodeVo;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.properties.EmailConfigProperties;
import course.QAssistant.service.EmailCodeService;
import course.QAssistant.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户相关接口，包括验证码获取、用户注册、登录等功能")
public class UserController {

    private final SysUserService sysUserService;
    private final EmailCodeService emailCodeService;

    @Operation(
            summary = "获取图像验证码",
            description = "生成并返回图形验证码，用于用户登录或其他需要验证的场景。验证码会存储在Redis中，有效期为5分钟。",
            method = "GET"
    )
    @GetMapping("/getCaptcha")
    public R<CheckCodeVo> getCaptcha() {
        return sysUserService.getCaptcha();
    }

    @Operation(
            summary = "发送邮箱验证码",
            description = "向指定邮箱发送验证码，用于用户注册或找回密码等场景。验证码会存储在Redis中，有效期为5分钟。",
            method = "POST"
    )
    @Parameters({
            @Parameter(name = "emailCheckCodeVo", description = "邮箱地址", required = true)
    })
    @PostMapping("/sendEmailCode")
    public R sendEmailCode(@RequestBody EmailCheckCodeVo  emailCheckCodeVo) {
        return emailCodeService.sendEmailCode(emailCheckCodeVo);
    }

    @Operation(
            summary = "用户注册",
            description = "用户注册接口，用户需要提供用户名、密码、手机号码等信息进行注册。注册成功后，用户将获得一个唯一的用户ID。",
            method = "POST"
    )
    @Parameters({
            @Parameter(name = "emailLoginVo", description = "用户注册信息", required = true)
    })
    @PostMapping()
    public R register(@RequestBody EmailLoginVo emailLoginVo) {
        return sysUserService.register(emailLoginVo);
    }

    @Operation(
            summary = "邮箱密码登录",
            description = "使用邮箱和密码进行登录，需要提供图形验证码。",
            method = "POST"
    )
    @Parameters({
            @Parameter(
                    name = "emailPasswordLoginVo",
                    description = "邮箱密码登录信息",
                    required = true,
                    example = "{\"email\":\"user@example.com\",\"password\":\"123456\",\"checkCode\":\"12345\",\"sessionId\":\"session123\"}"
            )
    })
    @PostMapping("/email-password-login")
    public R emailPasswordLogin(@RequestBody EmailPasswordLoginVo emailPasswordLoginVo) {
        return null;
    }

    @Operation(
            summary = "邮箱验证码登录",
            description = "使用邮箱和验证码进行登录。",
            method = "POST"
    )
    @Parameters({
            @Parameter(
                    name = "emailCodeLoginVo",
                    description = "邮箱验证码登录信息",
                    required = true,
                    example = "{\"email\":\"user@example.com\",\"emailCode\":\"123456\"}"
            )
    })
    @PostMapping("/email-code-login")
    public R emailCodeLogin(@RequestBody EmailCodeLoginVo emailCodeLoginVo) {
        return null;
    }

    @Operation(
            summary = "退出登录",
            description = "用户退出登录，使当前token失效。",
            method = "POST"
    )
    @Parameters({
            @Parameter(
                    name = "Authorization",
                    description = "用户登录token",
                    required = true,
                    in = ParameterIn.HEADER,
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            )
    })
    @PostMapping("/logout")
    public R logout(HttpServletRequest request) {
        return null;
    }

    @Operation(
            summary = "重置密码",
            description = "通过邮箱验证码重置用户密码。",
            method = "POST"
    )
    @Parameters({
            @Parameter(
                    name = "resetVo",
                    description = "重置密码信息",
                    required = true,
                    example = "{\"email\":\"user@example.com\",\"newPassword\":\"newpass123\",\"emailCode\":\"123456\"}"
            )
    })
    @PostMapping("/reset-password")
    public R resetPassword(@RequestBody ResetPasswordVo resetVo,
                           HttpServletRequest request) {
        return null;
    }

    @Operation(
            summary = "上传头像",
            description = "上传用户头像图片。",
            method = "POST"
    )
    @Parameters({
            @Parameter(
                    name = "avatarFile",
                    description = "头像文件",
                    required = true,
                    example = "头像图片文件"
            )
    })
    @PostMapping("/upload-avatar")
    public R uploadAvatar(@ModelAttribute AvatarUploadVo avatarVo,
                          HttpServletRequest request) {
        return null;
    }

}
