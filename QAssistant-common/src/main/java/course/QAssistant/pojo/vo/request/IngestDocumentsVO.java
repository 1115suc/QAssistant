package course.QAssistant.pojo.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "RAG文档解析请求对象")
public class IngestDocumentsVO {
    @NotEmpty(message = "文件ID列表不能为空")
    @Schema(description = "需要解析的文件ID列表（仅允许解析自己上传的文件）", example = "[1, 2, 3]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> fileIds;
}
