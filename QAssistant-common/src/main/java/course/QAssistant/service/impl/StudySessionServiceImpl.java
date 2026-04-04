package course.QAssistant.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import course.QAssistant.constant.CommonConstant;
import course.QAssistant.exception.QAWebException;
import course.QAssistant.handler.RedisComponent;
import course.QAssistant.pojo.dto.StudySessionEndDTO;
import course.QAssistant.pojo.dto.TokenUserDTO;
import course.QAssistant.pojo.po.StudyDailySummary;
import course.QAssistant.pojo.po.StudySession;
import course.QAssistant.pojo.vo.request.EndSessionVO;
import course.QAssistant.pojo.vo.request.HeartbeatVO;
import course.QAssistant.pojo.vo.request.StartSessionVO;
import course.QAssistant.pojo.vo.response.HeartbeatResultVO;
import course.QAssistant.pojo.vo.response.R;
import course.QAssistant.pojo.vo.response.SessionItemVO;
import course.QAssistant.pojo.vo.response.TodayOverviewVO;
import course.QAssistant.service.StudySessionService;
import course.QAssistant.mapper.StudySessionMapper;
import course.QAssistant.util.QAssistantDateUtil;
import course.QAssistant.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.RedisConnection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static course.QAssistant.constant.StudyRedisConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudySessionServiceImpl extends ServiceImpl<StudySessionMapper, StudySession>
        implements StudySessionService {

    private final RedisUtil redisUtil;
    private final RedisComponent redisComponent;
    private final StudyDailySummaryServiceImpl studyDailySummaryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Long> startSession(StartSessionVO vo, String token, String loginType) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);
        String uid = tokenUserDTO.getUid();

        // 校验：不允许同时存在两个进行中的会话
        LambdaQueryWrapper<StudySession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudySession::getUserId, uid)
                .eq(StudySession::getStatus, 1);
        long activeCount = count(wrapper);

        if (activeCount > 0) {
            throw new QAWebException("当前已有进行中的学习会话，请先结束后再开始");
        }

        LocalDateTime startTime = LocalDateTime.now();

        StudySession session = new StudySession();
        session.setUserId(uid);
        session.setCategory(vo.getCategory());
        session.setStartTime(startTime);
        session.setStatus(1);
        session.setCreatedAt(LocalDate.now());
        save(session);

        // 写入心跳 Key，TTL=30min，前端每15min续期
        refreshHeartbeat(uid, session.getId());

        return R.ok(session.getId());
    }

    // 刷新心跳 Key（startSession 和 heartbeat 接口复用）
    private void refreshHeartbeat(String userId, Long sessionId) {
        String key = StrUtil.format(HEARTBEAT_KEY, userId, sessionId);
        redisUtil.set(key, "1", HEARTBEAT_TTL_MINUTES);
    }


    @Override
    public R endSession(EndSessionVO vo, String token, String loginType) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);
        String userId = tokenUserDTO.getUid();

        StudySession session = lambdaQuery()
                .eq(StudySession::getId, vo.getId())
                .eq(StudySession::getUserId, userId)
                .one();
        if (session == null) {
            throw new QAWebException("会话不存在");
        }
        if (session.getStatus() != 1) {
            throw new QAWebException("会话已结束");
        }

        doCloseSession(session, vo.getStatus(), vo.getRestMinutes());

        // 手动关闭，删除心跳 Key
        deleteHeartbeat(userId, vo.getId());

        return R.ok("已完成本次学习，继续加油！");
    }

    private void deleteHeartbeat(String userId, Long sessionId) {
        redisUtil.del(StrUtil.format(HEARTBEAT_KEY, userId, sessionId));
    }

    private void doCloseSession(StudySession session, int status, int restMinutes) {
        LocalDateTime endTime = LocalDateTime.now();
        int focusMinutes = (int) Duration.between(session.getStartTime(), endTime).toMinutes();
        // 专注时长最少记1分钟
        focusMinutes = Math.max(focusMinutes, 1);

        session.setEndTime(endTime);
        session.setFocusMinutes(focusMinutes);
        session.setRestMinutes(restMinutes);
        session.setStatus(status);
        updateById(session);

        // 同步每日汇总表
        studyDailySummaryService.syncSummaryOnSessionEnd(
                StudySessionEndDTO.builder()
                        .userId(session.getUserId())
                        .startTime(session.getStartTime())
                        .focusMinutes(Convert.toInt(focusMinutes))
                        .restMinutes(restMinutes)
                        .category(session.getCategory())
                        .build()
        );
    }


    // 获取今日概览
    @Override
    public R<TodayOverviewVO> getTodayOverview(String token, String loginType) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);
        String userId = tokenUserDTO.getUid();
        LocalDate today = LocalDate.now();

        // 查今日所有会话
        List<StudySession> sessions = lambdaQuery()
                .eq(StudySession::getUserId, userId)
                .apply("DATE(start_time) = {0}", today)
                .orderByAsc(StudySession::getStartTime)
                .list();

        // 查今日汇总
        StudyDailySummary summary = studyDailySummaryService.lambdaQuery()
                .eq(StudyDailySummary::getUserId, userId)
                .eq(StudyDailySummary::getStudyDate, today)
                .one();

        TodayOverviewVO vo = new TodayOverviewVO();
        if (summary != null) {
            vo.setTotalStudyMinutes(summary.getTotalStudyMinutes());
            vo.setTotalRestMinutes(summary.getTotalRestMinutes());
            vo.setSessionCount(summary.getSessionCount());
            vo.setCategoryMinutes(JSONUtil.toBean(summary.getCategoryMinutes(), HashMap.class));
        } else {
            vo.setTotalStudyMinutes(0);
            vo.setTotalRestMinutes(0);
            vo.setSessionCount(0);
            vo.setCategoryMinutes(new HashMap<>());
        }
        vo.setSessions(sessions.stream().map(this::toSessionItemVO).collect(Collectors.toList()));
        return R.ok(vo);
    }

    // 获取进行中的会话
    @Override
    public R<SessionItemVO> getActiveSession(String token, String loginType) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);
        String userId = tokenUserDTO.getUid();

        StudySession session = lambdaQuery()
                .eq(StudySession::getUserId, userId)
                .eq(StudySession::getStatus, 1)
                .one();
        if (session == null) {
            throw new QAWebException("当前没有进行中的学习会话");
        }
        return R.ok(toSessionItemVO(session));
    }

    /**
     * 将 StudySession 转换为 SessionItemVO
     *
     * @param session 学习会话实体
     * @return 会话详情 VO
     */
    private SessionItemVO toSessionItemVO(StudySession session) {
        SessionItemVO vo = new SessionItemVO();
        vo.setId(session.getId());
        vo.setCategory(session.getCategory());
        vo.setStartTime(session.getStartTime());
        vo.setEndTime(session.getEndTime());
        vo.setFocusMinutes(session.getFocusMinutes());
        vo.setRestMinutes(session.getRestMinutes());
        vo.setStatus(session.getStatus());
        return vo;
    }

    // Impl 实现
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoCloseSession(StudySession session) {
        // 二次校验，防止并发场景下重复关闭
        StudySession latest = getById(session.getId());
        if (latest == null || latest.getStatus() != 1) return;
        // status=3 系统自动关闭，rest_minutes=0
        doCloseSession(latest, CommonConstant.THREE, CommonConstant.ZERO);
    }

    @Override
    public R<HeartbeatResultVO> heartbeat(HeartbeatVO vo, String token, String loginType) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);
        String userId = tokenUserDTO.getUid();

        // 校验会话是否属于当前用户且仍在进行中
        StudySession session = lambdaQuery()
                .eq(StudySession::getId, vo.getSessionId())
                .eq(StudySession::getUserId, userId)
                .eq(StudySession::getStatus, 1)
                .one();

        HeartbeatResultVO result = new HeartbeatResultVO();
        if (session == null) {
            // 会话已被关闭（可能已被自动关闭），通知前端
            result.setActive(false);
            result.setNextDeadline(null);
            return R.ok(result);
        }

        // 刷新心跳 TTL
        refreshHeartbeat(userId, vo.getSessionId());

        result.setActive(true);
        result.setNextDeadline(LocalDateTime.now().plusMinutes(HEARTBEAT_TTL_SECONDS));
        return R.ok(result);

    }

    // 获取某日概览（默认今天）
    @Override
    public R<TodayOverviewVO> getDailyOverview(String token, String loginType, LocalDate date) {
        TokenUserDTO tokenUserDTO = redisComponent.getTokenUserDTO(token, loginType);
        String userId = tokenUserDTO.getUid();

        // 如果未传入日期，默认查询今天
        LocalDate queryDate = (date != null) ? date : LocalDate.now();

        // 查指定日期的所有会话
        List<StudySession> sessions = lambdaQuery()
                .eq(StudySession::getUserId, userId)
                .apply("DATE(start_time) = {0}", queryDate)
                .orderByAsc(StudySession::getStartTime)
                .list();

        // 查指定日期的汇总
        StudyDailySummary summary = studyDailySummaryService.lambdaQuery()
                .eq(StudyDailySummary::getUserId, userId)
                .eq(StudyDailySummary::getStudyDate, queryDate)
                .one();

        TodayOverviewVO vo = new TodayOverviewVO();
        if (summary != null) {
            vo.setTotalStudyMinutes(summary.getTotalStudyMinutes());
            vo.setTotalRestMinutes(summary.getTotalRestMinutes());
            vo.setSessionCount(summary.getSessionCount());
            vo.setCategoryMinutes(JSONUtil.toBean(summary.getCategoryMinutes(), HashMap.class));
        } else {
            vo.setTotalStudyMinutes(0);
            vo.setTotalRestMinutes(0);
            vo.setSessionCount(0);
            vo.setCategoryMinutes(new HashMap<>());
        }
        vo.setSessions(sessions.stream().map(this::toSessionItemVO).collect(Collectors.toList()));
        return R.ok(vo);
    }
}




