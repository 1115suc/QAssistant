package course.QAssistant.controller;

import course.QAssistant.annotation.PreventRepeatSubmit;
import course.QAssistant.annotation.VerificationInterceptor;
import course.QAssistant.pojo.vo.request.UserAiPreferenceUpdateVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.pojo.vo.response.UserAiPreferenceVO;
import course.QAssistant.service.UserAiPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/modelConfig")
@RequiredArgsConstructor
@Tag(name = "用户AI个性化配置", description = "用户针对特定AI模型的个性化参数配置接口")
public class AIConfigController {

    private final UserAiPreferenceService userAiPreferenceService;

    @Operation(
            summary = "获取用户AI个性化配置",
            description = "根据AI模型ID获取当前用户的个性化配置。如果未配置，将返回默认值。",
            method = "GET"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "modelId", description = "AI模型ID", required = true, in = ParameterIn.PATH)
    })
    @VerificationInterceptor(checkLogin = true)
    @GetMapping("/{modelId}")
    public R<UserAiPreferenceVO> getPreference(@NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
                                               @NotBlank(message = "LoginType不能为空") @RequestHeader("LoginType") String loginType,
                                               @PathVariable("modelId") Long modelId) {
        return userAiPreferenceService.getPreference(token, loginType, modelId);
    }

    @Operation(
            summary = "更新或创建用户AI个性化配置",
            description = "更新当前用户针对特定AI模型的个性化配置。如果配置不存在则创建。",
            method = "PUT"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @VerificationInterceptor(checkLogin = true)
    @PutMapping()
    public R<UserAiPreferenceVO> updatePreference(@NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
                                                  @NotBlank(message = "LoginType不能为空") @RequestHeader("LoginType") String loginType,
                                                  @Valid @RequestBody UserAiPreferenceUpdateVO updateVO) {
        return userAiPreferenceService.updatePreference(token, loginType, updateVO);
    }
}
