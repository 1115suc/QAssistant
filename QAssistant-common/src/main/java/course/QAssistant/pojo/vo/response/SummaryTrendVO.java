package course.QAssistant.pojo.vo.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
@Schema(description = "分类趋势VO（堆叠柱图）")
public class SummaryTrendVO {
    @Schema(description = "日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate studyDate;
    @Schema(description = "总学习时长(分钟)")
    private Integer totalStudyMinutes;
    @Schema(description = "各标签时长 Map")
    private Map<String, Integer> categoryMinutes;
}