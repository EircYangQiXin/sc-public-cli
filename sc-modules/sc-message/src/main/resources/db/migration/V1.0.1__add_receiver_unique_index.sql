-- =========================================================
-- 站内信接收人表补充唯一索引，防止重复数据导致 JOIN 多行
-- =========================================================

ALTER TABLE sys_message_receiver
    ADD UNIQUE INDEX uk_msg_receiver (message_id, receiver_id);
