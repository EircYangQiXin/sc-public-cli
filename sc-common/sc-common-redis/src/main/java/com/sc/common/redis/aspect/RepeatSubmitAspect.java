package com.sc.common.redis.aspect;

import com.sc.common.core.context.SecurityContextHolder;
import com.sc.common.core.exception.ServiceException;
import com.sc.common.redis.annotation.RepeatSubmit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 防重复提交 AOP 切面
 * <p>
 * 原理：基于 Redis 的 SETNX（SET if Not eXists）实现。
 * 以 "用户标识 + 请求URI + 请求方法" 为 Key，
 * 在指定时间窗口内只允许一次通过，后续请求直接拦截。
 * </p>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnWebApplication
public class RepeatSubmitAspect {

    private static final String REPEAT_SUBMIT_KEY_PREFIX = "repeat_submit:";

    private final StringRedisTemplate redisTemplate;

    @Before("@annotation(repeatSubmit)")
    public void doBefore(JoinPoint joinPoint, RepeatSubmit repeatSubmit) {
        String key = buildKey(joinPoint);

        // SETNX：如果 Key 不存在则设置（返回 true），否则返回 false
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", repeatSubmit.interval(), TimeUnit.SECONDS);

        if (success == null || !success) {
            throw new ServiceException(repeatSubmit.message());
        }
    }

    /**
     * 构建防重复提交 Key
     * 格式：repeat_submit:{userId}:{httpMethod}:{requestURI}
     */
    private String buildKey(JoinPoint joinPoint) {
        StringBuilder sb = new StringBuilder(REPEAT_SUBMIT_KEY_PREFIX);

        // 用户标识
        Long userId = SecurityContextHolder.getUserId();
        if (userId != null) {
            sb.append(userId);
        } else {
            // 未登录场景使用 SessionId 或 IP
            HttpServletRequest request = getRequest();
            if (request != null) {
                sb.append(request.getRemoteAddr());
            }
        }

        sb.append(":");

        // 请求信息
        HttpServletRequest request = getRequest();
        if (request != null) {
            sb.append(request.getMethod()).append(":").append(request.getRequestURI());
        } else {
            // 降级为方法签名
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            sb.append(method.getDeclaringClass().getName()).append(".").append(method.getName());
        }

        return sb.toString();
    }

    private HttpServletRequest getRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) attributes).getRequest();
        }
        return null;
    }
}
