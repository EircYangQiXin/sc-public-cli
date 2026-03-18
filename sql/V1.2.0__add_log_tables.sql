-- ================================================
-- 操作日志表 & 登录日志表
-- ================================================

CREATE TABLE IF NOT EXISTS `sys_oper_log` (
    `oper_id`       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '操作日志ID',
    `title`         VARCHAR(64)  DEFAULT '' COMMENT '模块标题',
    `business_type` INT          DEFAULT 0  COMMENT '业务类型 (0其他 1新增 2修改 3删除 4授权 5导出 6导入 7清空)',
    `operator_type` INT          DEFAULT 0  COMMENT '操作人类型 (0其他 1后台 2移动端)',
    `method`        VARCHAR(256) DEFAULT '' COMMENT '请求方法',
    `oper_url`      VARCHAR(256) DEFAULT '' COMMENT '请求URL',
    `oper_name`     VARCHAR(64)  DEFAULT '' COMMENT '操作人',
    `oper_param`    TEXT                    COMMENT '请求参数',
    `json_result`   TEXT                    COMMENT '返回结果',
    `status`        INT          DEFAULT 0  COMMENT '操作状态 (0正常 1异常)',
    `error_msg`     VARCHAR(2000) DEFAULT '' COMMENT '错误信息',
    `oper_time`     DATETIME                COMMENT '操作时间',
    `cost_time`     BIGINT       DEFAULT 0  COMMENT '耗时（毫秒）',
    PRIMARY KEY (`oper_id`),
    KEY `idx_oper_time` (`oper_time`),
    KEY `idx_oper_name` (`oper_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

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
