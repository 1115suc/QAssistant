package course.QAssistant.mapper;

import course.QAssistant.pojo.po.StudyDailySummary;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;


public interface StudyDailySummaryMapper extends BaseMapper<StudyDailySummary> {
    /**
     * 按时间段统计累计学习分钟数
     * @param userId    用户ID
     * @param startDate 开始日期（可为null）
     * @param endDate   结束日期（可为null）
     * @return list，每条含 period(0/1/2/3) 和 totalMinutes
     */
    List<Map<String, Object>> selectGoldenPeriod(
            @Param("userId") String userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}




