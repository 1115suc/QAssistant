package course.QAssistant.pojo.quiz;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
 
/**
 * 题目实体（统一结构，choice/fill/qa 三种类型的超集）
 * <ul>
 *   <li>choice（选择题）：包含 options 字段，answer 为选项 key（如 "B"）</li>
 *   <li>fill  （填空题）：options 为 null，answer 为文字答案</li>
 *   <li>qa    （问答题）：options 为 null，answer 为文字答案</li>
 * </ul>
 */
@Data
@Schema(description = "题目")
public class QuizQuestion {
 
    @Schema(description = "题目 ID，如 q1 / q2", example = "q1")
    private String id;
 
    @Schema(description = "题目类型：choice / fill / qa", example = "choice")
    private String type;
 
    @Schema(description = "题目内容", example = "以下哪个方法可以获取字典的所有键？")
    private String content;

    @Schema(description = "选项列表（仅 choice 类型）")
    @JsonProperty("options")
    private List<QuestionOption> options;
 
    @Schema(description = "答案（选择题填写选项 key，其他题型填写文字答案）", example = "B")
    private String answer;
 
    @Schema(description = "解析说明（可选）", example = "dict.keys() 返回包含字典所有键的视图对象。")
    private String explanation;
 
    @Schema(description = "分值（选择题 5 分，填空题 5 分，问答题 10-15 分）", example = "5")
    private Integer score;
}
 