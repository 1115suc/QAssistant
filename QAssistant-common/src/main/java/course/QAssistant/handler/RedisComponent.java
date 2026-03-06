package course.QAssistant.handler;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONUtil;
import course.QAssistant.constant.TimeConstant;
import course.QAssistant.exception.QAWebException;
import course.QAssistant.pojo.dto.TokenUserDTO;
import course.QAssistant.pojo.enums.LoginTypeEnum;
import course.QAssistant.pojo.enums.ResponseCode;
import course.QAssistant.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisComponent {
    private final RedisUtil redisUtil;

    // 获取用户信息 TokenUserDTO
    public TokenUserDTO getTokenUserDTO(String token, String LoginType) {
        Integer type = Convert.toInt(LoginType);
        String loginPrefix = LoginTypeEnum.of(type).getPrefix();

        if (!redisUtil.hasKey(loginPrefix + token)) {
            throw new QAWebException(ResponseCode.LOGIN_TIMEOUT.getMessage());
        }

        String tokenLoginInfo = (String) redisUtil.get(loginPrefix + token);
        TokenUserDTO tokenUserDTO = JSONUtil.toBean(tokenLoginInfo.toString(), TokenUserDTO.class);
        return tokenUserDTO;
    }
}
