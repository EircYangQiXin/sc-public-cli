package com.sc.common.swagger.version;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * API 版本化自动配置
 * <p>
 * 通过 {@link WebMvcRegistrations} 替换默认的 {@link RequestMappingHandlerMapping}，
 * 使 {@link ApiVersion} 注解生效。
 * </p>
 */
@Configuration
public class ApiVersionAutoConfiguration {

    @Bean
    public WebMvcRegistrations apiVersionWebMvcRegistrations() {
        return new WebMvcRegistrations() {
            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
                return new ApiVersionRequestMappingHandlerMapping();
            }
        };
    }
}
