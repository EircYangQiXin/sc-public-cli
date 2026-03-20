package com.sc.gateway.config;

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Sa-Token 网关鉴权配置
 */
@Slf4j
@Configuration
public class SaTokenConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                // 拦截所有路径
                .addInclude("/**")
                // 排除的路径（公开接口、文档、健康检查）
                .addExclude(
                        "/auth/login",
                        "/auth/logout",
                        "/auth/captcha",
                        "/auth/mfa/verify",
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v2/api-docs/**",
                        "/actuator/**",
                        "/favicon.ico"
                )
                // 鉴权逻辑
                .setAuth(obj -> {
                    // 剥离外部请求中的内部调用标识头（防止伪造）
                    SaRouter.match("/**", r -> StpUtil.checkLogin());
                })
                // 异常处理 - 返回标准 JSON
                .setError(e -> {
                    Map<String, Object> result = new HashMap<>(4);
                    result.put("code", 401);
                    result.put("msg", "未登录或Token已过期");
                    result.put("data", null);
                    try {
                        return objectMapper.writeValueAsString(result);
                    } catch (Exception ex) {
                        return "{\"code\":401,\"msg\":\"未登录或Token已过期\",\"data\":null}";
                    }
                });
    }
}
