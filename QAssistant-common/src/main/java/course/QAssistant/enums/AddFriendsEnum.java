package course.QAssistant.enums;

public enum AddFriendsEnum {
    AGREE_ADD_FRIEND(0, "同意后加好友"),
    DIRECT_ADD_FRIEND(1, "直接加好友"),
    NOT_ALLOW_ADD_FRIEND(2, "不允许加好友");

    private Integer code;
    private String message;

    AddFriendsEnum(Integer code, String message) {
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
