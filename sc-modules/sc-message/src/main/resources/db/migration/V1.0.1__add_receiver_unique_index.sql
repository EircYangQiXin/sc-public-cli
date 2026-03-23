-- =========================================================
-- 站内信接收人表补充唯一索引，防止重复数据导致 JOIN 多行
-- 先清理可能存在的历史重复数据，再安全添加索引
-- =========================================================

-- 步骤1：清理历史重复数据，保留每组 (message_id, receiver_id) 中 id 最小的一条
DELETE r1 FROM sys_message_receiver r1
INNER JOIN sys_message_receiver r2
    ON r1.message_id = r2.message_id
   AND r1.receiver_id = r2.receiver_id
   AND r1.id > r2.id;

-- 步骤2：添加唯一索引（如果不存在）
-- MySQL 不支持 IF NOT EXISTS 语法，通过存储过程实现幂等
DROP PROCEDURE IF EXISTS add_uk_msg_receiver;

DELIMITER //
CREATE PROCEDURE add_uk_msg_receiver()
BEGIN
    DECLARE index_exists INT DEFAULT 0;
    SELECT COUNT(*) INTO index_exists
      FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'sys_message_receiver'
       AND INDEX_NAME = 'uk_msg_receiver';
    IF index_exists = 0 THEN
        ALTER TABLE sys_message_receiver
            ADD UNIQUE INDEX uk_msg_receiver (message_id, receiver_id);
    END IF;
END //
DELIMITER ;

CALL add_uk_msg_receiver();
DROP PROCEDURE IF EXISTS add_uk_msg_receiver;
