package course.QAssistant.controller;

import course.QAssistant.annotation.VerificationInterceptor;
import course.QAssistant.pojo.vo.request.UpdateUserInfoVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.pojo.vo.response.UserInfoVO;
import course.QAssistant.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户信息管理", description = "用户信息查询、修改等接口")
public class UserInfoController {

    private final SysUserService sysUserService;

    @Operation(
            summary = "查询当前用户信息",
            description = "获取当前登录用户的详细信息，包括基本信息、个人资料等。",
            method = "GET"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户 Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式 (1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @VerificationInterceptor(checkLogin = true)
    @GetMapping("/info")
    public R<UserInfoVO> getUserInfo(@NotBlank(message = "Authorization 不能为空") @RequestHeader("Authorization") String token,
                                     @NotBlank(message = "LoginType 不能为空") @RequestHeader("LoginType") String loginType) {
        return sysUserService.getUserInfo(token, loginType);
    }

    @Operation(
            summary = "更新当前用户信息",
            description = "更新当前登录用户的个人信息，支持部分字段更新。",
            method = "PUT"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户 Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式 (1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @VerificationInterceptor(checkLogin = true)
    @PutMapping("/info")
    public R updateUserInfo(@NotBlank(message = "Authorization 不能为空") @RequestHeader("Authorization") String token,
                            @NotBlank(message = "LoginType 不能为空") @RequestHeader("LoginType") String loginType,
                            @ModelAttribute UpdateUserInfoVO updateUserInfoVO) {
        return sysUserService.updateUserInfo(token, loginType, updateUserInfoVO);
    }

}
