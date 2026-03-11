package course.QAssistant.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "创建用户自定义AI模型请求参数")
public class UserAiModelCreateVO {

    @NotBlank(message = "模型名称不能为空")
    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "模型调用URL")
    private String baseUrl;

    @Schema(description = "API Key")
    private String apiKey;

    @Schema(description = "模型提供商 (OpenAI, DeepSeek等)")
    private String provider;

    @Schema(description = "模型描述")
    private String description;
}

