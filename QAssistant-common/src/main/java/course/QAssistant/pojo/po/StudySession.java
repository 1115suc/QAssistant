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
 * 学习会话记录表
 * @TableName study_session
 */
@TableName(value ="study_session")
@Data
public class StudySession {
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
     * 用户自定义标签 eg.语文/数学
     */
    private String category;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间，未结束时为NULL
     */
    private LocalDateTime endTime;

    /**
     * 专注时长(分钟)，结束后计算写入
     */
    private Integer focusMinutes;

    /**
     * 本次会话后休息时长(分钟)
     */
    private Integer restMinutes;

    /**
     * 状态: 1=进行中 2=正常结束 3=系统关闭
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDate createdAt;


}