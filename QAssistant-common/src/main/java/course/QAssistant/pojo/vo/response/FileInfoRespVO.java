package course.QAssistant.pojo.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "文件信息响应（用于列表）")
public class FileInfoRespVO {

    @Schema(description = "minioFile记录id")
    private Long id;

    @Schema(description = "上传时文件名(原始文件名)")
    private String fileName;

    @Schema(description = "文件扩展名(不含点)")
    private String fileExt;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "MinIO存储路径(bucket/objectName)")
    private String minioPath;

    @Schema(description = "上传时间")
    private Date createTime;
}
