package com.sc.common.security.handler;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.sc.common.core.context.SecurityContextHolder;
import com.sc.common.core.domain.model.LoginUser;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 安全拦截器 - 将登录用户信息注入到线程上下文
 */
@Component
public class SecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            if (StpUtil.isLogin()) {
                SaSession session = StpUtil.getSession();
                LoginUser loginUser = session.getModel("loginUser", LoginUser.class);
                if (loginUser != null) {
                    SecurityContextHolder.set("loginUser", loginUser);
                    SecurityContextHolder.setUserId(loginUser.getUserId());
                    SecurityContextHolder.setUsername(loginUser.getUsername());
                }
            }
        } catch (Exception e) {
            // 未登录时忽略
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        SecurityContextHolder.remove();
    }
}
