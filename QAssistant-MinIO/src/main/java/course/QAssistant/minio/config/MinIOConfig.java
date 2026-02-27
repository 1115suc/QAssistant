package course.QAssistant.minio.config;

import course.QAssistant.minio.properties.MinIOConfigProperties;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO configuration class
 */
@Slf4j
@Configuration
@ComponentScan("course.QAssistant.minio")
@EnableConfigurationProperties(MinIOConfigProperties.class)
public class MinIOConfig {

    private final MinIOConfigProperties minIOConfigProperties;

    public MinIOConfig(MinIOConfigProperties minIOConfigProperties) {
        this.minIOConfigProperties = minIOConfigProperties;
    }

    @Bean
    public MinioClient minioClient() {
        log.info("带端点初始化 MinIO 客户端: {}", minIOConfigProperties.getEndpoint());
        try {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(minIOConfigProperties.getEndpoint())
                    .credentials(minIOConfigProperties.getAccessKey(), minIOConfigProperties.getSecretKey())
                    .build();
            
            // 如果需要可以设置超时，但建构者在新版本中不会直接暴露，除非有 OkHttpClient 自定义。
            // MinIO Java SDK 8.5.10 使用 OkHttp。setTimeout 方法可在早期版本或自定义 HttpClient 中使用。
            // 实际上，MinioClient.setTimeout（） 是可用的。我们去看看。
            minioClient.setTimeout(
                    minIOConfigProperties.getConnectTimeout(),
                    minIOConfigProperties.getWriteTimeout(),
                    minIOConfigProperties.getReadTimeout()
            );

            return minioClient;
        } catch (Exception e) {
            log.error("未能初始化 MinIO 客户端", e);
            throw new RuntimeException("未能初始化 MinIO 客户端", e);
        }
    }
}
