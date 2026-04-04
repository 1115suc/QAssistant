package course.QAssistant.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 学习会话结束同步请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudySessionEndDTO {
    /**
     * 用户 ID
     */
    private String userId;
    
    /**
     * 会话开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 专注时长（分钟）
     */
    private int focusMinutes;
    
    /**
     * 休息时长（分钟）
     */
    private int restMinutes;
    
    /**
     * 学习类别
     */
    private String category;
}