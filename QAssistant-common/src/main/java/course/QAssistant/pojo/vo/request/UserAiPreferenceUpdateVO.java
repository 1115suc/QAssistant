package course.QAssistant.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "用户AI个性化配置更新对象")
public class UserAiPreferenceUpdateVO {

    @Schema(description = "关联的AI模型ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "模型ID不能为空")
    private Long aiModelId;

    @Schema(description = "温度参数 (0.0 - 2.0)")
    @DecimalMin(value = "0.0", message = "温度参数最小为0.0")
    @DecimalMax(value = "2.0", message = "温度参数最大为2.0")
    private BigDecimal temperature;

    @Schema(description = "核采样参数 (0.0 - 1.0)")
    @DecimalMin(value = "0.0", message = "核采样参数最小为0.0")
    @DecimalMax(value = "1.0", message = "核采样参数最大为1.0")
    private BigDecimal topP;

    @Schema(description = "最大生成Token数")
    private Integer maxTokens;

    @Schema(description = "系统提示词")
    private String systemPrompt;
}
