package course.QAssistant.pojo.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "文件URL响应")
public class FileUrlRespVO {

    @Schema(description = "minioFile记录id")
    private Long id;

    @Schema(description = "原始文件名")
    private String fileName;

    @Schema(description = "实际返回的URL类型 (preview=临时签名URL / public=永久公开URL)")
    private String urlType;

    @Schema(description = "生成的文件访问链接")
    private String url;
}
