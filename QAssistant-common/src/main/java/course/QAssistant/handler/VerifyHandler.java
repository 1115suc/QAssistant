package course.QAssistant.handler;

import course.QAssistant.constant.RedisConstant;
import course.QAssistant.exception.QAWebException;
import course.QAssistant.pojo.enums.ResponseCode;
import course.QAssistant.util.RedisUtil;
import jodd.util.StringUtil;

public class VerifyHandler {
    public static void verifyCheckCode(String checkCode, String sessionId, RedisUtil redisUtil) {
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
