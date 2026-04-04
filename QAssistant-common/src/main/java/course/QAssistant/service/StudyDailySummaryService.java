package course.QAssistant.service;

import course.QAssistant.pojo.dto.StudySessionEndDTO;
import course.QAssistant.pojo.po.StudyDailySummary;
import com.baomidou.mybatisplus.extension.service.IService;
import course.QAssistant.pojo.vo.request.DateRangeVO;
import course.QAssistant.pojo.vo.request.GoldenPeriodQueryVO;
import course.QAssistant.pojo.vo.response.GoldenPeriodVO;
import course.QAssistant.pojo.vo.response.HeatmapItemVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.pojo.vo.response.SummaryTrendVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface StudyDailySummaryService extends IService<StudyDailySummary> {

    R<List<SummaryTrendVO>> getTrend(DateRangeVO vo, String token, String loginType);

    R<List<HeatmapItemVO>> getHeatmap(DateRangeVO vo, String token, String loginType);

    // 内部方法：会话结束时同步汇总（由 StudySessionServiceImpl 调用）
    void syncSummaryOnSessionEnd(StudySessionEndDTO dto);

    R<GoldenPeriodVO> getGoldenPeriod(GoldenPeriodQueryVO vo, String token, String loginType);
}
