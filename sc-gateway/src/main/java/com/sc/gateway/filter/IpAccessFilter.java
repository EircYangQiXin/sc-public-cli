package com.sc.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sc.gateway.config.IpAccessProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IP 黑白名单全局过滤器
 * <p>
 * 安全策略:
 * <ul>
 *   <li><b>默认模式</b>: 始终优先取 TCP 层 remoteAddress（不可伪造）</li>
 *   <li><b>可信代理模式</b>: 仅当 {@code trustProxy=true} 且 remoteAddress 在
 *       {@code trustedProxies} 列表中时，才读取 X-Forwarded-For/X-Real-IP</li>
 * </ul>
 * 这防止了公网客户端通过伪造请求头绕过 IP 访问控制。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IpAccessFilter implements GlobalFilter, Ordered {

    private final IpAccessProperties ipAccessProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!ipAccessProperties.isEnabled()) {
            return chain.filter(exchange);
        }

        String clientIp = getClientIp(exchange.getRequest());
        String mode = ipAccessProperties.getMode();

        boolean blocked = false;

        if ("blacklist".equalsIgnoreCase(mode)) {
            blocked = matchIpList(clientIp, ipAccessProperties.getBlacklist());
        } else if ("whitelist".equalsIgnoreCase(mode)) {
            blocked = !matchIpList(clientIp, ipAccessProperties.getWhitelist());
        }

        if (blocked) {
            log.warn("IP 访问被拦截: {} (mode={})", clientIp, mode);
            return forbidden(exchange.getResponse());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -200;
    }

    /**
     * 安全获取客户端 IP
     * <p>
     * 默认取 TCP remoteAddress（不可伪造）。
     * 仅在启用可信代理且 remoteAddress 属于可信代理时，才读取转发头。
     * </p>
     */
    private String getClientIp(ServerHttpRequest request) {
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        String directIp = remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";

        // 只有在启用可信代理模式、并且直连 IP 在可信代理列表中时，才信任转发头
        if (ipAccessProperties.isTrustProxy() && matchIpList(directIp, ipAccessProperties.getTrustedProxies())) {
            // 信任 X-Forwarded-For (取最左端，即客户端原始 IP)
            String xff = request.getHeaders().getFirst("X-Forwarded-For");
            if (xff != null && !xff.isEmpty() && !"unknown".equalsIgnoreCase(xff)) {
                int idx = xff.indexOf(',');
                return idx > 0 ? xff.substring(0, idx).trim() : xff.trim();
            }

            String xri = request.getHeaders().getFirst("X-Real-IP");
            if (xri != null && !xri.isEmpty() && !"unknown".equalsIgnoreCase(xri)) {
                return xri.trim();
            }
        }

        return directIp;
    }

    private boolean matchIpList(String clientIp, List<String> ipList) {
        if (ipList == null || ipList.isEmpty()) {
            return false;
        }
        for (String pattern : ipList) {
            if (pattern.contains("/")) {
                if (matchCidr(clientIp, pattern)) {
                    return true;
                }
            } else {
                if (pattern.equals(clientIp)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchCidr(String ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            if (parts.length != 2) {
                return false;
            }
            InetAddress cidrAddress = InetAddress.getByName(parts[0]);
            int prefixLength = Integer.parseInt(parts[1]);

            byte[] cidrBytes = cidrAddress.getAddress();
            byte[] ipBytes = InetAddress.getByName(ip).getAddress();

            if (cidrBytes.length != ipBytes.length) {
                return false;
            }

            int fullBytes = prefixLength / 8;
            int remainBits = prefixLength % 8;

            for (int i = 0; i < fullBytes; i++) {
                if (cidrBytes[i] != ipBytes[i]) {
                    return false;
                }
            }

            if (remainBits > 0 && fullBytes < cidrBytes.length) {
                int mask = 0xFF << (8 - remainBits);
                return (cidrBytes[fullBytes] & mask) == (ipBytes[fullBytes] & mask);
            }

            return true;
        } catch (UnknownHostException | NumberFormatException e) {
            log.warn("CIDR 解析失败: {}", cidr, e);
            return false;
        }
    }

    private Mono<Void> forbidden(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> result = new HashMap<String, Object>(4);
        result.put("code", HttpStatus.FORBIDDEN.value());
        result.put("msg", "您的 IP 地址已被限制访问");
        result.put("data", null);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(result);
        } catch (JsonProcessingException e) {
            bytes = "{\"code\":403,\"msg\":\"IP 访问受限\",\"data\":null}".getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
