package com.sc.auth.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.sc.api.system.RemoteUserService;
import com.sc.api.system.dto.SysUserDTO;
import com.sc.auth.domain.vo.LoginTokenVO;
import com.sc.auth.security.AccountLockService;
import com.sc.auth.security.DeviceTrustService;
import com.sc.auth.security.PasswordPolicyService;
import com.sc.auth.security.SecurityProperties;
import com.sc.auth.security.TotpService;
import com.sc.common.core.constant.Constants;
import com.sc.common.core.domain.R;
import com.sc.common.core.domain.model.LoginUser;
import com.sc.common.core.exception.ServiceException;
import com.sc.common.core.utils.PasswordUtils;
import com.sc.common.log.event.LoginLogEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 登录服务
 * <p>
 * 登录流程:
 * <ol>
 *   <li>账号锁定检查</li>
 *   <li>远程获取用户信息</li>
 *   <li>用户状态检查</li>
 *   <li>BCrypt 密码校验（失败时累加锁定计数）</li>
 *   <li>MFA 校验（启用时）: 可信设备跳过</li>
 *   <li>Sa-Token 登录 + Session 写入</li>
 *   <li>密码过期检查</li>
 * </ol>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private static final String MFA_TOKEN_PREFIX = "mfa:pending:";

    private final RemoteUserService remoteUserService;
    private final ApplicationEventPublisher eventPublisher;
    private final HttpServletRequest request;
    private final AccountLockService accountLockService;
    private final PasswordPolicyService passwordPolicyService;
    private final TotpService totpService;
    private final DeviceTrustService deviceTrustService;
    private final SecurityProperties securityProperties;
    private final StringRedisTemplate redisTemplate;

    /**
     * 用户登录（完整安全链路）
     */
    public LoginTokenVO login(String username, String password, String mfaCode,
                               String deviceFingerprint, Boolean trustDevice) {
        // 1. 账号锁定检查
        if (accountLockService.isLocked(username)) {
            long remaining = accountLockService.getRemainingLockSeconds(username);
            publishLoginLog(username, 1, "账号已锁定");
            throw new ServiceException("账号已锁定，请" + (remaining / 60 + 1) + "分钟后重试");
        }

        // 2. 远程调用获取用户信息
        R<SysUserDTO> result = remoteUserService.getUserInfo(username);
        if (!result.isSuccess() || result.getData() == null) {
            publishLoginLog(username, 1, "用户不存在或服务异常");
            throw new ServiceException("用户不存在或服务异常");
        }

        SysUserDTO user = result.getData();

        // 3. 校验用户状态
        if (Constants.DISABLE.equals(user.getStatus())) {
            publishLoginLog(username, 1, "用户已被停用");
            throw new ServiceException("用户已被停用");
        }

        // 4. BCrypt 密码比对
        if (!PasswordUtils.matches(password, user.getPassword())) {
            int attempts = accountLockService.recordFailedAttempt(username);
            int max = securityProperties.getLock().getMaxAttempts();
            int remaining = max - attempts;
            publishLoginLog(username, 1, "账号或密码错误");
            if (remaining > 0) {
                throw new ServiceException("账号或密码错误，还可尝试" + remaining + "次");
            } else {
                throw new ServiceException("账号或密码错误，账号已被锁定" + securityProperties.getLock().getLockMinutes() + "分钟");
            }
        }

        // 密码正确，但不立即 reset — 等 MFA 也通过后在 doLogin 中 reset

        // 5. MFA 校验
        boolean mfaEnabled = Integer.valueOf(1).equals(user.getMfaEnabled())
                || securityProperties.getMfa().isForceEnabled();

        if (mfaEnabled && user.getMfaSecret() != null && !user.getMfaSecret().isEmpty()) {
            // 检查设备信任: 可信设备跳过 MFA
            boolean trusted = deviceFingerprint != null
                    && deviceTrustService.isTrustedDevice(user.getUserId(), deviceFingerprint);

            if (!trusted) {
                // 需要 MFA 验证
                if (mfaCode == null || mfaCode.isEmpty()) {
                    // 未提供 MFA 码 → 返回 mfaRequired + mfaToken
                    String mfaToken = generateMfaToken(user);
                    LoginTokenVO tokenVO = new LoginTokenVO();
                    tokenVO.setMfaRequired(true);
                    tokenVO.setMfaToken(mfaToken);
                    return tokenVO;
                }

                // 验证 TOTP 码
                if (!totpService.verifyCode(user.getMfaSecret(), mfaCode)) {
                    accountLockService.recordFailedAttempt(username);
                    publishLoginLog(username, 1, "MFA 验证失败");
                    throw new ServiceException("MFA 验证码错误");
                }

                // MFA 验证成功，如果请求信任设备
                if (Boolean.TRUE.equals(trustDevice) && deviceFingerprint != null) {
                    deviceTrustService.trustDevice(user.getUserId(), deviceFingerprint);
                }
            }
        }

        // 6. Sa-Token 登录
        return doLogin(user, deviceFingerprint);
    }

    /**
     * MFA 验证（用于两步登录的第二步）
     */
    public LoginTokenVO verifyMfa(String mfaToken, String mfaCode,
                                   String deviceFingerprint, Boolean trustDevice) {
        // 从 Redis 获取临时用户信息
        String userDataKey = MFA_TOKEN_PREFIX + mfaToken;
        String username = redisTemplate.opsForValue().get(userDataKey);
        if (username == null) {
            throw new ServiceException("MFA 令牌已过期，请重新登录");
        }

        // 锁定检查
        if (accountLockService.isLocked(username)) {
            redisTemplate.delete(userDataKey);
            long remaining = accountLockService.getRemainingLockSeconds(username);
            throw new ServiceException("账号已锁定，请" + (remaining / 60 + 1) + "分钟后重试");
        }

        // 重新获取用户信息
        R<SysUserDTO> result = remoteUserService.getUserInfo(username);
        if (!result.isSuccess() || result.getData() == null) {
            throw new ServiceException("用户信息获取失败");
        }
        SysUserDTO user = result.getData();

        // 验证 TOTP 码（失败累加锁定计数，但不销毁 mfaToken 以允许重试）
        if (!totpService.verifyCode(user.getMfaSecret(), mfaCode)) {
            accountLockService.recordFailedAttempt(username);
            publishLoginLog(username, 1, "MFA 验证失败");
            throw new ServiceException("MFA 验证码错误");
        }

        // MFA 验证成功，销毁 mfaToken
        redisTemplate.delete(userDataKey);

        // 信任设备
        if (Boolean.TRUE.equals(trustDevice) && deviceFingerprint != null) {
            deviceTrustService.trustDevice(user.getUserId(), deviceFingerprint);
        }

        publishLoginLog(username, 0, "MFA 验证成功");
        return doLogin(user, deviceFingerprint);
    }

    /**
     * 执行 Sa-Token 登录并返回 Token
     */
    private LoginTokenVO doLogin(SysUserDTO user, String deviceFingerprint) {
        // 全链路校验通过，重置失败计数
        accountLockService.resetAttempts(user.getUsername());

        StpUtil.login(user.getUserId());

        // 构建 LoginUser 存入 Session
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setDeptId(user.getDeptId());
        loginUser.setUsername(user.getUsername());
        loginUser.setNickName(user.getNickName());
        loginUser.setTenantId(user.getTenantId());
        loginUser.setRoles(user.getRoles());
        loginUser.setPermissions(user.getPermissions());
        loginUser.setDataScope(user.getDataScope());
        loginUser.setDataScopeDeptIds(user.getDataScopeDeptIds());
        StpUtil.getSession().set("loginUser", loginUser);

        // 记录登录成功日志
        publishLoginLog(user.getUsername(), 0, "登录成功");

        // 组装返回
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        LoginTokenVO tokenVO = new LoginTokenVO();
        tokenVO.setAccessToken(tokenInfo.getTokenValue());
        tokenVO.setExpiresIn(tokenInfo.getTokenTimeout());
        tokenVO.setMfaRequired(false);

        // 密码过期检查
        if (passwordPolicyService.isExpired(user.getPasswordUpdateTime())) {
            tokenVO.setNeedChangePassword(true);
        }

        log.info("用户登录成功: {}", user.getUsername());
        return tokenVO;
    }

    /**
     * 用户退出
     */
    public void logout() {
        try {
            Object loginUserObj = StpUtil.getSession().get("loginUser");
            String username = "unknown";
            if (loginUserObj instanceof LoginUser) {
                username = ((LoginUser) loginUserObj).getUsername();
            }
            StpUtil.logout();
            publishLoginLog(username, 0, "退出成功");
        } catch (Exception e) {
            log.warn("用户退出处理异常", e);
        }
    }

    /**
     * 刷新 Token
     */
    public LoginTokenVO refreshToken() {
        StpUtil.checkLogin();
        StpUtil.renewTimeout(1800);

        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        LoginTokenVO tokenVO = new LoginTokenVO();
        tokenVO.setAccessToken(tokenInfo.getTokenValue());
        tokenVO.setExpiresIn(tokenInfo.getTokenTimeout());
        return tokenVO;
    }

    /**
     * 生成 MFA 临时 Token
     */
    private String generateMfaToken(SysUserDTO user) {
        String mfaToken = UUID.randomUUID().toString().replace("-", "");
        int expireSeconds = securityProperties.getMfa().getTokenExpireSeconds();
        redisTemplate.opsForValue().set(MFA_TOKEN_PREFIX + mfaToken,
                user.getUsername(), expireSeconds, TimeUnit.SECONDS);
        return mfaToken;
    }

    /**
     * 发布登录日志事件
     */
    private void publishLoginLog(String username, int status, String msg) {
        try {
            LoginLogEvent event = new LoginLogEvent();
            event.setUsername(username);
            event.setStatus(status);
            event.setMsg(msg);
            event.setLoginTime(LocalDateTime.now());

            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            event.setIpAddr(ip);

            String userAgent = request.getHeader("User-Agent");
            if (userAgent != null) {
                event.setBrowser(parseBrowser(userAgent));
                event.setOs(parseOs(userAgent));
            }

            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.error("发布登录日志事件失败", e);
        }
    }

    private String parseBrowser(String userAgent) {
        if (userAgent.contains("Chrome")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari")) return "Safari";
        if (userAgent.contains("Edge")) return "Edge";
        if (userAgent.contains("MSIE") || userAgent.contains("Trident")) return "IE";
        return "Unknown";
    }

    private String parseOs(String userAgent) {
        if (userAgent.contains("Windows")) return "Windows";
        if (userAgent.contains("Mac")) return "macOS";
        if (userAgent.contains("Linux")) return "Linux";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) return "iOS";
        return "Unknown";
    }
}
