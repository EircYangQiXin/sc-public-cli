package com.sc.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 灰度路由配置
 * <p>
 * 配置示例：
 * <pre>
 * sc:
 *   gateway:
 *     gray:
 *       enabled: true
 *       sign-secret: "my-secret-key"       # 灰度签名密钥（推荐配置）
 *       allowed-sources:                     # 允许的灰度流量来源 IP
 *         - 10.0.0.0/8
 *         - 192.168.1.0/24
 * </pre>
 * </p>
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "sc.gateway.gray")
public class GrayProperties {

    /** 是否启用灰度路由 */
    private boolean enabled = false;

    /**
     * 灰度签名密钥。
     * 客户端需在 X-Gray-Sign 头中携带此值才能访问灰度实例。
     * 未配置时需依赖 allowedSources。
     */
    private String signSecret;

    /**
     * 允许发起灰度请求的来源 IP 列表 (作为 signSecret 的替代方案)。
     * 仅当 signSecret 未配置时生效。
     */
    private List<String> allowedSources = new ArrayList<String>();
}
