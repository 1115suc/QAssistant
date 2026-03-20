package course.QAssistant.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEmbeddingConfig implements Serializable {
    private String uid;
    private String ApiKey;
    private String ModelName;
    private String BaseUrl;
    private LocalDateTime updateTime;
}