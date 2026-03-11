package course.QAssistant.pojo.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "用户自定义AI模型返回对象")
public class UserAiModelVO {

    @Schema(description = "AI模型ID")
    private Long id;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "模型调用URL")
    private String baseUrl;

    @Schema(description = "模型提供商")
    private String provider;

    @Schema(description = "模型描述")
    private String description;

    @Schema(description = "创建时间")
    private Date createdAt;

    @Schema(description = "更新时间")
    private Date updatedAt;
}

