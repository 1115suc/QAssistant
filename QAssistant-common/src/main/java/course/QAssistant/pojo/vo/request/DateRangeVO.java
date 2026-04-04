package course.QAssistant.pojo.vo.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "日期范围查询VO")
public class DateRangeVO {
    @NotNull(message = "开始日期不能为空")
    @Schema(description = "开始日期 yyyy-MM-dd", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-M-d")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    @Schema(description = "结束日期 yyyy-MM-dd", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(pattern = "yyyy-M-d")
    private LocalDate endDate;
}