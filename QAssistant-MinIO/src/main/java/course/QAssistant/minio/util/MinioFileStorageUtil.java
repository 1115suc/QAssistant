package course.QAssistant.minio.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Component
public class MinioFileStorageUtil {
    
    private final static String separator = "/";

    /**
     * 构建文件存储路径
     * @param dirPrefix 目录前缀，如果为空则不添加前缀
     * @param filename 文件名
     * @return 完整路径: [dirPrefix/]yyyy/MM/dd/filename
     */
    public String buildFilePath(String dirPrefix, String filename) {
        StringBuilder stringBuilder = new StringBuilder(50);
        if (dirPrefix != null && !dirPrefix.isEmpty()) {
            stringBuilder.append(dirPrefix);
            if (!dirPrefix.endsWith(separator)) {
                stringBuilder.append(separator);
            }
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String todayString = simpleDateFormat.format(new Date());
        stringBuilder.append(todayString)
                .append(separator)
                .append(filename);
        return stringBuilder.toString();
    }
}