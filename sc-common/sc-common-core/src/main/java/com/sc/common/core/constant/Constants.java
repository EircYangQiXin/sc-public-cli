package com.sc.common.core.constant;

/**
 * 通用常量
 */
public final class Constants {

    private Constants() {}

    /** UTF-8 编码 */
    public static final String UTF8 = "UTF-8";

    /** 成功标记 */
    public static final int SUCCESS = 200;
    /** 失败标记 */
    public static final int FAIL = 500;

    /** 登录用户 Token Header */
    public static final String HEADER_TOKEN = "Authorization";

    /** 登录用户 ID Header (内部传递) */
    public static final String HEADER_USER_ID = "X-User-Id";
    /** 登录用户名 Header (内部传递) */
    public static final String HEADER_USERNAME = "X-Username";

    /** 正常状态 */
    public static final String NORMAL = "0";
    /** 停用状态 */
    public static final String DISABLE = "1";

    /** 超级管理员角色 Key */
    public static final String SUPER_ADMIN_ROLE = "admin";

    /** 菜单类型 - 目录 */
    public static final String MENU_TYPE_DIR = "M";
    /** 菜单类型 - 菜单 */
    public static final String MENU_TYPE_MENU = "C";
    /** 菜单类型 - 按钮 */
    public static final String MENU_TYPE_BUTTON = "F";
}
