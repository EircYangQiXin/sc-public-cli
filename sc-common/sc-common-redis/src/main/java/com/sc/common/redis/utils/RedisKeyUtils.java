package com.sc.common.redis.utils;

/**
 * Redis Key 规范管理工具
 * <p>
 * 统一 Key 前缀和分隔符，避免各业务 Key 命名冲突
 * </p>
 */
public final class RedisKeyUtils {

    /** 全局 Key 前缀 */
    private static final String KEY_PREFIX = "sc";

    /** Key 分隔符 */
    private static final String SEPARATOR = ":";

    private RedisKeyUtils() {}

    /**
     * 构建 Redis Key
     * <p>
     * 示例：{@code buildKey("user", "info", "123")} → {@code "sc:user:info:123"}
     * </p>
     *
     * @param parts Key 的各段
     * @return 完整 Key
     */
    public static String buildKey(String... parts) {
        StringBuilder sb = new StringBuilder(KEY_PREFIX);
        for (String part : parts) {
            sb.append(SEPARATOR).append(part);
        }
        return sb.toString();
    }

    /**
     * 构建带模块前缀的 Key
     * <p>
     * 示例：{@code moduleKey("auth", "captcha", uuid)} → {@code "sc:auth:captcha:{uuid}"}
     * </p>
     */
    public static String moduleKey(String module, String bizKey, Object id) {
        return KEY_PREFIX + SEPARATOR + module + SEPARATOR + bizKey + SEPARATOR + id;
    }

    /**
     * 构建通配符 Key（用于批量删除/扫描）
     * <p>
     * 示例：{@code patternKey("user", "role")} → {@code "sc:user:role:*"}
     * </p>
     */
    public static String patternKey(String... parts) {
        return buildKey(parts) + SEPARATOR + "*";
    }
}
