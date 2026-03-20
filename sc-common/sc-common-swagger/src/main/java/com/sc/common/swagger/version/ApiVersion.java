package com.sc.common.swagger.version;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API 版本控制注解
 * <p>
 * 标注在 Controller 类或方法上，自动为请求路径添加 /api/v{version} 前缀。
 * 方法级注解优先于类级注解。
 * </p>
 *
 * <pre>
 * &#64;RestController
 * &#64;RequestMapping("/users")
 * &#64;ApiVersion(1)
 * public class UserController {
 *     // 实际路径: /api/v1/users/...
 * }
 * </pre>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiVersion {

    /**
     * API 版本号，默认为 1
     */
    int value() default 1;
}
