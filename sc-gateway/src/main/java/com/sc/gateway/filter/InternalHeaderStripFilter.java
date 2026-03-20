package com.sc.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关安全过滤器 — 剥离外部请求中的内部调用标识头
 * <p>
 * 防止外部客户端伪造 {@code X-SC-Internal} 头绕过内部接口鉴权。
 * 此过滤器在最高优先级执行，确保请求头被清理后才进入后续过滤链。
 * </p>
 */
@Component
public class InternalHeaderStripFilter implements GlobalFilter, Ordered {

    private static final String INTERNAL_HEADER = "X-SC-Internal";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 剥离外部请求携带的内部标识头
        ServerHttpRequest cleanedRequest = exchange.getRequest().mutate()
                .headers(headers -> headers.remove(INTERNAL_HEADER))
                .build();
        return chain.filter(exchange.mutate().request(cleanedRequest).build());
    }

    @Override
    public int getOrder() {
        // 最高优先级，在所有业务过滤器之前执行
        return -100;
    }
}
