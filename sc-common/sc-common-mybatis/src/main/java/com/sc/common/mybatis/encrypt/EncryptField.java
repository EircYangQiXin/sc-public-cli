package com.sc.common.mybatis.encrypt;

import java.lang.annotation.*;

/**
 * 敏感字段加密注解
 * <p>
 * 标注在 Entity 字段上，配合 {@link EncryptTypeHandler} 使用。
 * 入库时自动加密，查询时自动解密。
 * <p>
 * 使用方式：
 * <pre>
 * {@code
 * @TableField(typeHandler = EncryptTypeHandler.class)
 * @EncryptField
 * private String phone;
 * }
 * </pre>
 * <p>
 * 设计模式：策略模式 — 加密算法通过 {@link EncryptStrategy} 接口抽象，
 * 默认使用 AES-256-CBC，可替换为国密 SM4 等。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EncryptField {

    /**
     * 加密算法标识（可用于策略选择）
     * 默认使用 AES
     */
    String algorithm() default "AES";
}
