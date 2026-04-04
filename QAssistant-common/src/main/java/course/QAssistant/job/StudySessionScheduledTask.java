package course.QAssistant.job;

import cn.hutool.core.util.StrUtil;
import course.QAssistant.pojo.po.StudySession;
import course.QAssistant.service.StudySessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static course.QAssistant.constant.StudyRedisConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudySessionScheduledTask {

    private final StudySessionService studySessionService;
    private final StringRedisTemplate redisTemplate;

    /**
     * 每3分钟扫描，检查进行中的会话心跳Key是否已过期
     * 过期则说明前端超过30min未续期（用户已退出应用）→ 自动关闭
     */
    @Scheduled(fixedDelay = 3 * 60 * 1000)
    public void autoCloseDeadSessions() {
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(SCHEDULED_LOCK, "1", 170L, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            log.debug("[心跳检测] 其他实例正在执行，跳过");
            return;
        }
        try {
            doAutoClose();
        } finally {
            redisTemplate.delete(SCHEDULED_LOCK);
        }
    }

    private void doAutoClose() {
        // 查所有进行中的会话（不再限制时间，交由心跳Key判断）
        List<StudySession> activeSessions = studySessionService.lambdaQuery()
                .eq(StudySession::getStatus, 1)
                .list();

        if (activeSessions.isEmpty()) return;

        for (StudySession session : activeSessions) {
            String heartbeatKey = StrUtil.format(HEARTBEAT_KEY, session.getUserId(), session.getId());
            Boolean alive = redisTemplate.hasKey(heartbeatKey);

            if (!Boolean.TRUE.equals(alive)) {
                // 心跳 Key 已过期 → 用户已退出应用，自动关闭
                try {
                    studySessionService.autoCloseSession(session);
                    log.info("[心跳检测] 会话 {} (用户:{}) 心跳超时，已自动关闭",
                            session.getId(), session.getUserId());
                } catch (Exception e) {
                    log.error("[心跳检测] 会话 {} 自动关闭失败", session.getId(), e);
                }
            }
        }
    }
}