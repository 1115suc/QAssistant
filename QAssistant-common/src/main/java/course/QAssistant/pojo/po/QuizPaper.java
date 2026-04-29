package course.QAssistant.pojo.po;

import course.QAssistant.pojo.quiz.QuizQuestion;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "Quiz_paper")
@CompoundIndexes({
        @CompoundIndex(name = "idx_uid_created", def = "{'userUid': 1, 'createdAt': -1}")
})
public class QuizPaper {

    @Id
    private String id;

    /** 归属用户 UID，所有查询都带此条件做数据隔离 */
    private String userUid;

    private String title;
    private String topic;

    private List<QuizQuestion> questions;

    private Integer totalScore;

    /** 冗余字段：题目总数，列表页直接返回，不用再 count questions */
    private Integer questionCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}