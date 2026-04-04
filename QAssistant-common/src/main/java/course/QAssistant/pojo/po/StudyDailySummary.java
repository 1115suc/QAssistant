package course.QAssistant.pojo.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 每日学习汇总统计表
 * @TableName study_daily_summary
 */
@TableName(value ="study_daily_summary")
@Data
public class StudyDailySummary {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户标识
     */
    private String userId;

    /**
     * 统计日期
     */
    private LocalDate studyDate;

    /**
     * 当日总学习时长(分钟)
     */
    private Integer totalStudyMinutes;

    /**
     * 当日总休息时长(分钟)
     */
    private Integer totalRestMinutes;

    /**
     * 当日学习次数
     */
    private Integer sessionCount;

    /**
     * 各标签时长 {"语文":90,"数学":60}
     */
    private String categoryMinutes;

    /**
     * 最后更新时间
     */
    private LocalDateTime updatedAt;
}