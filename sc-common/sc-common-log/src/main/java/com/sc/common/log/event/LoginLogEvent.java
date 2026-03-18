package com.sc.common.log.event;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志事件（用于异步处理）
 */
@Data
public class LoginLogEvent {

    /** 用户名 */
    private String username;

    /** 登录IP */
    private String ipAddr;

    /** 登录状态 (0成功 1失败) */
    private Integer status;

    /** 提示消息 */
    private String msg;

    /** 浏览器 */
    private String browser;

    /** 操作系统 */
    private String os;

    /** 登录时间 */
    private LocalDateTime loginTime;
}
