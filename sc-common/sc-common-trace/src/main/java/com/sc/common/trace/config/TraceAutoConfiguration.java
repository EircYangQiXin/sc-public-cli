package com.sc.common.trace.config;

import brave.Tracer;
import com.sc.common.trace.filter.TraceResponseFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import javax.servlet.Filter;

/**
 * 链路追踪自动配置
 * <p>
 * 在 Servlet 环境下自动注册 {@link TraceResponseFilter}，
 * 将 traceId 写入响应头。WebFlux（Gateway）环境下不生效。
 */
@Configuration
public class TraceAutoConfiguration {

    /**
     * 注册 TraceResponseFilter（仅 Servlet 环境）
     */
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnBean(Tracer.class)
    @ConditionalOnClass(Filter.class)
    public FilterRegistrationBean<TraceResponseFilter> traceResponseFilter(Tracer tracer) {
        FilterRegistrationBean<TraceResponseFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceResponseFilter(tracer));
        registration.addUrlPatterns("/*");
        registration.setName("traceResponseFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }
}
