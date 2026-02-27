package course.QAssistant.enums;

public enum StatusEnum {
    NORMAL(0, "正常"),
    DISABLED(1, "锁定");

    private Integer code;
    private String message;

    StatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
