package course.QAssistant.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "生成题目请求VO")
public class GenerateQuestionsRequestVO {

    @NotBlank(message = "主题不能为空")
    @Schema(description = "题目主题", requiredMode = Schema.RequiredMode.REQUIRED, example = "Python 列表与字典")
    private String topic;

    @Min(value = 0, message = "选择题数量不能小于0")
    @Max(value = 20, message = "选择题数量不能超过20")
    @Schema(description = "选择题数量（可选，默认0，最大20）", example = "5")
    private Integer choiceCount = 0;

    @Min(value = 0, message = "填空题数量不能小于0")
    @Max(value = 20, message = "填空题数量不能超过20")
    @Schema(description = "填空题数量（可选，默认0，最大20）", example = "3")
    private Integer fillCount = 0;

    @Min(value = 0, message = "问答题数量不能小于0")
    @Max(value = 10, message = "问答题数量不能超过10")
    @Schema(description = "问答题数量（可选，默认0，最大10）", example = "2")
    private Integer qaCount = 0;

    @NotBlank(message = "难度不能为空")
    @Schema(description = "题目难度 (easy/medium/hard)", requiredMode = Schema.RequiredMode.REQUIRED, example = "medium")
    private String difficulty;

    @Schema(description = "使用的自定义AI模型ID，为空则使用系统默认模型", example = "1")
    private Long aiModelId;

    @Schema(description = "关联的文件ID列表（可选，用于RAG或其他场景）", example = "[10, 11]")
    private List<Long> fileIds;
}
