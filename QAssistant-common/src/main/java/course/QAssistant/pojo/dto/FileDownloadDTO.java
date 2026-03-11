package course.QAssistant.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDownloadDTO {
    private InputStream inputStream;
    private String fileName;
}

