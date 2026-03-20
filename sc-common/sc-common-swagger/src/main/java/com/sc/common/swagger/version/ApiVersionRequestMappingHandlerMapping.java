package com.sc.common.swagger.version;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

/**
 * 自定义 RequestMappingHandlerMapping，支持 @ApiVersion 注解
 * <p>
 * 读取 Controller 类或方法上的 {@link ApiVersion} 注解，
 * 自动在原始请求路径前添加 /api/v{version} 前缀。
 * <br>
 * 方法级注解优先于类级注解。
 * </p>
 */
public class ApiVersionRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo info = super.getMappingForMethod(method, handlerType);
        if (info == null) {
            return null;
        }

        // 优先方法级注解，其次类级注解
        ApiVersion apiVersion = AnnotationUtils.findAnnotation(method, ApiVersion.class);
        if (apiVersion == null) {
            apiVersion = AnnotationUtils.findAnnotation(handlerType, ApiVersion.class);
        }

        if (apiVersion != null) {
            String prefix = "/api/v" + apiVersion.value();
            RequestMappingInfo versionInfo = RequestMappingInfo
                    .paths(prefix)
                    .build();
            info = versionInfo.combine(info);
        }

        return info;
    }
}
