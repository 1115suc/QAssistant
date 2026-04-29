package course.QAssistant.repository;

import course.QAssistant.pojo.po.QuizPaper;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizPaperRepository extends MongoRepository<QuizPaper, String> {

    /**
     * 按用户查所有题卷，按创建时间倒序
     */
    List<QuizPaper> findByUserUidOrderByCreatedAtDesc(String userUid);

    /**
     * 按用户 + id 查单篇（防止越权访问他人题卷）
     */
    Optional<QuizPaper> findByIdAndUserUid(String id, String userUid);

    /**
     * 删除时同样带 userUid 校验
     */
    void deleteByIdAndUserUid(String id, String userUid);
}