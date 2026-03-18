package com.sc.common.core.utils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期工具类（基于 Java 8 Time API）
 */
public final class DateUtils {

    /** 常用格式 */
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    private DateUtils() {}

    /**
     * 当前日期时间
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 当前日期
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * 格式化日期时间
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 格式化为 yyyy-MM-dd HH:mm:ss
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return format(dateTime, YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 格式化为 yyyy-MM-dd
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern(YYYY_MM_DD));
    }

    /**
     * 解析日期时间字符串
     */
    public static LocalDateTime parse(String str, String pattern) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(str, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 解析 yyyy-MM-dd HH:mm:ss 格式
     */
    public static LocalDateTime parseDateTime(String str) {
        return parse(str, YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 解析 yyyy-MM-dd 格式
     */
    public static LocalDate parseDate(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        return LocalDate.parse(str, DateTimeFormatter.ofPattern(YYYY_MM_DD));
    }

    /**
     * 计算两个日期时间之间的间隔（秒）
     */
    public static long betweenSeconds(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end).getSeconds();
    }

    /**
     * 计算两个日期时间之间的间隔（毫秒）
     */
    public static long betweenMillis(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end).toMillis();
    }

    /**
     * 判断日期是否在指定范围内
     */
    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        return !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }

    /**
     * 获取当天的开始时间 00:00:00
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    /**
     * 获取当天的结束时间 23:59:59
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        return date.atTime(23, 59, 59);
    }
}
