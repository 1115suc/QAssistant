package course.QAssistant.controller;

import course.QAssistant.annotation.VerificationInterceptor;
import course.QAssistant.pojo.vo.request.DateRangeVO;
import course.QAssistant.pojo.vo.request.GoldenPeriodQueryVO;
import course.QAssistant.pojo.vo.response.GoldenPeriodVO;
import course.QAssistant.pojo.vo.response.HeatmapItemVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.pojo.vo.response.SummaryTrendVO;
import course.QAssistant.service.StudyDailySummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "学习统计分析")
@RestController
@RequestMapping("/study/summary")
@RequiredArgsConstructor
public class StudySummaryController {

    private final StudyDailySummaryService studyDailySummaryService;

    @Operation(summary = "分类趋势（堆叠柱图）", description = "按日期范围返回每天各标签学习时长，用于堆叠柱状图", method = "POST")
    @Parameters({
            @Parameter(name = "Authorization", description = "用户 Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式 (1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @VerificationInterceptor(checkLogin = true)
    @PostMapping("/trend")
    public R<List<SummaryTrendVO>> getTrend(
            @RequestBody @Validated DateRangeVO vo,
            @NotBlank(message = "Authorization 不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "LoginType 不能为空") @RequestHeader("LoginType") String loginType) {
        return studyDailySummaryService.getTrend(vo, token, loginType);
    }

    @Operation(summary = "历史热力打卡", description = "按日期范围返回每日总学习时长，用于热力图渲染", method = "POST")
    @Parameters({
            @Parameter(name = "Authorization", description = "用户 Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式 (1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @VerificationInterceptor(checkLogin = true)
    @PostMapping("/heatmap")
    public R<List<HeatmapItemVO>> getHeatmap(
            @RequestBody @Validated DateRangeVO vo,
            @NotBlank(message = "Authorization 不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "LoginType 不能为空") @RequestHeader("LoginType") String loginType) {
        return studyDailySummaryService.getHeatmap(vo, token, loginType);
    }

    @Operation(summary = "黄金学习时段",
            description = "统计各时间段(凌晨/早晨/下午/晚上)的累计学习时长，用于雷达图渲染",
            method = "POST")
    @Parameters({
            @Parameter(name = "Authorization", description = "用户 Token", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "LoginType", description = "登录方式 (1.Web 2.Android 3.ios)", required = true, in = ParameterIn.HEADER)
    })
    @VerificationInterceptor(checkLogin = true)
    @PostMapping("/golden-period")
    public R<GoldenPeriodVO> getGoldenPeriod(
            @RequestBody GoldenPeriodQueryVO vo,
            @NotBlank(message = "Authorization 不能为空") @RequestHeader("Authorization") String token,
            @NotBlank(message = "LoginType 不能为空") @RequestHeader("LoginType") String loginType) {
        return studyDailySummaryService.getGoldenPeriod(vo, token, loginType);
    }
}