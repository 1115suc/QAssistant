package course.QAssistant.pojo.enums;

public enum ResponseCode {
    // ==================== 成功响应 ====================

    // 通用
    SUCCESS(200, "操作成功"),

    // 注册
    REGISTER_SUCCESS(200, "注册成功"),

    // 登录
    LOGIN_SUCCESS(200, "登录成功"),
    LOGOUT_SUCCESS(200, "退出登录成功"),
    RESET_PASSWORD_SUCCESS(200, "重置密码成功"),

    // 邮件
    EMAIL_SEND_SUCCESS(200, "邮件发送成功"),

    // 群组
    GROUP_CREATE_SUCCESS(200, "创建群聊成功"),
    GROUP_LIST_EMPTY(200, "群组列表为空"),

    // ==================== 失败响应 ====================

    // 通用
    ERROR(0, "操作失败"),
    DATA_ERROR(0, "参数异常"),
    NO_RESPONSE_DATA(0, "无响应数据"),
    SERVER_ERROR(500, "服务器返回错误，请联系管理员"),
    DUPLICATE_REQUEST(429, "请求处理中，请勿重复提交"),

    // 注册
    REGISTER_ERROR(0, "注册失败"),
    ACCOUNT_EXISTS_ERROR(0, "该账号已存在"),
    ACCOUNT_NOT_EXISTS(0, "该账号不存在"),
    ACCOUNT_LOCKED(0, "该账号已被锁定"),

    // 登录
    LOGIN_ERROR(0, "登录失败"),
    LOGIN_TIMEOUT(0, "登录超时"),
    LOGIN_FROM_ERROR(0, "登录来源错误"),
    PASSWORD_ERROR(0, "密码错误"),
    EID_OR_PASSWORD_ERROR(0, "邮箱或密码错误"),
    SYSTEM_PASSWORD_ERROR(0, "系统密码错误"),

    // 验证码
    CHECK_CODE_GENERATE_ERROR(0, "生成校验码失败"),
    CHECK_CODE_NOT_EMPTY(0, "验证码不能为空"),
    CHECK_CODE_ERROR(0, "验证码错误"),
    CHECK_CODE_EXPIRED(0, "验证码已失效"),
    CHECK_CODE_EXPIRED_WAIT(0, "验证码已失效,请稍后重试"),

    // 邮件
    EMAIL_SEND_ERROR(0, "邮件发送失败"),
    EMAIL_SEND_ERROR_WAIT(0, "邮件已发送,请稍后重试"),

    // Token / 权限
    TOKEN_ERROR(2, "用户未登录，请先登录"),
    INVALID_TOKEN(0, "无效的票据"),
    NOT_PERMISSION(3, "没有权限访问该资源"),
    ANONYMOUS_NOT_PERMISSION(0, "匿名用户没有权限访问"),

    // 菜单权限
    OPERATION_MENU_PERMISSION_CATALOG_ERROR(0, "操作后的菜单类型是目录，所属菜单必须为默认顶级菜单或者目录"),
    OPERATION_MENU_PERMISSION_MENU_ERROR(0, "操作后的菜单类型是菜单，所属菜单必须为目录类型"),
    OPERATION_MENU_PERMISSION_BTN_ERROR(0, "操作后的菜单类型是按钮，所属菜单必须为菜单类型"),
    OPERATION_MENU_PERMISSION_URL_CODE_NULL(0, "菜单权限的按钮标识不能为空"),
    ROLE_PERMISSION_RELATION(0, "该菜单权限存在子集关联，不允许删除"),

    // 群组
    GROUP_AVATAR_UPLOAD_ERROR(0, "创建群聊失败，群聊头像上传失败"),
    GROUP_NOT_EXISTS(0, "该群组不存在"),

    // 响应码枚举
    CODE_200(200, "请求成功"),
    CODE_404(404, "请求地址不存在"),
    CODE_429(429, "请求过于频繁，请稍后重试"),
    CODE_600(600, "请求参数错误"),
    CODE_601(601, "信息已经存在"),
    CODE_602(602, "文件不存在"),
    CODE_500(500, "服务器返回错误，请联系管理员");

    private int code;
    private String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
