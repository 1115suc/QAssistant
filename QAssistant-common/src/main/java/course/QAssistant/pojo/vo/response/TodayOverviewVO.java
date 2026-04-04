package course.QAssistant.pojo.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Schema(description = "今日概览VO")
public class TodayOverviewVO {
    @Schema(description = "今日总学习时长(分钟)")
    private Integer totalStudyMinutes;
    @Schema(description = "今日总休息时长(分钟)")
    private Integer totalRestMinutes;
    @Schema(description = "今日学习次数")
    private Integer sessionCount;
    @Schema(description = "各标签学习时长 eg.{\"语文\":90,\"数学\":60}")
    private Map<String, Integer> categoryMinutes;
    @Schema(description = "今日会话列表")
    private List<SessionItemVO> sessions;
}