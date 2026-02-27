package course.QAssistant.enums;

public enum DeletedEnum {
    NOT_DELETED(0, "未删除"),
    DELETED(1, "已删除");

    private Integer code;
    private String message;

    DeletedEnum(Integer code, String message) {
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
