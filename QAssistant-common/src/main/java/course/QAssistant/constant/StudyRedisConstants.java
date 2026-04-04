package course.QAssistant.constant;

public class StudyRedisConstants {
    // 自动关闭标记：QAssistant:STUDY:AUTO_CLOSE:{userId}:{sessionId}  TTL=30min
    // public static final String AUTO_CLOSE_KEY = "QAssistant:STUDY:AUTO_CLOSE:{}:{}";
    // 定时任务分布式锁
    // 前端每15min续期一次，30min内无心跳则Key过期 → 触发自动关闭
    public static final String HEARTBEAT_KEY = "QAssistant:STUDY:HEARTBEAT:{}:{}";
    public static final long HEARTBEAT_TTL_SECONDS = 30L;
    public static final long HEARTBEAT_TTL_MINUTES = 30 * 60L;   // Key过期时间
    public static final long HEARTBEAT_INTERVAL_MINUTES = 15 * 60L; // 前端调用间隔(文档用)

    // 定时任务分布式锁
    public static final String SCHEDULED_LOCK = "QAssistant:STUDY:SCHEDULED:AUTO_CLOSE:LOCK";
}