package course.QAssistant.pojo.vo.request;

import course.QAssistant.pojo.quiz.QuizQuestion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "保存题卷请求对象")
public class SaveQuizPaperVO {

    @Schema(description = "前端传来的临时id（如果有），服务端忽略，由MongoDB自动生成")
    private String id;

    @NotBlank(message = "题卷标题不能为空")
    @Schema(description = "题卷标题", example = "Java基础测试", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "题目主题不能为空")
    @Schema(description = "题目主题/科目", example = "Java编程基础", requiredMode = Schema.RequiredMode.REQUIRED)
    private String topic;

    @NotEmpty(message = "题目列表不能为空")
    @Schema(description = "题目列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<QuizQuestion> questions;

    @NotNull(message = "总分不能为空")
    @Schema(description = "题卷总分", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer totalScore;

    @Schema(description = "前端传来的创建时间字符串，服务端以服务器时间为准", example = "2024-01-01 12:00:00")
    private String createdAt;
}
