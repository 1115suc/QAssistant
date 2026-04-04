package course.QAssistant.pojo.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "心跳响应VO")
public class HeartbeatResultVO {
    @Schema(description = "会话是否仍在进行中")
    private Boolean active;
    @Schema(description = "下次心跳截止时间（超过此时间未续期将被关闭）")
    private LocalDateTime nextDeadline;
}