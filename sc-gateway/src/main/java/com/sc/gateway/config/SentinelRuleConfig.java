package com.sc.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.sc.gateway.handler.SentinelFallbackHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sentinel 网关限流 + 熔断降级规则配置
 * <p>
 * 初始化默认限流和熔断规则。生产环境建议通过 Sentinel Dashboard 或 Nacos 动态覆盖。
 * </p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SentinelRuleConfig {

    private final SentinelFallbackHandler sentinelFallbackHandler;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }

    @PostConstruct
    public void init() {
        GatewayCallbackManager.setBlockHandler(sentinelFallbackHandler);
        initApiGroups();
        initGatewayRules();
        initDegradeRules();
        log.info("Sentinel 网关限流 + 熔断降级规则初始化完成");
    }

    /**
     * 定义 API 分组
     */
    private void initApiGroups() {
        Set<ApiDefinition> definitions = new HashSet<ApiDefinition>();

        Set<ApiPredicateItem> authPredicates = new HashSet<ApiPredicateItem>();
        authPredicates.add(new ApiPathPredicateItem()
                .setPattern("/auth/**")
                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
        definitions.add(new ApiDefinition("auth-api").setPredicateItems(authPredicates));

        Set<ApiPredicateItem> systemPredicates = new HashSet<ApiPredicateItem>();
        systemPredicates.add(new ApiPathPredicateItem()
                .setPattern("/system/**")
                .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
        definitions.add(new ApiDefinition("system-api").setPredicateItems(systemPredicates));

        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
    }

    /**
     * 初始化默认限流规则
     */
    private void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<GatewayFlowRule>();

        // 登录接口: 每秒最多 10 次
        rules.add(new GatewayFlowRule("auth-api")
                .setCount(10)
                .setIntervalSec(1));

        // 系统管理接口: 每秒最多 50 次
        rules.add(new GatewayFlowRule("system-api")
                .setCount(50)
                .setIntervalSec(1));

        GatewayRuleManager.loadRules(rules);
    }

    /**
     * 初始化默认熔断降级规则
     * <p>
     * 策略:
     * <ul>
     *   <li>慢调用比例: 当慢调用（RT > 1s）比例超过 50% 时触发熔断（10秒窗口），熔断 30s</li>
     *   <li>异常比例: 当异常比例超过 60% 时触发熔断（10秒窗口），熔断 30s</li>
     * </ul>
     * </p>
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<DegradeRule>();

        // 认证服务 - 慢调用比例熔断
        DegradeRule authSlowRule = new DegradeRule("auth-api");
        authSlowRule.setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType());
        authSlowRule.setCount(0.5);       // 慢调用比例阈值 50%
        authSlowRule.setSlowRatioThreshold(0.5); // 慢调用比例阈值
        authSlowRule.setTimeWindow(30);    // 熔断持续 30 秒
        authSlowRule.setStatIntervalMs(10000); // 统计窗口 10 秒
        authSlowRule.setMinRequestAmount(5);   // 最小请求数
        rules.add(authSlowRule);

        // 系统管理服务 - 异常比例熔断
        DegradeRule systemErrorRule = new DegradeRule("system-api");
        systemErrorRule.setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType());
        systemErrorRule.setCount(0.6);     // 异常比例阈值 60%
        systemErrorRule.setTimeWindow(30);  // 熔断持续 30 秒
        systemErrorRule.setStatIntervalMs(10000);
        systemErrorRule.setMinRequestAmount(5);
        rules.add(systemErrorRule);

        DegradeRuleManager.loadRules(rules);
    }
}
