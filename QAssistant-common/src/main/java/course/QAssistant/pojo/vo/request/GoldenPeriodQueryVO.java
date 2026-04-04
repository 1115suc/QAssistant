package course.QAssistant.pojo.vo.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "黄金学习时段查询请求VO")
public class GoldenPeriodQueryVO {
    @Schema(description = "开始日期 yyyy-MM-dd，为空则查全部历史")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "结束日期 yyyy-MM-dd，为空则查全部历史")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}