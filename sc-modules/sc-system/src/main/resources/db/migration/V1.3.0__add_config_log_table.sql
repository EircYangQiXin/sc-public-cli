-- ================================================
-- V1.3.0 系统配置变更审计日志表
-- ================================================

CREATE TABLE IF NOT EXISTS `sys_config_log` (
    `log_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `config_id`  BIGINT       DEFAULT NULL COMMENT '参数ID',
    `config_key` VARCHAR(100) DEFAULT '' COMMENT '参数键名',
    `old_value`  VARCHAR(500) DEFAULT '' COMMENT '变更前值',
    `new_value`  VARCHAR(500) DEFAULT '' COMMENT '变更后值',
    `oper_type`  VARCHAR(20)  DEFAULT '' COMMENT '操作类型 (INSERT/UPDATE/DELETE)',
    `oper_by`    VARCHAR(64)  DEFAULT '' COMMENT '操作人',
    `oper_time`  DATETIME                COMMENT '操作时间',
    PRIMARY KEY (`log_id`),
    KEY `idx_config_key` (`config_key`),
    KEY `idx_oper_time` (`oper_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置变更审计日志表';
