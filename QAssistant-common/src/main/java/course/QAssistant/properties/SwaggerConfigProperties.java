package course.QAssistant.properties;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Data
@Component
@ConfigurationProperties(prefix = "knife4j.swagger")
@ConditionalOnProperty(prefix = "knife4j.swagger",value = "package-path")
public class SwaggerConfigProperties implements Serializable {
    // 扫描的包路径
    public String packagePath;
    // 文档标题
    public String title;
    // 文档描述
    public String description;
    // 联系人姓名
    public String contactName;
    // 联系人URL
    public String contactUrl;
    // 联系人邮箱
    public String contactEmail;
    // 文档版本
    public String version;
}
