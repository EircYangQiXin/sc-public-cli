package com.sc.auth.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 设备信任服务
 * <p>
 * 已信任的设备在有效期内免 MFA 验证。
 * 设备指纹由客户端生成（User-Agent + 屏幕 + 时区等的 hash）。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTrustService {

    private static final String TRUST_KEY_PREFIX = "device:trust:";

    private final StringRedisTemplate redisTemplate;
    private final SecurityProperties securityProperties;

    /**
     * 标记设备为可信
     */
    public void trustDevice(Long userId, String deviceFingerprint) {
        String key = TRUST_KEY_PREFIX + userId;
        int days = securityProperties.getDeviceTrust().getTrustDays();
        redisTemplate.opsForSet().add(key, deviceFingerprint);
        redisTemplate.expire(key, days, TimeUnit.DAYS);
        log.info("设备已标记为可信: userId={}, fingerprint={}", userId, maskFingerprint(deviceFingerprint));
    }

    /**
     * 检查设备是否可信
     */
    public boolean isTrustedDevice(Long userId, String deviceFingerprint) {
        if (deviceFingerprint == null || deviceFingerprint.isEmpty()) {
            return false;
        }
        String key = TRUST_KEY_PREFIX + userId;
        Boolean isMember = redisTemplate.opsForSet().isMember(key, deviceFingerprint);
        return Boolean.TRUE.equals(isMember);
    }

    /**
     * 撤销设备信任
     */
    public void removeTrustedDevice(Long userId, String deviceFingerprint) {
        String key = TRUST_KEY_PREFIX + userId;
        redisTemplate.opsForSet().remove(key, deviceFingerprint);
        log.info("设备信任已撤销: userId={}", userId);
    }

    /**
     * 撤销用户所有设备信任
     */
    public void removeAllTrustedDevices(Long userId) {
        redisTemplate.delete(TRUST_KEY_PREFIX + userId);
        log.info("已撤销用户所有设备信任: userId={}", userId);
    }

    private String maskFingerprint(String fp) {
        if (fp == null || fp.length() <= 8) return "***";
        return fp.substring(0, 4) + "***" + fp.substring(fp.length() - 4);
    }
}
