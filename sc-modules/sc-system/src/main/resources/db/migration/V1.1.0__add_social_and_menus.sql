-- ==============================================
-- V1.1.0 第三方账号绑定表 + 补充菜单权限
-- ==============================================

-- ----------------------------
-- 第三方账号绑定表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_social`;
CREATE TABLE `sys_user_social` (
    `id`                BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`           BIGINT NOT NULL COMMENT '系统用户ID',
    `social_type`       VARCHAR(30) NOT NULL COMMENT '平台类型: wechat_open/wechat_mp/wechat_mini/alipay/qq/weibo/github/dingtalk/wechat_work/apple/google',
    `social_id`         VARCHAR(200) NOT NULL COMMENT '第三方唯一标识 (openId/userId/uid)',
    `union_id`          VARCHAR(200) DEFAULT NULL COMMENT '第三方 unionId (微信跨应用统一标识)',
    `social_nickname`   VARCHAR(100) DEFAULT NULL COMMENT '第三方昵称',
    `social_avatar`     VARCHAR(500) DEFAULT NULL COMMENT '第三方头像',
    `access_token`      VARCHAR(500) DEFAULT NULL COMMENT 'Access Token (加密存储)',
    `refresh_token`     VARCHAR(500) DEFAULT NULL COMMENT 'Refresh Token (加密存储)',
    `token_expire_time` DATETIME DEFAULT NULL COMMENT 'Token 过期时间',
    `raw_data`          TEXT DEFAULT NULL COMMENT '第三方原始返回数据 (JSON)',
    `tenant_id`         BIGINT DEFAULT NULL COMMENT '租户ID',
    `del_flag`          INT DEFAULT 0 COMMENT '删除标志',
    `create_by`         VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time`       DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by`         VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time`       DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_social_type_id` (`social_type`, `social_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_union_id` (`union_id`)
) ENGINE=InnoDB COMMENT='用户第三方账号绑定表';

-- ----------------------------
-- 补充菜单：部门管理按钮权限
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1005, '部门查询', 103, 1, '', NULL, 'F', '0', '0', 'system:dept:query', '#', 'system', NOW(), '', NULL);
INSERT INTO `sys_menu` VALUES (1006, '部门新增', 103, 2, '', NULL, 'F', '0', '0', 'system:dept:add', '#', 'system', NOW(), '', NULL);
INSERT INTO `sys_menu` VALUES (1007, '部门修改', 103, 3, '', NULL, 'F', '0', '0', 'system:dept:edit', '#', 'system', NOW(), '', NULL);
INSERT INTO `sys_menu` VALUES (1008, '部门删除', 103, 4, '', NULL, 'F', '0', '0', 'system:dept:remove', '#', 'system', NOW(), '', NULL);

-- 角色管理按钮权限
INSERT INTO `sys_menu` VALUES (1009, '角色查询', 101, 1, '', NULL, 'F', '0', '0', 'system:role:query', '#', 'system', NOW(), '', NULL);
INSERT INTO `sys_menu` VALUES (1010, '角色新增', 101, 2, '', NULL, 'F', '0', '0', 'system:role:add', '#', 'system', NOW(), '', NULL);
INSERT INTO `sys_menu` VALUES (1011, '角色修改', 101, 3, '', NULL, 'F', '0', '0', 'system:role:edit', '#', 'system', NOW(), '', NULL);
INSERT INTO `sys_menu` VALUES (1012, '角色删除', 101, 4, '', NULL, 'F', '0', '0', 'system:role:remove', '#', 'system', NOW(), '', NULL);

-- 菜单管理按钮权限
INSERT INTO `sys_menu` VALUES (1013, '菜单查询', 102, 1, '', NULL, 'F', '0', '0', 'system:menu:query', '#', 'system', NOW(), '', NULL);
INSERT INTO `sys_menu` VALUES (1014, '菜单新增', 102, 2, '', NULL, 'F', '0', '0', 'system:menu:add', '#', 'system', NOW(), '', NULL);
INSERT INTO `sys_menu` VALUES (1015, '菜单修改', 102, 3, '', NULL, 'F', '0', '0', 'system:menu:edit', '#', 'system', NOW(), '', NULL);
INSERT INTO `sys_menu` VALUES (1016, '菜单删除', 102, 4, '', NULL, 'F', '0', '0', 'system:menu:remove', '#', 'system', NOW(), '', NULL);

-- 字典管理菜单 + 按钮权限
INSERT INTO `sys_menu` VALUES (104, '字典管理', 1, 5, 'dict', 'system/dict/index', 'C', '0', '0', 'system:dict:list', 'dict', 'system', NOW(), '', NULL);
INSERT INTO `sys_menu` VALUES (1017, '字典查询', 104, 1, '', NULL, 'F', '0', '0', 'system:dict:query', '#', 'system', NOW(), '', NULL);
INSERT INTO `sys_menu` VALUES (1018, '字典新增', 104, 2, '', NULL, 'F', '0', '0', 'system:dict:add', '#', 'system', NOW(), '', NULL);
INSERT INTO `sys_menu` VALUES (1019, '字典修改', 104, 3, '', NULL, 'F', '0', '0', 'system:dict:edit', '#', 'system', NOW(), '', NULL);
INSERT INTO `sys_menu` VALUES (1020, '字典删除', 104, 4, '', NULL, 'F', '0', '0', 'system:dict:remove', '#', 'system', NOW(), '', NULL);

-- 超级管理员赋予新增权限
INSERT INTO `sys_role_menu` VALUES (1, 104);
INSERT INTO `sys_role_menu` VALUES (1, 1005);
INSERT INTO `sys_role_menu` VALUES (1, 1006);
INSERT INTO `sys_role_menu` VALUES (1, 1007);
INSERT INTO `sys_role_menu` VALUES (1, 1008);
INSERT INTO `sys_role_menu` VALUES (1, 1009);
INSERT INTO `sys_role_menu` VALUES (1, 1010);
INSERT INTO `sys_role_menu` VALUES (1, 1011);
INSERT INTO `sys_role_menu` VALUES (1, 1012);
INSERT INTO `sys_role_menu` VALUES (1, 1013);
INSERT INTO `sys_role_menu` VALUES (1, 1014);
INSERT INTO `sys_role_menu` VALUES (1, 1015);
INSERT INTO `sys_role_menu` VALUES (1, 1016);
INSERT INTO `sys_role_menu` VALUES (1, 1017);
INSERT INTO `sys_role_menu` VALUES (1, 1018);
INSERT INTO `sys_role_menu` VALUES (1, 1019);
INSERT INTO `sys_role_menu` VALUES (1, 1020);
