package com.sc.gateway.handler;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Sentinel 网关限流/熔断降级统一响应处理器
 * <p>
 * 当请求被 Sentinel 拦截时（限流或熔断降级），返回标准 JSON 响应。
 * </p>
 */
@Component
public class SentinelFallbackHandler implements BlockRequestHandler {

    @Override
    public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable ex) {
        String path = exchange.getRequest().getURI().getPath();

        Map<String, Object> result = new HashMap<>(4);
        result.put("code", HttpStatus.TOO_MANY_REQUESTS.value());
        result.put("msg", "请求过于频繁，请稍后重试");
        result.put("data", null);

        return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(result);
    }
}
