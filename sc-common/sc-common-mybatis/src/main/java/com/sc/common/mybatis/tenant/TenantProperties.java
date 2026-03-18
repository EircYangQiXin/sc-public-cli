package com.sc.common.mybatis.tenant;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 多租户配置属性
 */
@Data
@ConfigurationProperties(prefix = "sc.tenant")
public class TenantProperties {

    /** 是否开启多租户（默认关闭） */
    private boolean enabled = false;

    /** 租户字段名 */
    private String column = "tenant_id";

    /** 需要忽略的表名集合 */
    private List<String> ignoreTables = new ArrayList<>();
}
