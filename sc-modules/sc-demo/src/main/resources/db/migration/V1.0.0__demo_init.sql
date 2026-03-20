-- ================================================
-- Demo 模块初始化 - 账户示例表 + Seata undo_log
-- ================================================

-- 示例账户表（演示分布式事务）
CREATE TABLE IF NOT EXISTS `demo_account` (
    `id`      BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` BIGINT      NOT NULL COMMENT '用户ID',
    `balance` DECIMAL(10,2) DEFAULT 0 COMMENT '余额',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='示例账户表';

-- 初始化测试账户
INSERT INTO `demo_account` (user_id, balance) VALUES (1, 10000.00);
INSERT INTO `demo_account` (user_id, balance) VALUES (2, 10000.00);

-- Seata AT 模式必需的 undo_log 表（每个参与分布式事务的数据库都需要）
CREATE TABLE IF NOT EXISTS `undo_log` (
    `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context, such as serialization',
    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`    INT          NOT NULL COMMENT '0:normal status, 1:defense status',
    `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT undo log table';
