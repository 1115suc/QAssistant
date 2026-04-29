package course.QAssistant.pojo.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "题卷列表项响应对象")
public class QuizPaperListItemVO {

    @Schema(description = "题卷ID", example = "507f1f77bcf86cd799439011")
    private String id;

    @Schema(description = "题卷标题", example = "Java基础测试")
    private String title;

    @Schema(description = "题目主题/科目", example = "Java编程基础")
    private String topic;

    @Schema(description = "题卷总分", example = "100")
    private Integer totalScore;

    @Schema(description = "题目数量", example = "10")
    private Integer questionCount;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
