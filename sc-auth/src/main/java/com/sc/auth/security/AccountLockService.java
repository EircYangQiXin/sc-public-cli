package com.sc.auth.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 账号锁定服务
 * <p>
 * 基于 Redis 计数器实现登录失败锁定：
 * <ul>
 *   <li>连续失败 N 次后锁定账号 M 分钟</li>
 *   <li>登录成功后重置计数</li>
 *   <li>管理员可手动解锁</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountLockService {

    private static final String LOCK_KEY_PREFIX = "account:lock:";
    private static final String ATTEMPTS_KEY_PREFIX = "account:attempts:";

    private final StringRedisTemplate redisTemplate;
    private final SecurityProperties securityProperties;

    /**
     * 检查账号是否被锁定
     */
    public boolean isLocked(String username) {
        Boolean exists = redisTemplate.hasKey(LOCK_KEY_PREFIX + username);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 获取剩余锁定时间（秒），未锁定返回 0
     */
    public long getRemainingLockSeconds(String username) {
        Long ttl = redisTemplate.getExpire(LOCK_KEY_PREFIX + username, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : 0;
    }

    /**
     * 记录登录失败并在达到阈值时锁定
     *
     * @return 当前连续失败次数
     */
    public int recordFailedAttempt(String username) {
        String attemptsKey = ATTEMPTS_KEY_PREFIX + username;
        Long count = redisTemplate.opsForValue().increment(attemptsKey);
        int attempts = count != null ? count.intValue() : 1;

        // 首次设置过期时间（与锁定时长一致，自然窗口）
        if (attempts == 1) {
            redisTemplate.expire(attemptsKey, securityProperties.getLock().getLockMinutes(), TimeUnit.MINUTES);
        }

        int maxAttempts = securityProperties.getLock().getMaxAttempts();
        if (attempts >= maxAttempts) {
            // 锁定账号
            int lockMinutes = securityProperties.getLock().getLockMinutes();
            redisTemplate.opsForValue().set(LOCK_KEY_PREFIX + username,
                    String.valueOf(System.currentTimeMillis()),
                    lockMinutes, TimeUnit.MINUTES);
            log.warn("账号已锁定: username={}, 失败次数={}, 锁定{}分钟", username, attempts, lockMinutes);
        }

        return attempts;
    }

    /**
     * 登录成功后重置失败计数
     */
    public void resetAttempts(String username) {
        redisTemplate.delete(ATTEMPTS_KEY_PREFIX + username);
        redisTemplate.delete(LOCK_KEY_PREFIX + username);
    }

    /**
     * 管理员手动解锁
     */
    public void unlock(String username) {
        redisTemplate.delete(LOCK_KEY_PREFIX + username);
        redisTemplate.delete(ATTEMPTS_KEY_PREFIX + username);
        log.info("管理员手动解锁账号: {}", username);
    }

    /**
     * 获取当前失败次数
     */
    public int getFailedAttempts(String username) {
        String val = redisTemplate.opsForValue().get(ATTEMPTS_KEY_PREFIX + username);
        return val != null ? Integer.parseInt(val) : 0;
    }
}
