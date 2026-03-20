-- ================================================
-- V1.2.0 登录日志表
-- 说明: 操作日志表已在 V1.0.0 中创建, 本迁移仅补充登录日志表
-- ================================================

CREATE TABLE IF NOT EXISTS `sys_login_log` (
    `log_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `username`   VARCHAR(64)  DEFAULT '' COMMENT '用户名',
    `ip_addr`    VARCHAR(128) DEFAULT '' COMMENT '登录IP',
    `status`     INT          DEFAULT 0  COMMENT '登录状态 (0成功 1失败)',
    `msg`        VARCHAR(256) DEFAULT '' COMMENT '提示消息',
    `browser`    VARCHAR(64)  DEFAULT '' COMMENT '浏览器',
    `os`         VARCHAR(64)  DEFAULT '' COMMENT '操作系统',
    `login_time` DATETIME                COMMENT '登录时间',
    PRIMARY KEY (`log_id`),
    KEY `idx_login_time` (`login_time`),
    KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';
