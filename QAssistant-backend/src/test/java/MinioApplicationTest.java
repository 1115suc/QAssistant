import course.QAssistant.QAssistantBackendApplication;
import course.QAssistant.minio.model.FileUploadResponse;
import course.QAssistant.minio.service.MinIOFileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@SpringBootTest(classes = QAssistantBackendApplication.class)
public class MinioApplicationTest {
    @Autowired
    private MinIOFileService minIOFileService;

    @Test
    public void testUploadFile() throws IOException {
        // 创建 bucket
        minIOFileService.checkBucket("test");

        // 正确创建 MockMultipartFile
        File file = new File("D:\\Develop\\JavaProject\\QAssistant\\document\\img\\1734613927466.jpg");
        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                file.getName(),
                "image/jpeg",
                input
        );

        // 上传文件
        FileUploadResponse fileUploadResponse = minIOFileService.uploadFile(multipartFile, "test", "img/");

        System.out.println(fileUploadResponse.toString());
    }
}
