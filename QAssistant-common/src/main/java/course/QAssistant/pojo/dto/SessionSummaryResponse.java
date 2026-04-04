package course.QAssistant.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 会话列表项（不含 messages，轻量级）
 */
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SessionSummaryResponse {
    private String id;
    private String userUid;
    private String title;
    private Long aiModelId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}