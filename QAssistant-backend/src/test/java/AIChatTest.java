import cn.hutool.json.JSONUtil;
import course.QAssistant.QAssistantBackendApplication;
import course.QAssistant.pojo.vo.request.GenerateQuestionsRequestVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.repository.QuizPaperService;
import course.QAssistant.service.QuizService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;


@Slf4j
@SpringBootTest(classes = QAssistantBackendApplication.class)
public class AIChatTest {
    @Resource
    private QuizService quizService;
    @Resource
    private QuizPaperService quizPaperService;



}