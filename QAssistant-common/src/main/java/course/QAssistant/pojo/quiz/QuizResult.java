package course.QAssistant.pojo.quiz;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
 
import java.util.List;
 
/**
 * AI 生成题目集合响应
 */
@Data
@Schema(description = "AI 生成题目集合")
public class QuizResult {
 
    @Schema(description = "题目集标题", example = "Python 基础练习")
    @JsonIgnoreProperties(ignoreUnknown = true)
    private String title;
 
    @Schema(description = "题目主题", example = "Python 列表与字典")
    @JsonIgnoreProperties(ignoreUnknown = true)
    private String topic;
 
    @Schema(description = "题目列表")
    @JsonIgnoreProperties(ignoreUnknown = true)
    private List<QuizQuestion> questions;
}