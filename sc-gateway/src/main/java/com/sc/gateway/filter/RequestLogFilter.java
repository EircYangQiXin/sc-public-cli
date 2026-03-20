package com.sc.gateway.filter;

import brave.Span;
import brave.Tracer;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 全局请求日志过滤器
 * <p>
 * 功能：
 * 1. 记录请求入口和响应/异常日志（含 traceId）
 * 2. 将 traceId 写入下游请求头和 HTTP 响应头 {@code X-Trace-Id}
 * <p>
 * 注意事项：
 * - traceId 惰性读取，避免 Reactor 链早期 span 尚未绑定导致空值
 * - 使用 {@code response.beforeCommit} 写响应头，确保在 response 提交前完成
 * - 使用 {@code doFinally} 记录结束日志，覆盖正常完成、异常、取消等所有场景
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestLogFilter implements GlobalFilter, Ordered {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String START_TIME_ATTR = "sc-gateway-start-time";

    private final Tracer tracer;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethodValue();

        // 记录请求开始时间
        exchange.getAttributes().put(START_TIME_ATTR, System.currentTimeMillis());

        log.info("Gateway Request: [{}] {} {}", resolveTraceId(), method, path);

        // 在 response 提交前写入 X-Trace-Id 响应头
        ServerHttpResponse response = exchange.getResponse();
        response.beforeCommit(() -> {
            String traceId = resolveTraceId();
            if (StrUtil.isNotBlank(traceId)) {
                HttpHeaders headers = response.getHeaders();
                if (!headers.containsKey(TRACE_ID_HEADER)) {
                    headers.add(TRACE_ID_HEADER, traceId);
                }
            }
            return Mono.empty();
        });

        // 将 traceId 写入下游请求头（惰性获取，仅非空时写入）
        String currentTraceId = resolveTraceId();
        ServerWebExchange mutatedExchange = exchange;
        if (StrUtil.isNotBlank(currentTraceId)) {
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(TRACE_ID_HEADER, currentTraceId)
                    .build();
            mutatedExchange = exchange.mutate().request(mutatedRequest).build();
        }

        return chain.filter(mutatedExchange)
                .doOnError(throwable -> {
                    long duration = calculateDuration(exchange);
                    log.error("Gateway Error: [{}] {} {} ({}ms) - {}",
                            resolveTraceId(), method, path, duration, throwable.getMessage());
                })
                .doFinally(signalType -> {
                    long duration = calculateDuration(exchange);
                    int statusCode = response.getStatusCode() != null
                            ? response.getStatusCode().value() : 0;
                    log.info("Gateway Response: [{}] {} {} -> {} ({}ms) signal={}",
                            resolveTraceId(), method, path, statusCode, duration, signalType);
                });
    }

    /**
     * 惰性获取当前 traceId，避免过早固化空值
     */
    private String resolveTraceId() {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            return currentSpan.context().traceIdString();
        }
        return "";
    }

    /**
     * 计算请求耗时
     */
    private long calculateDuration(ServerWebExchange exchange) {
        Long startTime = exchange.getAttribute(START_TIME_ATTR);
        if (startTime != null) {
            return System.currentTimeMillis() - startTime;
        }
        return -1;
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
