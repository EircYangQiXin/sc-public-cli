package com.sc.common.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全配置属性
 */
@Data
@ConfigurationProperties(prefix = "sc.security")
public class SecurityProperties {

    /** 免鉴权路径 */
    private List<String> ignoreUrls = new ArrayList<>();
}
