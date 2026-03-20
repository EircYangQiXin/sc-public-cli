package com.sc.common.security.feign;

import cn.dev33.satoken.same.SaSameUtil;
import cn.dev33.satoken.stp.StpUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

/**
 * Feign 请求拦截器 - 自动透传 Token
 */
@Component
public class FeignTokenInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // 透传用户 Token
        try {
            String tokenValue = StpUtil.getTokenValue();
            if (tokenValue != null) {
                template.header(StpUtil.getTokenName(), tokenValue);
            }
        } catch (Exception e) {
            // 未登录时忽略
        }

        // 添加内部调用标识 (Sa-Token Same-Token)
        template.header(SaSameUtil.SAME_TOKEN, SaSameUtil.getToken());

        // 添加内部 Feign 调用标识（供服务端接口校验）
        template.header("X-SC-Internal", "sc-internal-feign");
    }
}
