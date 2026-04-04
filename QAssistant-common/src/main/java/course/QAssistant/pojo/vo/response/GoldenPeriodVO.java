package course.QAssistant.pojo.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "黄金学习时段响应VO")
public class GoldenPeriodVO {
    @Schema(description = "凌晨 00-06 累计学习分钟数")
    private Integer dawnMinutes;      // 00-06

    @Schema(description = "早晨 06-12 累计学习分钟数")
    private Integer morningMinutes;   // 06-12

    @Schema(description = "下午 12-18 累计学习分钟数")
    private Integer afternoonMinutes; // 12-18

    @Schema(description = "晚上 18-24 累计学习分钟数")
    private Integer eveningMinutes;   // 18-24
}