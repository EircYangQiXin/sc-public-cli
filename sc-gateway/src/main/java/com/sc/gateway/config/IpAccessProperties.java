package com.sc.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * IP 黑白名单配置
 * <p>
 * 配置示例：
 * <pre>
 * sc:
 *   gateway:
 *     ip-access:
 *       enabled: true
 *       mode: blacklist
 *       trust-proxy: true                  # 是否信任代理转发头
 *       trusted-proxies:                    # 可信代理 IP（仅 trust-proxy=true 时生效）
 *         - 10.0.0.0/8
 *         - 172.16.0.0/12
 *       blacklist:
 *         - 192.168.1.100
 *         - 10.0.0.0/8
 *       whitelist:
 *         - 192.168.1.0/24
 * </pre>
 * </p>
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "sc.gateway.ip-access")
public class IpAccessProperties {

    /** 是否启用 IP 访问控制 */
    private boolean enabled = false;

    /** 模式: blacklist=黑名单拦截 / whitelist=白名单放行 */
    private String mode = "blacklist";

    /**
     * 是否信任反向代理转发头 (X-Forwarded-For / X-Real-IP)。
     * 仅在 网关前方有 Nginx/LB 等可信代理时开启，否则默认取 TCP 直连 IP。
     */
    private boolean trustProxy = false;

    /** 可信代理 IP 列表（支持 CIDR），仅 trustProxy=true 时生效 */
    private List<String> trustedProxies = new ArrayList<String>();

    /** 黑名单 IP 列表（支持 CIDR） */
    private List<String> blacklist = new ArrayList<String>();

    /** 白名单 IP 列表（支持 CIDR） */
    private List<String> whitelist = new ArrayList<String>();
}
