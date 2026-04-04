package course.QAssistant.controller;

import course.QAssistant.annotation.VerificationInterceptor;
import course.QAssistant.pojo.vo.request.EndSessionVO;
import course.QAssistant.pojo.vo.request.HeartbeatVO;
import course.QAssistant.pojo.vo.request.StartSessionVO;
import course.QAssistant.pojo.vo.response.HeartbeatResultVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.pojo.vo.response.SessionItemVO;
import course.QAssistant.pojo.vo.response.TodayOverviewVO;
import course.QAssistant.service.StudySessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "学习会话管理")
@RestController
@RequestMapping("/study/session")
@RequiredArgsConstructor
public class StudySessionController {

    private final StudySessionService studySessionService;

    @Operation(summary = "开始学习", description = "创建一条进行中的学习会话，返回会话 ID", method = "POST")
    @Parameters({
            @Parameter(name = "Authorization", description = "用户 Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式 (1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @VerificationInterceptor(checkLogin = true)
    @PostMapping("/start")
    public R<Long> startSession(
            @RequestBody @Validated StartSessionVO vo,
            @NotBlank(message = "Authorization 不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "LoginType 不能为空") @RequestHeader("LoginType") String loginType) {
        return studySessionService.startSession(vo, token, loginType);
    }

    @Operation(summary = "结束学习", description = "结束指定会话，写入结束时间和专注时长，并同步每日汇总", method = "PUT")
    @Parameters({
            @Parameter(name = "Authorization", description = "用户 Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式 (1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @VerificationInterceptor(checkLogin = true)
    @PutMapping("/end")
    public R endSession(
            @RequestBody @Validated EndSessionVO vo,
            @NotBlank(message = "Authorization 不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "LoginType 不能为空") @RequestHeader("LoginType") String loginType) {
        return studySessionService.endSession(vo, token, loginType);
    }

    @Operation(summary = "某日会话列表 + 概览", description = "获取指定日期的所有会话记录及统计概览", method = "GET")
    @Parameters({
            @Parameter(name = "Authorization", description = "用户 Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式 (1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "date", description = "查询日期 (yyyy-MM-dd)，不传则查询今天", required = false, in = ParameterIn.QUERY)
    })
    @VerificationInterceptor(checkLogin = true)
    @GetMapping("/daily")
    public R<TodayOverviewVO> getDailyOverview(
            @NotBlank(message = "Authorization 不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "LoginType 不能为空") @RequestHeader("LoginType") String loginType,
            @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return studySessionService.getDailyOverview(token, loginType, date);
    }

    @Operation(summary = "获取进行中的会话", description = "获取当前用户正在进行中 (status=1) 的会话，不存在返回 null", method = "GET")
    @Parameters({
            @Parameter(name = "Authorization", description = "用户 Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式 (1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @VerificationInterceptor(checkLogin = true)
    @GetMapping("/active")
    public R<SessionItemVO> getActiveSession(
            @NotBlank(message = "Authorization 不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "LoginType 不能为空") @RequestHeader("LoginType") String loginType) {
        return studySessionService.getActiveSession(token, loginType);
    }

    @Operation(summary = "学习心跳续期",
            description = "前端每15分钟调用一次，刷新会话活跃状态，超过30分钟未续期系统将自动关闭会话",
            method = "POST")
    @Parameters({
            @Parameter(name = "Authorization", description = "用户 Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式 (1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @VerificationInterceptor(checkLogin = true)
    @PostMapping("/heartbeat")
    public R<HeartbeatResultVO> heartbeat(@NotBlank(message = "Authorization 不能为空") @RequestHeader("Authorization") String token,
                                          @NotBlank(message = "LoginType 不能为空") @RequestHeader("LoginType") String loginType,
                                          @RequestBody @Validated HeartbeatVO vo) {
        return studySessionService.heartbeat(vo, token, loginType);
    }
}