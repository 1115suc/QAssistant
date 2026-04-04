package course.QAssistant.pojo.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "会话详情VO")
public class SessionItemVO {
    @Schema(description = "会话ID")
    private Long id;
    @Schema(description = "学习标签")
    private String category;
    @Schema(description = "开始时间")
    private LocalDateTime startTime;
    @Schema(description = "结束时间，进行中为null")
    private LocalDateTime endTime;
    @Schema(description = "专注时长(分钟)，进行中为null")
    private Integer focusMinutes;
    @Schema(description = "休息时长(分钟)")
    private Integer restMinutes;
    @Schema(description = "状态: 1=进行中 2=正常结束 3=系统关闭")
    private Integer status;
}