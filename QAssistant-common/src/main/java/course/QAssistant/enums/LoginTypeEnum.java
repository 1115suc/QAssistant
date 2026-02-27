package course.QAssistant.enums;

import course.QAssistant.constant.RedisConstant;
import course.QAssistant.exception.QAWebException;
import course.QAssistant.pojo.vo.response.ResponseCode;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum LoginTypeEnum {
    WEB(1, RedisConstant.WEB_LOGIN),
    ANDROID(2, RedisConstant.ANDROID_LOGIN),
    IOS(3, RedisConstant.IOS_LOGIN);

    private final int code;
    private final String prefix;

    LoginTypeEnum(int code, String prefix) {
        this.code = code;
        this.prefix = prefix;
    }

    private static final Map<Integer, LoginTypeEnum> MAP =
        Arrays.stream(values()).collect(Collectors.toMap(LoginTypeEnum::getCode, Function.identity()));

    public static LoginTypeEnum of(int code) {
        LoginTypeEnum type = MAP.get(code);
        if (type == null) {
            throw new QAWebException(ResponseCode.LOGIN_FROM_ERROR.getMessage());
        }
        return type;
    }

    public String getPrefix() {
        return prefix;
    }

    public int getCode() {
        return code;
    }
}