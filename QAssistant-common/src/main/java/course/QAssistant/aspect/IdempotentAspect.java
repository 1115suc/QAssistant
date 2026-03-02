package course.QAssistant.aspect;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import course.QAssistant.annotation.Idempotent;
import course.QAssistant.constant.RedisConstant;
import course.QAssistant.exception.QAException;
import course.QAssistant.pojo.dto.TokenUserDTO;
import course.QAssistant.pojo.enums.LoginTypeEnum;
import course.QAssistant.pojo.enums.ResponseCode;
import course.QAssistant.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 幂等性切面实现
 * 基于 Redisson 分布式锁，防止重复提交
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentAspect {

    private final RedissonClient redissonClient;
    private final RedisUtil redisUtil;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        // 1. 获取 RequestId (前端必须传)
        String requestId = request.getHeader("X-Request-Id");
        if (StrUtil.isBlank(requestId)) {
            // 如果没有 RequestId，抛出异常要求前端必须传
            throw new QAException("缺少幂等键 X-Request-Id", ResponseCode.ERROR.getCode());
        }

        // 2. 获取用户 ID (防止不同用户使用相同的 RequestId 碰撞)
        String userId = "anonymous";
        String token = request.getHeader("Authorization");
        String loginType = request.getHeader("LoginType");

        if (StrUtil.isNotBlank(token) && StrUtil.isNotBlank(loginType)) {
            try {
                // 根据 Service 层逻辑解析 Token
                Integer type = Convert.toInt(loginType);
                String loginPrefix = LoginTypeEnum.of(type).getPrefix();
                Object tokenLoginInfo = redisUtil.get(loginPrefix + token);

                if (ObjectUtil.isNotNull(tokenLoginInfo)) {
                    TokenUserDTO tokenUserDTO = JSONUtil.toBean(tokenLoginInfo.toString(), TokenUserDTO.class);
                    if (tokenUserDTO != null && tokenUserDTO.getUid() != null) {
                        userId = tokenUserDTO.getUid();
                    }
                }
            } catch (Exception e) {
                log.warn("幂等切面解析 Token 失败: {}", e.getMessage());
            }
        }

        // 3. 构造锁 Key: QTalk:Idempotent:{userId}:{requestId}
        String lockKey = RedisConstant.IDEMPOTENT_KEY + userId + ":" + requestId;

        // 4. 尝试获取锁
        // waitTime = 0 表示不等待，立即返回结果
        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;
        try {
            // 尝试获取锁，最多等待 0 秒，最多持有锁 10 秒
            isLocked = lock.tryLock(0, idempotent.expire(), idempotent.timeUnit());
            if (!isLocked) {
                log.warn("重复请求拦截: {}", lockKey);
                // 使用 ResponseCode 中定义的 DUPLICATE_REQUEST
                throw new QAException(idempotent.message(), ResponseCode.DUPLICATE_REQUEST.getCode());
            }

            // 5. 执行业务
            return joinPoint.proceed();

        } finally {
//             if (isLocked && lock.isHeldByCurrentThread()) {
//                 lock.unlock();
//             }
        }
    }
}
