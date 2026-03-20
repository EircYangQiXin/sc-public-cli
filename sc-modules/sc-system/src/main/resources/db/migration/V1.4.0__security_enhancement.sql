-- ==============================================
-- V1.4.0 安全增强: 账号锁定/密码策略/MFA/设备信任
-- ==============================================

-- sys_user 新增安全字段
ALTER TABLE sys_user ADD COLUMN password_update_time DATETIME DEFAULT NULL COMMENT '密码最后修改时间';
ALTER TABLE sys_user ADD COLUMN mfa_secret VARCHAR(64) DEFAULT NULL COMMENT 'MFA密钥(Base32编码)';
ALTER TABLE sys_user ADD COLUMN mfa_enabled TINYINT DEFAULT 0 COMMENT '是否启用MFA (0否 1是)';

-- 初始化已有用户的密码修改时间为当前时间（避免所有用户首次登录都提示过期）
UPDATE sys_user SET password_update_time = NOW() WHERE password_update_time IS NULL;
