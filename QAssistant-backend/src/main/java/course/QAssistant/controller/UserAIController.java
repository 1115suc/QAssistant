package course.QAssistant.controller;

import course.QAssistant.annotation.PreventRepeatSubmit;
import course.QAssistant.annotation.VerificationInterceptor;
import course.QAssistant.pojo.vo.request.UserAiModelCreateVO;
import course.QAssistant.pojo.vo.request.UserAiModelUpdateVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.pojo.vo.response.UserAiModelVO;
import course.QAssistant.service.UserAiService;
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

import java.util.List;

@Validated
@RestController
@RequestMapping("/userAI")
@RequiredArgsConstructor
@Tag(name = "用户AI模型管理", description = "用户自定义AI模型的增删改查接口")
public class UserAIController {

    private final UserAiService userAiService;

    @Operation(
            summary = "创建用户自定义AI模型",
            description = "在当前登录用户下创建一个自定义AI模型，模型名称在同一用户下不可重复。",
            method = "POST"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @VerificationInterceptor(checkLogin = true)
    @PreventRepeatSubmit
    @PostMapping("/model")
    public R<UserAiModelVO> createModel(@NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
                                        @NotBlank(message = "LoginType不能为空") @RequestHeader("LoginType") String loginType,
                                        @Valid @RequestBody UserAiModelCreateVO createVO) {
        return userAiService.createUserAiModel(token, loginType, createVO);
    }

    @Operation(
            summary = "更新用户自定义AI模型",
            description = "更新当前登录用户下已有的自定义AI模型信息。",
            method = "PUT"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @VerificationInterceptor(checkLogin = true)
    @PreventRepeatSubmit
    @PutMapping("/model")
    public R<UserAiModelVO> updateModel(@NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
                                        @NotBlank(message = "LoginType不能为空") @RequestHeader("LoginType") String loginType,
                                        @Valid @RequestBody UserAiModelUpdateVO updateVO) {
        return userAiService.updateUserAiModel(token, loginType, updateVO);
    }

    @Operation(
            summary = "删除用户自定义AI模型",
            description = "根据ID删除当前登录用户下的某个自定义AI模型。",
            method = "DELETE"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "id", description = "模型ID", required = true, in = ParameterIn.PATH)
    })
    @VerificationInterceptor(checkLogin = true)
    @PreventRepeatSubmit
    @DeleteMapping("/model/{id}")
    public R deleteModel(@NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
                         @NotBlank(message = "LoginType不能为空") @RequestHeader("LoginType") String loginType,
                         @PathVariable("id") Long id) {
        return userAiService.deleteUserAiModel(token, loginType, id);
    }

    @Operation(
            summary = "查询当前用户的AI模型列表",
            description = "查询当前登录用户下所有自定义AI模型。",
            method = "GET"
    )
    @Parameters({
            @Parameter(name = "Authorization", description = "用户Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式(1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @VerificationInterceptor(checkLogin = true)
    @GetMapping("/model/list")
    public R<List<UserAiModelVO>> listModels(@NotBlank(message = "Authorization不能为空") @RequestHeader("Authorization") String token,
                                             @NotBlank(message = "LoginType不能为空") @RequestHeader("LoginType") String loginType) {
        return userAiService.listUserAiModels(token, loginType);
    }
}
