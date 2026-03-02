package course.QAssistant.handler;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import course.QAssistant.exception.QAException;
import course.QAssistant.exception.QAWebException;
import course.QAssistant.exception.RepeatSubmitException;
import course.QAssistant.pojo.enums.ResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 自定义异常处理
     */
    @ExceptionHandler(QAException.class)
    public ResponseEntity<Object> handle(QAException exception) {
        if (ObjectUtil.isNotEmpty(exception.getCause())) {
            log.error("自定义异常处理 -> ", exception);
        }
        return buildResponse(exception.getCode(), exception.getMsg(), exception.getStatus());
    }

    /**
     * web自定义异常处理
     */
    @ExceptionHandler(QAWebException.class)
    public ResponseEntity<Object> handle(QAWebException exception) {
        if (ObjectUtil.isNotEmpty(exception.getCause())) {
            log.error("自定义异常处理 -> ", exception);
        }
        return buildResponse(exception.getCode(), exception.getMsg(), exception.getStatus());
    }

    /**
     * 参数校验失败异常
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handle(ValidationException exception) {
        List<String> errors = new ArrayList<>();
        if (exception instanceof ConstraintViolationException) {
            ConstraintViolationException exs = (ConstraintViolationException) exception;
            Set<ConstraintViolation<?>> violations = exs.getConstraintViolations();
            errors = violations.stream()
                    .map(ConstraintViolation::getMessage).collect(Collectors.toList());
        }
        log.error("参数校验失败异常 -> ", exception);
        String msg = errors.isEmpty() ? ResponseCode.CODE_600.getMessage() : errors.toString();
        return buildResponse(ResponseCode.CODE_600.getCode(), msg, HttpStatus.BAD_REQUEST);
    }

    /**
     * 其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handle(Exception exception, HttpServletRequest request) {
        log.error("请求错误，请求地址{},错误信息:", request.getRequestURL(), exception);

        ResponseCode responseCode;
        String extraMsg = null;
        HttpStatus httpStatus = HttpStatus.OK;

        if (exception instanceof MethodArgumentNotValidException) {
            // 处理Spring Boot参数校验失败异常
            responseCode = ResponseCode.CODE_600;
            BindingResult bindingResult = ((MethodArgumentNotValidException) exception).getBindingResult();
            Map<String, String> errors = new HashMap<>();
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
            extraMsg = errors.toString();
            httpStatus = HttpStatus.BAD_REQUEST;
            log.warn("参数校验异常: {}", extraMsg);
        } else if (exception instanceof HttpMessageNotReadableException) {
            // 处理请求体解析异常（JSON格式错误、空请求体等情况）
            responseCode = ResponseCode.CODE_600;
            extraMsg = "请求体格式错误，请传入正确的JSON格式数据";
            httpStatus = HttpStatus.BAD_REQUEST;
            log.warn("请求体解析异常: {}", extraMsg);
        } else if (exception instanceof RepeatSubmitException) {
            // 处理重复提交异常（幂等性校验失败，防止重复请求）
            responseCode = ResponseCode.CODE_429;
            extraMsg = exception.getMessage();
            httpStatus = HttpStatus.TOO_MANY_REQUESTS;
            log.warn("重复提交异常: {}", extraMsg);
        } else if (exception instanceof NoHandlerFoundException || exception instanceof NoResourceFoundException) {
            // 处理404异常（请求的接口或资源不存在）
            responseCode = ResponseCode.CODE_404;
            httpStatus = HttpStatus.NOT_FOUND;
            // 404不打印堆栈，仅警告
            log.warn("资源未找到: {}", request.getRequestURI());
        } else if (exception instanceof DuplicateKeyException) {
            //  处理数据库主键冲突异常（插入重复数据时触发）
            responseCode = ResponseCode.CODE_601;
            log.warn("数据库主键冲突: {}", exception.getMessage());
        } else {
            // 处理系统内部错误、未捕获的异常
            responseCode = ResponseCode.CODE_500;
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            // 真正的未知异常才打印堆栈
            log.error("系统未知异常，请求地址{},错误信息:", request.getRequestURL(), exception);
        }

        String finalMsg = extraMsg != null ? extraMsg : responseCode.getMessage();
        return buildResponse(responseCode.getCode(), finalMsg, httpStatus);
    }

    private ResponseEntity<Object> buildResponse(Integer code, Object msg, int status) {
        return ResponseEntity.status(status)
                .body(MapUtil.<String, Object>builder()
                        .put("code", code)
                        .put("msg", msg)
                        .build());
    }

    private ResponseEntity<Object> buildResponse(Integer code, Object msg, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(MapUtil.<String, Object>builder()
                        .put("code", code)
                        .put("msg", msg)
                        .build());
    }
}