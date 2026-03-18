package com.sc.common.core.utils;

import com.sc.common.core.exception.ServiceException;

import java.util.Collection;

/**
 * 业务断言工具类
 * <p>
 * 简化 if-throw 模式，断言失败时抛出 {@link ServiceException}
 * </p>
 */
public final class AssertUtils {

    private AssertUtils() {}

    /**
     * 断言对象不为 null
     *
     * @param object  目标对象
     * @param message 异常消息
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new ServiceException(message);
        }
    }

    /**
     * 断言字符串不为空
     */
    public static void notEmpty(String str, String message) {
        if (str == null || str.isEmpty()) {
            throw new ServiceException(message);
        }
    }

    /**
     * 断言集合不为空
     */
    public static void notEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new ServiceException(message);
        }
    }

    /**
     * 断言条件为 true
     *
     * @param expression 条件表达式
     * @param message    异常消息
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new ServiceException(message);
        }
    }

    /**
     * 断言条件为 false
     */
    public static void isFalse(boolean expression, String message) {
        if (expression) {
            throw new ServiceException(message);
        }
    }

    /**
     * 断言两个值相等
     */
    public static void equals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected == null || !expected.equals(actual)) {
            throw new ServiceException(message);
        }
    }

    /**
     * 直接抛出业务异常
     */
    public static void fail(String message) {
        throw new ServiceException(message);
    }
}
