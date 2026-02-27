package course.QAssistant.service.base;

import course.QAssistant.constant.RedisConstant;
import course.QAssistant.exception.QAWebException;
import course.QAssistant.pojo.vo.response.ResponseCode;
import course.QAssistant.util.RedisUtil;
import jodd.util.StringUtil;

public class BaseService {
    public static void vertifyCheckCode(String checkCode, String sessionId, RedisUtil redisUtil) {
        if (!redisUtil.hasKey(RedisConstant.CAPTCHA_KEY + sessionId)) {
            throw new QAWebException(ResponseCode.CHECK_CODE_EXPIRED.getMessage());
        }
        String verification = (String) redisUtil.get(RedisConstant.CAPTCHA_KEY + sessionId);
        if (!StringUtil.equals(verification, checkCode)) {
            throw new QAWebException(ResponseCode.CHECK_CODE_ERROR.getMessage());
        }
        redisUtil.del(RedisConstant.CAPTCHA_KEY + sessionId);
    }
}
