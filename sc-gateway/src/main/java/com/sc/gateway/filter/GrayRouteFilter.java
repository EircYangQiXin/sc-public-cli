package com.sc.gateway.filter;

import com.sc.gateway.config.GrayProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 灰度路由全局过滤器
 * <p>
 * 安全策略:
 * <ul>
 *   <li>HMAC 签名校验: X-Gray-Sign = HmacSHA256(serviceId + ":" + timestamp, signSecret)</li>
 *   <li>时间戳防重放: X-Gray-Timestamp 与服务器时间差超过 5 分钟则拒绝</li>
 *   <li>来源网段约束: 支持 CIDR 匹配（如 10.0.0.0/8）</li>
 *   <li>路由替换时保留 query string</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
public class GrayRouteFilter implements GlobalFilter, Ordered {

    private static final String GRAY_HEADER = "X-Gray-Tag";
    private static final String GRAY_SIGN_HEADER = "X-Gray-Sign";
    private static final String GRAY_TIMESTAMP_HEADER = "X-Gray-Timestamp";
    private static final String GRAY_VALUE = "gray";
    private static final String GRAY_METADATA_KEY = "gray";
    /** 允许的时间偏差（毫秒）: 5 分钟 */
    private static final long TIMESTAMP_TOLERANCE_MS = 5 * 60 * 1000L;

    private final DiscoveryClient discoveryClient;
    private final GrayProperties grayProperties;

    public GrayRouteFilter(DiscoveryClient discoveryClient, GrayProperties grayProperties) {
        this.discoveryClient = discoveryClient;
        this.grayProperties = grayProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!grayProperties.isEnabled()) {
            return chain.filter(exchange);
        }

        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if (route == null) {
            return chain.filter(exchange);
        }

        URI uri = route.getUri();
        String serviceId = uri.getHost();
        if (serviceId == null || !"lb".equalsIgnoreCase(uri.getScheme())) {
            return chain.filter(exchange);
        }

        boolean isGray = isValidGrayRequest(exchange, serviceId);

        List<ServiceInstance> allInstances = discoveryClient.getInstances(serviceId);
        if (allInstances == null || allInstances.isEmpty()) {
            return chain.filter(exchange);
        }

        ServiceInstance chosen = chooseInstance(allInstances, isGray);
        if (chosen == null) {
            return chain.filter(exchange);
        }

        // 替换目标 URI，保留 path + query string
        URI requestUri = exchange.getRequest().getURI();
        String rawPath = requestUri.getRawPath();
        String rawQuery = requestUri.getRawQuery();
        String targetUrl = "http://" + chosen.getHost() + ":" + chosen.getPort() + rawPath;
        if (rawQuery != null && !rawQuery.isEmpty()) {
            targetUrl = targetUrl + "?" + rawQuery;
        }

        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, URI.create(targetUrl));

        if (isGray) {
            log.debug("灰度路由: {} → {}:{}", serviceId, chosen.getHost(), chosen.getPort());
        }

        return chain.filter(exchange);
    }

    /**
     * 校验灰度请求的合法性
     * <p>
     * 策略优先级:
     * <ol>
     *   <li>有 signSecret → HMAC 签名校验 + 时间戳防重放</li>
     *   <li>无 signSecret 有 allowedSources → 来源网段 CIDR 校验</li>
     *   <li>都没配 → 不信任灰度标记</li>
     * </ol>
     * </p>
     */
    private boolean isValidGrayRequest(ServerWebExchange exchange, String serviceId) {
        String grayTag = exchange.getRequest().getHeaders().getFirst(GRAY_HEADER);
        if (!GRAY_VALUE.equalsIgnoreCase(grayTag)) {
            return false;
        }

        String signSecret = grayProperties.getSignSecret();

        // 策略 1: HMAC 签名校验
        if (signSecret != null && !signSecret.isEmpty()) {
            return verifyHmacSign(exchange, serviceId, signSecret);
        }

        // 策略 2: 来源网段 CIDR 校验
        List<String> allowedSources = grayProperties.getAllowedSources();
        if (allowedSources != null && !allowedSources.isEmpty()) {
            String clientIp = getDirectIp(exchange);
            if (matchIpList(clientIp, allowedSources)) {
                return true;
            }
            log.warn("灰度请求来源不在允许列表: ip={}", clientIp);
            return false;
        }

        log.warn("灰度路由未配置安全策略(signSecret 或 allowedSources)，忽略灰度标记");
        return false;
    }

    /**
     * HMAC-SHA256 签名校验 + 时间戳防重放
     * <p>
     * 客户端签名方式:
     * <pre>
     * timestamp = System.currentTimeMillis()
     * plainText = serviceId + ":" + timestamp
     * sign = Hex(HmacSHA256(plainText, signSecret))
     *
     * Headers:
     *   X-Gray-Tag: gray
     *   X-Gray-Timestamp: {timestamp}
     *   X-Gray-Sign: {sign}
     * </pre>
     * </p>
     */
    private boolean verifyHmacSign(ServerWebExchange exchange, String serviceId, String signSecret) {
        String sign = exchange.getRequest().getHeaders().getFirst(GRAY_SIGN_HEADER);
        String timestampStr = exchange.getRequest().getHeaders().getFirst(GRAY_TIMESTAMP_HEADER);

        if (sign == null || sign.isEmpty() || timestampStr == null || timestampStr.isEmpty()) {
            log.warn("灰度签名或时间戳缺失");
            return false;
        }

        // 时间戳防重放
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            log.warn("灰度时间戳格式错误: {}", timestampStr);
            return false;
        }

        long diff = Math.abs(System.currentTimeMillis() - timestamp);
        if (diff > TIMESTAMP_TOLERANCE_MS) {
            log.warn("灰度时间戳偏差过大: {}ms", diff);
            return false;
        }

        // HMAC-SHA256 签名校验
        String expectedSign = hmacSha256(serviceId + ":" + timestampStr, signSecret);
        if (expectedSign == null || !expectedSign.equals(sign)) {
            log.warn("灰度 HMAC 签名校验失败");
            return false;
        }

        return true;
    }

    /**
     * 计算 HMAC-SHA256 并返回十六进制字符串
     */
    private String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("HMAC-SHA256 计算失败", e);
            return null;
        }
    }

    /**
     * 检查 IP 是否在列表中（支持精确匹配和 CIDR）
     */
    private boolean matchIpList(String clientIp, List<String> ipList) {
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

    /**
     * CIDR 匹配
     */
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

    private String getDirectIp(ServerWebExchange exchange) {
        InetSocketAddress addr = exchange.getRequest().getRemoteAddress();
        return addr != null ? addr.getAddress().getHostAddress() : "unknown";
    }

    private ServiceInstance chooseInstance(List<ServiceInstance> instances, boolean preferGray) {
        List<ServiceInstance> candidates = new ArrayList<ServiceInstance>();
        List<ServiceInstance> others = new ArrayList<ServiceInstance>();

        for (ServiceInstance inst : instances) {
            Map<String, String> metadata = inst.getMetadata();
            boolean isGrayInstance = metadata != null
                    && "true".equalsIgnoreCase(metadata.get(GRAY_METADATA_KEY));

            if (preferGray && isGrayInstance) {
                candidates.add(inst);
            } else if (!preferGray && !isGrayInstance) {
                candidates.add(inst);
            } else {
                others.add(inst);
            }
        }

        if (candidates.isEmpty()) {
            candidates = others;
        }

        if (candidates.isEmpty()) {
            return null;
        }

        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
