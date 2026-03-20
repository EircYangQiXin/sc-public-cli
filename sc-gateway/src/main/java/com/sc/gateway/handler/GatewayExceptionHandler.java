package com.sc.gateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一网关异常处理器
 * <p>
 * 捕获网关层面所有未处理异常，返回标准 JSON 格式响应。
 * 覆盖场景：路由找不到(404)、服务不可用(503)、连接超时、未知异常等。
 * </p>
 */
@Slf4j
@Order(-1)
@Component
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        // 已经提交的响应不再处理
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        int code;
        String msg;

        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            HttpStatus status = rse.getStatus();
            code = status.value();
            switch (status) {
                case NOT_FOUND:
                    msg = "服务未找到，请检查请求路径";
                    break;
                case BAD_GATEWAY:
                    msg = "网关错误，后端服务异常";
                    break;
                case SERVICE_UNAVAILABLE:
                    msg = "服务暂时不可用，请稍后重试";
                    break;
                case GATEWAY_TIMEOUT:
                    msg = "网关超时，后端服务响应过慢";
                    break;
                default:
                    msg = status.getReasonPhrase();
            }
        } else if (ex.getMessage() != null && ex.getMessage().contains("Connection refused")) {
            code = HttpStatus.SERVICE_UNAVAILABLE.value();
            msg = "目标服务不可用，请检查服务是否启动";
        } else {
            code = HttpStatus.INTERNAL_SERVER_ERROR.value();
            msg = "系统内部错误";
        }

        log.error("Gateway Exception: {} {} → [{}] {}",
                exchange.getRequest().getMethodValue(),
                exchange.getRequest().getURI().getPath(),
                code, ex.getMessage(), ex);

        response.setStatusCode(HttpStatus.valueOf(code));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> result = new HashMap<>(4);
        result.put("code", code);
        result.put("msg", msg);
        result.put("data", null);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(result);
        } catch (JsonProcessingException e) {
            bytes = ("{\"code\":" + code + ",\"msg\":\"" + msg + "\",\"data\":null}")
                    .getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
