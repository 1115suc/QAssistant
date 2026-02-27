package course.QAssistant.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "头像上传值对象类")
@Data
public class AvatarUploadVo {
    // 头像文件
    @Schema(description = "头像文件")
    private MultipartFile avatarFile;
}