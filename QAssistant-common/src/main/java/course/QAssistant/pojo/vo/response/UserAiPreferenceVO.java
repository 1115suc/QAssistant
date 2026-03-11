package course.QAssistant.pojo.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Schema(description = "用户AI个性化配置返回对象")
public class UserAiPreferenceVO {

    @Schema(description = "配置ID")
    private Long id;

    @Schema(description = "关联的AI模型ID")
    private Long aiModelId;

    @Schema(description = "温度参数")
    private BigDecimal temperature;

    @Schema(description = "核采样参数")
    private BigDecimal topP;

    @Schema(description = "最大生成Token数")
    private Integer maxTokens;

    @Schema(description = "系统提示词")
    private String systemPrompt;

    @Schema(description = "创建时间")
    private Date createdAt;

    @Schema(description = "更新时间")
    private Date updatedAt;
}
