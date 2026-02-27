package course.QAssistant.handler;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import course.QAssistant.exception.QAException;
import course.QAssistant.exception.QAWebException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 MethodArgumentNotValidException (Spring Boot 参数校验失败)
     * @param exception 参数校验异常
     * @return 响应数据
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        log.error("参数校验失败异常 -> ", exception); // 总是打印日志
        BindingResult bindingResult = exception.getBindingResult();
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(MapUtil.<String, Object>builder()
                        .put("code", HttpStatus.BAD_REQUEST.value())
                        .put("msg", errors) // 返回具体的字段错误信息
                        .build());
    }

    /**
     * 请求体解析异常处理
     * 用于处理请求体格式错误、空字符串、JSON解析失败等情况
     *
     * @param exception 请求体不可读异常
     * @return 响应数据
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handle(HttpMessageNotReadableException exception) {
        log.error("请求体解析异常 -> ", exception); // 总是打印日志
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(MapUtil.<String, Object>builder()
                        .put("code", HttpStatus.BAD_REQUEST.value())
                        .put("msg", "请求体格式错误，请传入正确的JSON格式数据")
                        .build());
    }

    /**
     * 参数校验失败异常
     *
     * @param exception 校验失败异常
     * @return 响应数据
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handle(ValidationException exception) {
        log.error("参数校验失败异常 -> ", exception); // 总是打印日志
        List<String> errors = null;
        if (exception instanceof ConstraintViolationException) {
            ConstraintViolationException exs = (ConstraintViolationException) exception;
            Set<ConstraintViolation<?>> violations = exs.getConstraintViolations();
            errors = violations.stream()
                    .map(ConstraintViolation::getMessage).collect(Collectors.toList());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(MapUtil.<String, Object>builder()
                        .put("code", HttpStatus.BAD_REQUEST.value())
                        .put("msg", errors)
                        .build());
    }

    /**
     * 自定义异常处理
     *
     * @param exception 自定义异常
     * @return 响应数据
     */
    @ExceptionHandler(QAException.class)
    public ResponseEntity<Object> handle(QAException exception) {
        log.error("自定义异常处理 -> ", exception); // 总是打印日志
        return ResponseEntity.status(exception.getStatus())
                .body(MapUtil.<String, Object>builder()
                        .put("code", exception.getCode())
                        .put("msg", exception.getMsg())
                        .build());
    }

    /**
     * web自定义异常处理
     * 用于统一封装VO对象返回前端
     * @param exception web自定义异常
     * @return 响应数据
     */
    @ExceptionHandler(QAWebException.class)
    public ResponseEntity<Object> handle(QAWebException exception) {
        log.error("自定义异常处理 -> ", exception); // 总是打印日志
        JSONObject jsonObject = JSONUtil.parseObj(exception);
        return ResponseEntity.ok(MapUtil.<String, Object>builder()
                        .put("code", exception.getCode())
                        .put("msg", jsonObject.getStr("msg"))
                        .build());
    }

    /**
     * 其他未知异常
     *
     * @param exception 未知异常
     * @return 响应数据
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handle(Exception exception) {
        log.error("其他未知异常 -> ", exception); // 总是打印日志
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(MapUtil.<String, Object>builder()
                        .put("code", HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .put("msg", ExceptionUtil.stacktraceToString(exception))
                        .build());
    }

}