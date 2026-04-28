package course.QAssistant.service;

import course.QAssistant.pojo.vo.request.GenerateQuestionsRequestVO;
import reactor.core.publisher.Flux;

public interface QuizService {
    String generateQuiz(GenerateQuestionsRequestVO requestVO, String token, String loginType);
}
