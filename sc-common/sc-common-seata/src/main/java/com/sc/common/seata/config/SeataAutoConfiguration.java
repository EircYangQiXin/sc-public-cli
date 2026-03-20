package com.sc.common.seata.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Seata 分布式事务自动配置
 * <p>
 * Spring Cloud Alibaba Seata Starter 已自动完成以下工作：
 * <ul>
 *   <li>自动代理数据源（DataSourceProxy，AT 模式核心）</li>
 *   <li>注册 GlobalTransactionScanner（扫描 @GlobalTransactional 注解）</li>
 *   <li>Feign/RestTemplate 自动传播 XID</li>
 * </ul>
 * 使用方只需在 bootstrap.yml 中配置 Seata Server 地址和事务分组即可。
 * </p>
 * <p>
 * 配置示例（bootstrap.yml）：
 * <pre>
 * seata:
 *   enabled: true
 *   application-id: ${spring.application.name}
 *   tx-service-group: sc_tx_group
 *   service:
 *     vgroup-mapping:
 *       sc_tx_group: default
 *     grouplist:
 *       default: 127.0.0.1:8091
 * </pre>
 * </p>
 */
@Slf4j
@Configuration
public class SeataAutoConfiguration {

    // Spring Cloud Alibaba Seata Starter 已自动配置 DataSourceProxy 和 GlobalTransactionScanner
    // 此类保留为扩展点，可用于自定义 Seata 配置（如自定义序列化、超时策略等）
}
