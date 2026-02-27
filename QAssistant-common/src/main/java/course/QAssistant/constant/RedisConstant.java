package course.QAssistant.constant;

public class RedisConstant {
    // 项目名
    public static final String PROJECT_NAME = "QTalk:";
    // 验证码
    public static final String CHECK_CODE = PROJECT_NAME + "CheckCode:";
    // 图形验证码key
    public static final String CAPTCHA_KEY = CHECK_CODE + "GraphicsCaptcha:";
    // 邮箱验证码
    public static final String EMAIL_CODE = CHECK_CODE + "EmailCode:";
    // TokenKey
    public static final String TOKEN = PROJECT_NAME + "Token:";
    // Web登录
    public static final String WEB_LOGIN = TOKEN + "WebLogin:";
    // Android登录
    public static final String ANDROID_LOGIN = TOKEN + "AndroidLogin:";
    // IOS登录
    public static final String IOS_LOGIN = TOKEN + "IOSLogin:";
}
