package course.QAssistant.pojo.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 选择题选项
 */
@Data
@Schema(description = "选择题选项")
public class QuestionOption {
 
    @Schema(description = "选项键（A/B/C/D）", example = "A")
    private String key;
 
    @Schema(description = "选项内容", example = "dict.keys()")
    private String text;
}