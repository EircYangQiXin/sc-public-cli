package com.sc.auth.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.sc.api.system.RemoteUserService;
import com.sc.api.system.dto.SysUserDTO;
import com.sc.common.core.constant.Constants;
import com.sc.common.core.domain.R;
import com.sc.common.core.domain.model.LoginUser;
import com.sc.common.core.exception.ServiceException;
import com.sc.common.core.utils.PasswordUtils;
import com.sc.common.log.event.LoginLogEvent;
import com.sc.auth.domain.vo.LoginTokenVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 登录服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final RemoteUserService remoteUserService;
    private final ApplicationEventPublisher eventPublisher;
    private final HttpServletRequest request;

    /**
     * 用户登录
     */
    public LoginTokenVO login(String username, String password) {
        // 1. 远程调用获取用户信息
        R<SysUserDTO> result = remoteUserService.getUserInfo(username);
        if (!result.isSuccess() || result.getData() == null) {
            publishLoginLog(username, 1, "用户不存在或服务异常");
            throw new ServiceException("用户不存在或服务异常");
        }

        SysUserDTO user = result.getData();

        // 2. 校验用户状态
        if (Constants.DISABLE.equals(user.getStatus())) {
            publishLoginLog(username, 1, "用户已被停用");
            throw new ServiceException("用户已被停用");
        }

        // 3. BCrypt 密码比对
        if (!PasswordUtils.matches(password, user.getPassword())) {
            publishLoginLog(username, 1, "账号或密码错误");
            throw new ServiceException("账号或密码错误");
        }

        // 4. Sa-Token 登录
        StpUtil.login(user.getUserId());

        // 5. 构建 LoginUser 并存入 Session
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

        // 6. 记录登录成功日志
        publishLoginLog(username, 0, "登录成功");

        // 7. 返回 Token 信息
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        LoginTokenVO tokenVO = new LoginTokenVO();
        tokenVO.setAccessToken(tokenInfo.getTokenValue());
        tokenVO.setExpiresIn(tokenInfo.getTokenTimeout());

        log.info("用户登录成功: {}", username);
        return tokenVO;
    }

    /**
     * 用户退出
     */
    public void logout() {
        try {
            String username = (String) StpUtil.getSession().get("loginUser") != null
                    ? ((LoginUser) StpUtil.getSession().get("loginUser")).getUsername()
                    : "unknown";
            StpUtil.logout();
            publishLoginLog(username, 0, "退出成功");
        } catch (Exception e) {
            // ignore
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
     * 发布登录日志事件
     */
    private void publishLoginLog(String username, int status, String msg) {
        try {
            LoginLogEvent event = new LoginLogEvent();
            event.setUsername(username);
            event.setStatus(status);
            event.setMsg(msg);
            event.setLoginTime(LocalDateTime.now());

            // 从请求中获取 IP 和 User-Agent
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
