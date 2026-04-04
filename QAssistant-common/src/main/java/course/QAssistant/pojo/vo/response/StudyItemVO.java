package course.QAssistant.pojo.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "学习项VO")
public class StudyItemVO {
    @Schema(description = "学习标签")
    private String category;
    @Schema(description = "开始时间")
    private LocalDateTime startTime;
    @Schema(description = "结束时间")
    private LocalDateTime endTime;
    @Schema(description = "学习时长")
    private Integer studyMinutes;
}
