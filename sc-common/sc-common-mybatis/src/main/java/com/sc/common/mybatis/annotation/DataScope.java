package com.sc.common.mybatis.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * <p>在 Mapper 方法上标注，配合 DataPermissionInterceptor 使用</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 部门表的别名
     */
    String deptAlias() default "d";

    /**
     * 用户表的别名
     */
    String userAlias() default "u";
}
