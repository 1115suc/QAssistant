import cn.hutool.json.JSONUtil;
import course.QAssistant.QAssistantBackendApplication;
import course.QAssistant.pojo.vo.request.GenerateQuestionsRequestVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.repository.QuizPaperService;
import course.QAssistant.service.QuizService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootTest(classes = QAssistantBackendApplication.class)
public class AIChatTest {
    @Resource
    private QuizService quizService;
    @Resource
    private QuizPaperService quizPaperService;

    @Test
    public void testGenerateQuiz() {
        GenerateQuestionsRequestVO generateQuestionsRequestVO = new GenerateQuestionsRequestVO();
        generateQuestionsRequestVO.setTopic("笔试题目");
        generateQuestionsRequestVO.setChoiceCount(5);
        generateQuestionsRequestVO.setFillCount(5);
        generateQuestionsRequestVO.setQaCount(1);
        generateQuestionsRequestVO.setDifficulty("medium");
        List<Long> fileIds = new ArrayList<>(); // 2039610612220432384l
        fileIds.add(2039610612220432384L);
        generateQuestionsRequestVO.setFileIds(fileIds);

        String test = quizService.generateQuiz(generateQuestionsRequestVO, "d92feb162d5559c492cf583e4a773f64", "1");
        System.out.println(JSONUtil.toJsonPrettyStr(test));
        System.out.println("生成题目成功");
    }


}