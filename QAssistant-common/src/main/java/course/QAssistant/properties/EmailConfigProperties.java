package course.QAssistant.properties;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.mail")
@ConditionalOnProperty(prefix = "spring.mail",value = "password")
public class EmailConfigProperties {
    private String username;
}
