package course.QAssistant.pojo.vo.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "热力打卡VO")
public class HeatmapItemVO {
    @Schema(description = "日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate studyDate;
    @Schema(description = "当日总学习时长(分钟)")
    private Integer totalStudyMinutes;
}