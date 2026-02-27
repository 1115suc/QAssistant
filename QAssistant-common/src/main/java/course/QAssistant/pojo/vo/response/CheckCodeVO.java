package course.QAssistant.pojo.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "验证码返回数据")
public class CheckCodeVO {
    @Schema(description = "sessionId 用于标识验证码")
    private String sessionId;

    @Schema(description = "base64格式的图片数据")
    private String imageData;
}
