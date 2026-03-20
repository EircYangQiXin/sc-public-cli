package com.sc.common.redis.annotation;

import java.lang.annotation.*;

/**
 * 防重复提交注解
 * <p>
 * 标注在 Controller 方法上，基于 Redis 实现接口幂等性。
 * 同一用户在指定时间窗口内重复提交相同请求将被拦截。
 * </p>
 * <p>
 * 使用方式：
 * <pre>
 * {@code
 * @RepeatSubmit(interval = 3)  // 3 秒内不允许重复提交
 * @PostMapping("/add")
 * public R<Void> add(@RequestBody SysUser user) { ... }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit {

    /**
     * 防重复提交间隔时间（秒），默认 5 秒
     */
    int interval() default 5;

    /**
     * 提示消息
     */
    String message() default "请勿重复提交";
}
