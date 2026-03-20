package course.QAssistant.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 会话详情（含完整 messages）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDetailResponse {
    private String id;
    private String userUid;
    private String title;
    private Long aiModelId;
    private Date createdAt;
    private Date updatedAt;
    private List<MessageResponse> messages;
}