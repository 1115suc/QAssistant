package course.QAssistant;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Slf4j
@MapperScan("course.QAssistant.mapper")
@SpringBootApplication()
public class QAssistantBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(QAssistantBackendApplication.class, args);
    }
}
