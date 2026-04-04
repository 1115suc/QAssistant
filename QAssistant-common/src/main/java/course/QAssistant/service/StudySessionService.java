package course.QAssistant.service;

import course.QAssistant.pojo.po.StudySession;
import com.baomidou.mybatisplus.extension.service.IService;
import course.QAssistant.pojo.vo.request.EndSessionVO;
import course.QAssistant.pojo.vo.request.HeartbeatVO;
import course.QAssistant.pojo.vo.request.StartSessionVO;
import course.QAssistant.pojo.vo.response.HeartbeatResultVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.pojo.vo.response.SessionItemVO;
import course.QAssistant.pojo.vo.response.TodayOverviewVO;

import java.time.LocalDate;

public interface StudySessionService extends IService<StudySession> {

    R<Long> startSession(StartSessionVO vo, String token, String loginType);

    R endSession(EndSessionVO vo, String token, String loginType);

    R<TodayOverviewVO> getTodayOverview(String token, String loginType);

    R<SessionItemVO> getActiveSession(String token, String loginType);

    void autoCloseSession(StudySession session);

    R<HeartbeatResultVO> heartbeat(HeartbeatVO vo, String token, String loginType);

    R<TodayOverviewVO> getDailyOverview(String token, String loginType, LocalDate date);
}
