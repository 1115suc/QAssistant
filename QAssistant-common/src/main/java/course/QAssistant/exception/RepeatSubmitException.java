package course.QAssistant.exception;

import lombok.Getter;

/**
 * 重复提交异常
 */
@Getter
public class RepeatSubmitException extends RuntimeException {

    private final int code;
    private final String message;

    public RepeatSubmitException(String message) {
        super(message);
        this.code = 429;
        this.message = message;
    }

    public RepeatSubmitException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}