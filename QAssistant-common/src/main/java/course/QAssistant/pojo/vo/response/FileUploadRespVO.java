package course.QAssistant.pojo.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "文件上传响应参数")
public class FileUploadRespVO {

    @Schema(description = "minioFile记录id")
    private Long id;

    @Schema(description = "MinIO bucket")
    private String bucket;

    @Schema(description = "MinIO objectName(对象key)")
    private String objectName;

    @Schema(description = "MinIO存储路径(bucket/objectName)")
    private String minioPath;

    @Schema(description = "上传时文件名(原始文件名)")
    private String fileName;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "MIME类型")
    private String mimeType;

    @Schema(description = "预览URL(临时签名URL)")
    private String fileUrl;

    @Schema(description = "上传时间")
    private LocalDateTime uploadTime;
}

