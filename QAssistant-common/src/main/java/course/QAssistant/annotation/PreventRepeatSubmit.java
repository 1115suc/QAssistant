package course.QAssistant.annotation;

import course.QAssistant.pojo.enums.CheckType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 防重复提交注解
 * 使用示例：
 *   @PreventRepeatSubmit(timeout = 5, checkType = CheckType.ALL, message = "请勿重复提交")
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PreventRepeatSubmit {

    /**
     * 锁定时间（秒），默认 5 秒
     */
    int timeout() default 5;

    /**
     * 检查类型，默认基于 IP
     */
    CheckType checkType() default CheckType.TOKEN;

    /**
     * 重复提交时的错误消息
     */
    String message() default "请勿重复提交，请稍后再试";

    /**
     * 自定义 key 前缀，用于区分不同业务场景
     */
    String keyPrefix() default "prevent:repeat:";
}