package course.QAssistant.repository.Impl;

import course.QAssistant.exception.QAWebException;
import course.QAssistant.handler.RedisComponent;
import course.QAssistant.pojo.dto.TokenUserDTO;
import course.QAssistant.pojo.po.QuizPaper;
import course.QAssistant.pojo.vo.request.SaveQuizPaperVO;
import course.QAssistant.pojo.vo.response.QuizPaperListItemVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.repository.QuizPaperRepository;
import course.QAssistant.repository.QuizPaperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizPaperServiceImpl implements QuizPaperService {

    private final QuizPaperRepository quizPaperRepository;
    private final RedisComponent redisComponent;

    // ================================================================
    // 保存题卷
    // ================================================================

    @Override
    public R saveQuizPaper(SaveQuizPaperVO vo, String token, String loginType) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);

        QuizPaper paper = new QuizPaper();
        // ✅ 不使用前端传来的 id，由 MongoDB 自动生成，防止伪造
        paper.setUserUid(tokenUserDTO.getUid());
        paper.setTitle(vo.getTitle());
        paper.setTopic(vo.getTopic());
        paper.setQuestions(vo.getQuestions());
        paper.setTotalScore(vo.getTotalScore());
        paper.setQuestionCount(
                vo.getQuestions() == null ? 0 : vo.getQuestions().size()
        );
        paper.setCreatedAt(LocalDateTime.now());
        paper.setUpdatedAt(LocalDateTime.now());

        QuizPaper saved = quizPaperRepository.save(paper);
        log.info("[QuizPaper] 保存成功 | id={} | uid={}", saved.getId(), tokenUserDTO.getUid());

        return R.ok(saved.getId(), "题卷保存成功");
    }

    // ================================================================
    // 获取题卷列表（只返回摘要，不含 questions）
    // ================================================================

    @Override
    public R listQuizPapers(String token, String loginType) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);

        List<QuizPaper> papers = quizPaperRepository
                .findByUserUidOrderByCreatedAtDesc(tokenUserDTO.getUid());

        List<QuizPaperListItemVO> result = papers.stream()
                .map(this::toListItemVO)
                .toList();

        return R.ok(result);
    }

    // ================================================================
    // 获取题卷详情（含完整 questions）
    // ================================================================

    @Override
    public R getQuizPaperDetail(String paperId, String token, String loginType) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);

        QuizPaper paper = quizPaperRepository
                .findByIdAndUserUid(paperId, tokenUserDTO.getUid())
                .orElseThrow(() -> new QAWebException("题卷不存在或无权访问"));

        return R.ok(paper);
    }

    // ================================================================
    // 删除题卷
    // ================================================================

    @Override
    public R deleteQuizPaper(String paperId, String token, String loginType) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);

        // 先查再删，确认存在且属于当前用户
        quizPaperRepository.findByIdAndUserUid(paperId, tokenUserDTO.getUid())
                .orElseThrow(() -> new QAWebException("题卷不存在或无权访问"));

        quizPaperRepository.deleteByIdAndUserUid(paperId, tokenUserDTO.getUid());
        log.info("[QuizPaper] 删除成功 | id={} | uid={}", paperId, tokenUserDTO.getUid());

        return R.ok("删除成功");
    }

    // ================================================================
    // 私有方法
    // ================================================================

    private QuizPaperListItemVO toListItemVO(QuizPaper paper) {
        QuizPaperListItemVO vo = new QuizPaperListItemVO();
        vo.setId(paper.getId());
        vo.setTitle(paper.getTitle());
        vo.setTopic(paper.getTopic());
        vo.setTotalScore(paper.getTotalScore());
        vo.setQuestionCount(paper.getQuestionCount());
        vo.setCreatedAt(paper.getCreatedAt());
        return vo;
    }
}