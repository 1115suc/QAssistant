package course.QAssistant.controller;

import course.QAssistant.pojo.vo.request.*;
import course.QAssistant.pojo.vo.response.CheckCodeVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.pojo.vo.response.UserLoginVO;
import course.QAssistant.service.EmailCodeService;
import course.QAssistant.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户相关接口，包括验证码获取、用户注册、登录等功能")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);
    private final SysUserService sysUserService;
    private final EmailCodeService emailCodeService;

    @Operation(
            summary = "获取图像验证码",
            description = "生成并返回图形验证码，用于用户登录或其他需要验证的场景。验证码会存储在Redis中，有效期为5分钟。",
            method = "GET"
    )
    @GetMapping()
    public R<CheckCodeVO> getCaptcha() {
        return sysUserService.getCaptcha();
    }

    @Operation(
            summary = "发送邮箱验证码",
            description = "向指定邮箱发送验证码，用于用户注册或找回密码等场景。验证码会存储在Redis中，有效期为5分钟。",
            method = "POST"
    )
    @PostMapping("/sendEmailCode")
    public R sendEmailCode(@RequestBody EmailCheckCodeVO emailCheckCodeVo) {
        return emailCodeService.sendEmailCode(emailCheckCodeVo);
    }

    @Operation(
            summary = "用户注册",
            description = "用户注册接口，用户需要提供用户名、密码、手机号码等信息进行注册。注册成功后，用户将获得一个唯一的用户ID。",
            method = "POST"
    )
    @PostMapping("/register")
    public R register(@RequestBody EmailLoginVO emailLoginVo) {
        return sysUserService.register(emailLoginVo);
    }

    @Operation(
            summary = "邮箱密码登录",
            description = "使用邮箱和密码进行登录，需要提供图形验证码。",
            method = "POST"
    )
    @PostMapping("/emailPasswordLogin")
    public R<UserLoginVO> emailPasswordLogin(@RequestBody EmailPasswordLoginVO emailPasswordLoginVo) {
        return sysUserService.emailPasswordLogin(emailPasswordLoginVo);
    }

    @Operation(
            summary = "邮箱验证码登录",
            description = "使用邮箱和验证码进行登录。",
            method = "POST"
    )
    @PostMapping("/emailCodeLogin")
    public R<UserLoginVO> emailCodeLogin(@RequestBody EmailCodeLoginVO emailCodeLoginVo) {
        log.info("邮箱验证码登录请求参数: {}", emailCodeLoginVo);
        return sysUserService.emailCodeLogin(emailCodeLoginVo);
    }

    @Operation(
            summary = "用户登出",
            description = "注销当前登录状态，清除服务端存储的Token信息。",
            method = "DELETE"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.QUERY)
    })
    @DeleteMapping()
    public R logout(@NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
                    @NotNull(message = "登录方式不能为空") Integer LoginType) {
        return sysUserService.logout(token, LoginType);
    }

    @Operation(
            summary = "重置密码",
            description = "登录状态下修改当前用户的登录密码。",
            method = "PUT"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.QUERY),
            @Parameter(name = "resetVo", description = "重置密码信息", required = true)
    })
    @PutMapping()
    public R resetPassword(@NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
                           @RequestBody ResetPasswordVO resetVo,
                           @NotNull(message = "登录方式不能为空") Integer LoginType) {
        return sysUserService.resetPassword(token, LoginType, resetVo);
    }
}
