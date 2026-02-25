package course.QAssistant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("course.QAssistant.mapper")
@SpringBootApplication
public class QAssistantBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(QAssistantBackendApplication.class, args);
    }
}
