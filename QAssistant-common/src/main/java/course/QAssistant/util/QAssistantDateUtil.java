package course.QAssistant.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class QAssistantDateUtil {
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();

    /**
     * 获取当前时间，格式为 yyyy-MM-dd HH:mm:ss
     * @return 格式化后的当前时间字符串
     */
    public static String getCurrentTime() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    /**
     * 获取当前日期，格式为 yyyy-MM-dd
     * @return 格式化后的当前日期字符串
     */
    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    // ... existing code ...

    /**
     * 将 Date 对象的时分秒去除，返回当天的 00:00:00
     * @param date 日期对象
     * @return 当天零点的 Date 对象
     */
    public static Date truncateToMidnight(Date date) {
        if (date == null) {
            return null;
        }
        LocalDate localDate = date.toInstant().atZone(DEFAULT_ZONE_ID).toLocalDate();
        return Date.from(localDate.atStartOfDay(DEFAULT_ZONE_ID).toInstant());
    }

    // ... existing code ...

    /**
     * 计算两个时间点之间的分钟差值
     * 规则：不足 30 秒算 0 分钟，大于等于 30 秒按 1 分钟计算
     *
     * @param startTime 开始时间（格式：yyyy-MM-dd HH:mm:ss）
     * @param endTime 结束时间（格式：yyyy-MM-dd HH:mm:ss）
     * @return 相差的分钟数
     */
    public static long calculateMinuteDifference(String startTime, String endTime) {
        LocalDateTime start = LocalDateTime.parse(startTime, DATETIME_FORMATTER);
        LocalDateTime end = LocalDateTime.parse(endTime, DATETIME_FORMATTER);

        return calculateMinuteDifference(start, end);
    }

    /**
     * 计算两个时间点之间的分钟差值（使用 LocalDateTime 对象）
     * 规则：不足 30 秒算 0 分钟，大于等于 30 秒按 1 分钟计算
     *
     * @param start 开始时间
     * @param end 结束时间
     * @return 相差的分钟数
     */
    public static long calculateMinuteDifference(LocalDateTime start, LocalDateTime end) {
        long totalSeconds = ChronoUnit.SECONDS.between(start, end);

        long minutes = totalSeconds / 60;
        long remainingSeconds = totalSeconds % 60;

        if (remainingSeconds >= 30) {
            minutes += 1;
        }

        return minutes;
    }

    // ... existing code ...

    /**
     * 计算两个时间点之间的分钟差值（使用 Date 对象）
     * 规则：不足 30 秒算 0 分钟，大于等于 30 秒按 1 分钟计算
     *
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 相差的分钟数
     */
    public static long calculateMinuteDifference(Date startDate, Date endDate) {
        LocalDateTime start = startDate.toInstant().atZone(DEFAULT_ZONE_ID).toLocalDateTime();
        LocalDateTime end = endDate.toInstant().atZone(DEFAULT_ZONE_ID).toLocalDateTime();

        return calculateMinuteDifference(start, end);
    }
}
