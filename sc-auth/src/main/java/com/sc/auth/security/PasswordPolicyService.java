package com.sc.auth.security;

import com.sc.common.core.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 密码策略服务
 */
@Service
@RequiredArgsConstructor
public class PasswordPolicyService {

    private final SecurityProperties securityProperties;

    /**
     * 校验密码强度，不满足策略则抛出异常
     */
    public void validate(String password) {
        SecurityProperties.Password policy = securityProperties.getPassword();
        List<String> violations = new ArrayList<String>();

        if (password == null || password.length() < policy.getMinLength()) {
            violations.add("密码长度不能少于" + policy.getMinLength() + "位");
        }

        if (password != null) {
            if (policy.isRequireUppercase() && !containsUppercase(password)) {
                violations.add("必须包含大写字母");
            }
            if (policy.isRequireLowercase() && !containsLowercase(password)) {
                violations.add("必须包含小写字母");
            }
            if (policy.isRequireDigit() && !containsDigit(password)) {
                violations.add("必须包含数字");
            }
            if (policy.isRequireSpecialChar() && !containsSpecialChar(password)) {
                violations.add("必须包含特殊字符(!@#$%^&*等)");
            }
        }

        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder("密码不满足安全策略: ");
            for (int i = 0; i < violations.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(violations.get(i));
            }
            throw new ServiceException(sb.toString());
        }
    }

    /**
     * 检查密码是否过期
     *
     * @param passwordUpdateTime 密码最后修改时间
     * @return true=已过期
     */
    public boolean isExpired(LocalDateTime passwordUpdateTime) {
        int expireDays = securityProperties.getPassword().getExpireDays();
        if (expireDays <= 0 || passwordUpdateTime == null) {
            return false;
        }
        long daysSinceUpdate = ChronoUnit.DAYS.between(passwordUpdateTime, LocalDateTime.now());
        return daysSinceUpdate >= expireDays;
    }

    private boolean containsUppercase(String s) {
        for (char c : s.toCharArray()) {
            if (Character.isUpperCase(c)) return true;
        }
        return false;
    }

    private boolean containsLowercase(String s) {
        for (char c : s.toCharArray()) {
            if (Character.isLowerCase(c)) return true;
        }
        return false;
    }

    private boolean containsDigit(String s) {
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c)) return true;
        }
        return false;
    }

    private boolean containsSpecialChar(String s) {
        String special = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
        for (char c : s.toCharArray()) {
            if (special.indexOf(c) >= 0) return true;
        }
        return false;
    }
}
