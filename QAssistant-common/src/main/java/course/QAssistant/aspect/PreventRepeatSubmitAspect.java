package course.QAssistant.aspect;

import course.QAssistant.annotation.PreventRepeatSubmit;
import course.QAssistant.exception.RepeatSubmitException;
import course.QAssistant.pojo.enums.CheckType;
import course.QAssistant.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 防重复提交 AOP 切面
 *
 * 核心流程：
 *   1. 解析注解参数（timeout、checkType、message、keyPrefix）
 *   2. 从 Request 中提取 IP / DeviceID / Token 组合成唯一 Key
 *   3. 通过 Redis SETNX 尝试加锁
 *      - 加锁成功 → 执行目标方法，方法完成后删除 key（或让其自然过期）
 *      - 加锁失败 → 抛出 RepeatSubmitException
 *   4. 记录操作日志
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PreventRepeatSubmitAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(course.QAssistant.annotation.PreventRepeatSubmit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // ---------- 1. 获取注解参数 ----------
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        PreventRepeatSubmit annotation = method.getAnnotation(PreventRepeatSubmit.class);

        int timeout       = annotation.timeout();
        CheckType checkType = annotation.checkType();
        String message    = annotation.message();
        String keyPrefix  = annotation.keyPrefix();

        // ---------- 2. 获取 HttpServletRequest ----------
        ServletRequestAttributes attributes =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            // 非 Web 环境，直接放行
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();

        // ---------- 3. 构建 Redis Key ----------
        String redisKey = buildRedisKey(keyPrefix, checkType, request, joinPoint);

        // ---------- 4. Redis SETNX 尝试加锁（setIfAbsent = SET key value NX PX） ----------
        Boolean locked = redisTemplate.opsForValue()
            .setIfAbsent(redisKey, "1", timeout, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(locked)) {
            // 加锁失败 = 重复提交
            log.warn("[防重复提交] 重复请求被拦截 | key={} | uri={} | ip={}",
                redisKey, request.getRequestURI(), RequestUtil.getClientIp(request));
            throw new RepeatSubmitException(message);
        }

        log.info("[防重复提交] 请求通过 | key={} | timeout={}s | checkType={}",
            redisKey, timeout, checkType);

        // ---------- 5. 执行目标方法 ----------
        try {
            return joinPoint.proceed();
        } finally {
            // 如果希望方法完成后立即解锁（不等超时），取消下面注释
            // redisTemplate.delete(redisKey);
        }
    }

    /**
     * 根据 CheckType 构建 Redis Key
     *
     * Key 格式：{keyPrefix}:{className}#{methodName}:{checkValue}
     * 例如：prevent:repeat:OrderController#createOrder:192.168.1.1
     */
    private String buildRedisKey(String keyPrefix,
                                  CheckType checkType,
                                  HttpServletRequest request,
                                  ProceedingJoinPoint joinPoint) {
        String className  = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String methodId   = className + "#" + methodName;

        String checkValue = switch (checkType) {
            case IP        -> RequestUtil.getClientIp(request);
            case DEVICE_ID -> RequestUtil.getDeviceId(request);
            case TOKEN     -> hashValue(RequestUtil.getToken(request));  // Token 做哈希，避免 key 过长
            case ALL       -> RequestUtil.getClientIp(request)
                              + ":" + RequestUtil.getDeviceId(request)
                              + ":" + hashValue(RequestUtil.getToken(request));
        };

        return keyPrefix + methodId + ":" + checkValue;
    }

    /**
     * 对敏感值（Token）做简单哈希，避免 Redis Key 过长或泄露信息
     */
    private String hashValue(String value) {
        return String.valueOf(value.hashCode() & 0x7FFFFFFF);  // 正整数哈希
    }
}