-- =========================================================
-- 站内信表结构初始化
-- =========================================================

-- 站内信消息主表
CREATE TABLE sys_message (
  message_id   BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
  title        VARCHAR(200) NOT NULL             COMMENT '消息标题',
  content      TEXT NOT NULL                     COMMENT '消息内容',
  msg_type     TINYINT DEFAULT 0                 COMMENT '消息类型 0=通知 1=公告 2=私信',
  send_scope   VARCHAR(20) DEFAULT 'USER'        COMMENT '发送范围 USER=指定用户 ROLE=按角色 ALL=全员',
  priority     TINYINT DEFAULT 0                 COMMENT '优先级 0=普通 1=重要 2=紧急',
  sender_id    BIGINT                            COMMENT '发送者用户ID',
  sender_name  VARCHAR(64)                       COMMENT '发送者昵称',
  create_by    VARCHAR(64)  DEFAULT '',
  create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  update_by    VARCHAR(64)  DEFAULT '',
  update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  del_flag     TINYINT      DEFAULT 0            COMMENT '0=正常 1=删除',
  INDEX idx_msg_type (msg_type),
  INDEX idx_send_scope (send_scope),
  INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内信消息主表';

-- 站内信接收人表
CREATE TABLE sys_message_receiver (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
  message_id   BIGINT NOT NULL                   COMMENT '消息ID',
  receiver_id  BIGINT NOT NULL                   COMMENT '接收用户ID',
  is_read      TINYINT DEFAULT 0                 COMMENT '0=未读 1=已读',
  read_time    DATETIME                          COMMENT '阅读时间',
  create_time  DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_receiver (receiver_id, is_read),
  INDEX idx_message (message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内信接收人表';
