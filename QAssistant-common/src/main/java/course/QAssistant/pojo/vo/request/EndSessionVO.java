package course.QAssistant.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "结束学习会话请求VO")
public class EndSessionVO {
    @NotNull(message = "会话ID不能为空")
    @Schema(description = "会话ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @NotNull(message = "结束状态不能为空")
    @Schema(description = "结束状态: 2=正常结束 3=系统关闭", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "本次会话后休息时长(分钟)，可为0")
    private Integer restMinutes = 0;
}
