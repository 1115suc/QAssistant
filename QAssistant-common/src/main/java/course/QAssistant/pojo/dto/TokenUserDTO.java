package course.QAssistant.pojo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenUserDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String nickname;
    // 登录方式(1.Web 2.Android 3.ios)
    private Integer loginWhere;
    private Long expireAt;
    private String token;
}
