package com.sc.auth.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 安全策略配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "sc.security")
public class SecurityProperties {

    /** 账号锁定配置 */
    private Lock lock = new Lock();

    /** 密码策略配置 */
    private Password password = new Password();

    /** MFA 配置 */
    private Mfa mfa = new Mfa();

    /** 设备信任配置 */
    private DeviceTrust deviceTrust = new DeviceTrust();

    @Data
    public static class Lock {
        /** 最大失败次数 */
        private int maxAttempts = 5;
        /** 锁定时长（分钟） */
        private int lockMinutes = 30;
    }

    @Data
    public static class Password {
        /** 最小长度 */
        private int minLength = 8;
        /** 是否要求包含大写字母 */
        private boolean requireUppercase = true;
        /** 是否要求包含小写字母 */
        private boolean requireLowercase = true;
        /** 是否要求包含数字 */
        private boolean requireDigit = true;
        /** 是否要求包含特殊字符 */
        private boolean requireSpecialChar = true;
        /** 密码过期天数 (0=不过期) */
        private int expireDays = 90;
    }

    @Data
    public static class Mfa {
        /** 是否全局强制启用 MFA */
        private boolean forceEnabled = false;
        /** MFA 临时 Token 有效期（秒） */
        private int tokenExpireSeconds = 300;
        /** TOTP 发行者名称 */
        private String issuer = "SC-CLI";
    }

    @Data
    public static class DeviceTrust {
        /** 设备信任有效期（天） */
        private int trustDays = 30;
    }
}
