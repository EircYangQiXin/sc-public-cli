package com.sc.common.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录用户上下文（线程安全，支持线程池传递）
 */
public class SecurityContextHolder {

    private static final TransmittableThreadLocal<Map<String, Object>> THREAD_LOCAL =
            new TransmittableThreadLocal<Map<String, Object>>() {
                @Override
                protected Map<String, Object> initialValue() {
                    return new HashMap<>(8);
                }
            };

    public static void set(String key, Object value) {
        THREAD_LOCAL.get().put(key, value);
    }

    public static Object get(String key) {
        return THREAD_LOCAL.get().get(key);
    }

    public static <T> T get(String key, Class<T> clazz) {
        Object value = THREAD_LOCAL.get().get(key);
        if (value == null) {
            return null;
        }
        return clazz.cast(value);
    }

    public static void setUserId(Long userId) {
        set("userId", userId);
    }

    public static Long getUserId() {
        Object value = get("userId");
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        return Long.parseLong(value.toString());
    }

    public static void setUsername(String username) {
        set("username", username);
    }

    public static String getUsername() {
        return get("username", String.class);
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
