-- ================================================
-- 本地消息表 (Outbox 模式)
-- 各业务模块按需在自己的数据库中执行此脚本
-- ================================================

CREATE TABLE IF NOT EXISTS `mq_local_message` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `message_key`     VARCHAR(128) NOT NULL COMMENT '消息唯一业务键',
    `exchange`        VARCHAR(128) NOT NULL COMMENT '交换机',
    `routing_key`     VARCHAR(128) NOT NULL COMMENT '路由键',
    `message_body`    TEXT                  COMMENT '消息体 (JSON)',
    `status`          VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/SENT/CONFIRMED/FAILED',
    `retry_count`     INT          NOT NULL DEFAULT 0 COMMENT '已重试次数',
    `max_retry`       INT          NOT NULL DEFAULT 5 COMMENT '最大重试次数',
    `next_retry_time` DATETIME              COMMENT '下次重试时间',
    `error_msg`       VARCHAR(500)          COMMENT '失败原因',
    `create_time`     DATETIME              COMMENT '创建时间',
    `update_time`     DATETIME              COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_message_key` (`message_key`),
    KEY `idx_status_retry` (`status`, `next_retry_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MQ本地消息表 (Outbox)';
