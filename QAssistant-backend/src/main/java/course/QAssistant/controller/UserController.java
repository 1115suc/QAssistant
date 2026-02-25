package course.QAssistant.controller;

import course.QAssistant.pojo.vo.request.EmailLoginVo;
import course.QAssistant.pojo.vo.response.CheckCodeVo;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    // 获取图像验证码
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
            summary = "用户注册",
            description = "用户注册接口，用户需要提供用户名、密码、手机号码等信息进行注册。注册成功后，用户将获得一个唯一的用户ID。",
            method = "POST"
    )
    @Parameters({
            @Parameter(name = "emailLoginVo", description = "用户注册信息", required = true)
    })
    @PostMapping()
    public R<String> register(@RequestBody EmailLoginVo emailLoginVo) {
        return sysUserService.register(emailLoginVo);
    }
}
