package course.QAssistant.repository;

import course.QAssistant.pojo.vo.request.SaveQuizPaperVO;
import course.QAssistant.pojo.vo.response.R;

public interface QuizPaperService {
    R saveQuizPaper(SaveQuizPaperVO vo, String token, String loginType);

    R listQuizPapers(String token, String loginType);

    R getQuizPaperDetail(String paperId, String token, String loginType);

    R deleteQuizPaper(String paperId, String token, String loginType);
}
