import cn.hutool.core.io.FileUtil;
import course.QAssistant.QAssistantBackendApplication;
import course.QAssistant.minio.service.MinIOFileService;
import course.QAssistant.util.QAssistantFileUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@SpringBootTest(classes = QAssistantBackendApplication.class)
public class MinioFileTest {
    @Resource
    private MinIOFileService minioFileService;

    @Test
    public void downloadFile() throws IOException {
//        InputStream inputStream = minioFileService.downloadFile("1115suc", "QAssistant/U667192869/DOC/附件1 - 副本 (2).docx");
//        String fileName = "附件1 - 副本 (2).docx";
//        String contentType = QAssistantFileUtil.getSuffix(fileName);
//
//        String targetPath = "E:\\Java\\JavaProject\\QAssistant\\document\\file\\" + fileName;
//
//        // Hutool 一行写入
//        /*FileUtil.writeFromStream(inputStream, targetPath);
//        System.out.println("文件已下载到: " + targetPath);*/
//
//        MockMultipartFile mockMultipartFile = new MockMultipartFile(
//                fileName,           // 参数名
//                fileName,           // 原始文件名
//                contentType,        // 内容类型
//                inputStream         // 输入流
//        );
//
//        mockMultipartFile.transferTo(new File(targetPath));
    }
}
