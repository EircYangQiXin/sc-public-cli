-- =============================================
-- SC Public CLI - 系统管理数据库初始化脚本
-- =============================================

CREATE DATABASE IF NOT EXISTS `sc_system` DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE `sc_system`;

-- ----------------------------
-- 部门表
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
    `dept_id`     BIGINT NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    `parent_id`   BIGINT DEFAULT 0 COMMENT '父部门ID',
    `ancestors`   VARCHAR(500) DEFAULT '' COMMENT '祖级列表',
    `dept_name`   VARCHAR(30) DEFAULT '' COMMENT '部门名称',
    `order_num`   INT DEFAULT 0 COMMENT '显示顺序',
    `leader`      VARCHAR(20) DEFAULT NULL COMMENT '负责人',
    `phone`       VARCHAR(11) DEFAULT NULL COMMENT '联系电话',
    `status`      CHAR(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `del_flag`    INT DEFAULT 0 COMMENT '删除标志（0正常 1删除）',
    `tenant_id`   BIGINT DEFAULT NULL COMMENT '租户ID',
    `create_by`   VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB COMMENT='部门表';

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `user_id`     BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `dept_id`     BIGINT DEFAULT NULL COMMENT '部门ID',
    `username`    VARCHAR(30) NOT NULL COMMENT '用户名',
    `nick_name`   VARCHAR(30) NOT NULL COMMENT '昵称',
    `email`       VARCHAR(50) DEFAULT '' COMMENT '邮箱',
    `phone`       VARCHAR(11) DEFAULT '' COMMENT '手机号',
    `sex`         CHAR(1) DEFAULT '0' COMMENT '性别（0男 1女 2未知）',
    `avatar`      VARCHAR(200) DEFAULT '' COMMENT '头像',
    `password`    VARCHAR(100) DEFAULT '' COMMENT '密码',
    `status`      CHAR(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `del_flag`    INT DEFAULT 0 COMMENT '删除标志（0正常 1删除）',
    `tenant_id`   BIGINT DEFAULT NULL COMMENT '租户ID',
    `create_by`   VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB COMMENT='用户表';

-- ----------------------------
-- 角色表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `role_id`     BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name`   VARCHAR(30) NOT NULL COMMENT '角色名称',
    `role_key`    VARCHAR(100) NOT NULL COMMENT '角色权限字符串',
    `role_sort`   INT DEFAULT 0 COMMENT '显示顺序',
    `data_scope`  INT DEFAULT 1 COMMENT '数据范围（1全部 2自定义 3本部门 4本部门及以下 5仅本人）',
    `status`      CHAR(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `del_flag`    INT DEFAULT 0 COMMENT '删除标志',
    `tenant_id`   BIGINT DEFAULT NULL COMMENT '租户ID',
    `create_by`   VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`role_id`)
) ENGINE=InnoDB COMMENT='角色表';

-- ----------------------------
-- 菜单表
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
    `menu_id`     BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `menu_name`   VARCHAR(50) NOT NULL COMMENT '菜单名称',
    `parent_id`   BIGINT DEFAULT 0 COMMENT '父菜单ID',
    `order_num`   INT DEFAULT 0 COMMENT '显示顺序',
    `path`        VARCHAR(200) DEFAULT '' COMMENT '路由地址',
    `component`   VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
    `menu_type`   CHAR(1) DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
    `visible`     CHAR(1) DEFAULT '0' COMMENT '可见状态（0显示 1隐藏）',
    `status`      CHAR(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
    `perms`       VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    `icon`        VARCHAR(100) DEFAULT '#' COMMENT '菜单图标',
    `create_by`   VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB COMMENT='菜单权限表';

-- ----------------------------
-- 用户角色关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB COMMENT='用户角色关联表';

-- ----------------------------
-- 角色菜单关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE=InnoDB COMMENT='角色菜单关联表';

-- ----------------------------
-- 角色部门关联表（数据权限）
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept` (
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `dept_id` BIGINT NOT NULL COMMENT '部门ID',
    PRIMARY KEY (`role_id`, `dept_id`)
) ENGINE=InnoDB COMMENT='角色部门关联表';

-- ----------------------------
-- 字典类型表
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
    `dict_id`     BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典ID',
    `dict_name`   VARCHAR(100) DEFAULT '' COMMENT '字典名称',
    `dict_type`   VARCHAR(100) DEFAULT '' COMMENT '字典类型',
    `status`      CHAR(1) DEFAULT '0' COMMENT '状态',
    `create_by`   VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`dict_id`),
    UNIQUE KEY `uk_dict_type` (`dict_type`)
) ENGINE=InnoDB COMMENT='字典类型表';

-- ----------------------------
-- 字典数据表
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
    `dict_code`   BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典编码',
    `dict_sort`   INT DEFAULT 0 COMMENT '排序',
    `dict_label`  VARCHAR(100) DEFAULT '' COMMENT '字典标签',
    `dict_value`  VARCHAR(100) DEFAULT '' COMMENT '字典键值',
    `dict_type`   VARCHAR(100) DEFAULT '' COMMENT '字典类型',
    `css_class`   VARCHAR(100) DEFAULT NULL COMMENT '样式',
    `list_class`  VARCHAR(100) DEFAULT NULL COMMENT '表格样式',
    `is_default`  CHAR(1) DEFAULT 'N' COMMENT '是否默认',
    `status`      CHAR(1) DEFAULT '0' COMMENT '状态',
    `create_by`   VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by`   VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`dict_code`)
) ENGINE=InnoDB COMMENT='字典数据表';

-- ----------------------------
-- 操作日志表
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log` (
    `oper_id`       BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `title`         VARCHAR(50) DEFAULT '' COMMENT '模块标题',
    `business_type` INT DEFAULT 0 COMMENT '业务类型',
    `method`        VARCHAR(100) DEFAULT '' COMMENT '方法名',
    `request_method` VARCHAR(10) DEFAULT '' COMMENT '请求方式',
    `operator_type` INT DEFAULT 0 COMMENT '操作类别',
    `oper_name`     VARCHAR(50) DEFAULT '' COMMENT '操作人',
    `oper_url`      VARCHAR(255) DEFAULT '' COMMENT '请求URL',
    `oper_ip`       VARCHAR(128) DEFAULT '' COMMENT '请求IP',
    `oper_param`    VARCHAR(2000) DEFAULT '' COMMENT '请求参数',
    `json_result`   VARCHAR(2000) DEFAULT '' COMMENT '返回结果',
    `status`        INT DEFAULT 0 COMMENT '状态（0正常 1异常）',
    `error_msg`     VARCHAR(2000) DEFAULT '' COMMENT '错误信息',
    `oper_time`     DATETIME DEFAULT NULL COMMENT '操作时间',
    `cost_time`     BIGINT DEFAULT 0 COMMENT '耗时',
    PRIMARY KEY (`oper_id`),
    KEY `idx_oper_time` (`oper_time`)
) ENGINE=InnoDB COMMENT='操作日志表';

-- ----------------------------
-- 系统配置参数表
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
    `config_id`    BIGINT NOT NULL AUTO_INCREMENT COMMENT '参数ID',
    `config_name`  VARCHAR(100) DEFAULT '' COMMENT '参数名称',
    `config_key`   VARCHAR(100) DEFAULT '' COMMENT '参数键名',
    `config_value` VARCHAR(500) DEFAULT '' COMMENT '参数键值',
    `config_type`  CHAR(1) DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
    `create_by`    VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time`  DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_by`    VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time`  DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`config_id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB COMMENT='系统配置参数表';

-- =============================================
-- 初始化数据
-- =============================================

-- 默认部门
INSERT INTO `sys_dept` VALUES (1, 0, '0', '总部', 0, 'admin', '15888888888', '0', 0, NULL, 'system', NOW(), '', NULL);
INSERT INTO `sys_dept` VALUES (2, 1, '0,1', '研发部', 1, NULL, NULL, '0', 0, NULL, 'system', NOW(), '', NULL);
INSERT INTO `sys_dept` VALUES (3, 1, '0,1', '运营部', 2, NULL, NULL, '0', 0, NULL, 'system', NOW(), '', NULL);

-- 默认管理员 (密码: admin123, 实际部署时请加密)
INSERT INTO `sys_user` VALUES (1, 1, 'admin', '超级管理员', 'admin@sc.com', '15888888888', '0', '', 'admin123', '0', 0, NULL, 'system', NOW(), '', NULL);

-- 默认角色
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'admin', 1, 1, '0', 0, NULL, 'system', NOW(), '', NULL);
INSERT INTO `sys_role` VALUES (2, '普通用户', 'common', 2, 5, '0', 0, NULL, 'system', NOW(), '', NULL);

-- 默认用户角色关联
INSERT INTO `sys_user_role` VALUES (1, 1);

-- 默认菜单
INSERT INTO `sys_menu` VALUES (1, '系统管理', 0, 1, 'system', NULL, 'M', '0', '0', NULL, 'system', 'system', NOW(), '', NULL);
INSERT INTO `sys_menu` VALUES (100, '用户管理', 1, 1, 'user', 'system/user/index', 'C', '0', '0', 'system:user:list', 'user', 'system', NOW(), '', NULL);
INSERT INTO `sys_menu` VALUES (101, '角色管理', 1, 2, 'role', 'system/role/index', 'C', '0', '0', 'system:role:list', 'peoples', 'system', NOW(), '', NULL);
INSERT INTO `sys_menu` VALUES (102, '菜单管理', 1, 3, 'menu', 'system/menu/index', 'C', '0', '0', 'system:menu:list', 'tree-table', 'system', NOW(), '', NULL);
INSERT INTO `sys_menu` VALUES (103, '部门管理', 1, 4, 'dept', 'system/dept/index', 'C', '0', '0', 'system:dept:list', 'tree', 'system', NOW(), '', NULL);

-- 用户管理按钮权限
INSERT INTO `sys_menu` VALUES (1001, '用户查询', 100, 1, '', NULL, 'F', '0', '0', 'system:user:query', '#', 'system', NOW(), '', NULL);
INSERT INTO `sys_menu` VALUES (1002, '用户新增', 100, 2, '', NULL, 'F', '0', '0', 'system:user:add', '#', 'system', NOW(), '', NULL);
INSERT INTO `sys_menu` VALUES (1003, '用户修改', 100, 3, '', NULL, 'F', '0', '0', 'system:user:edit', '#', 'system', NOW(), '', NULL);
INSERT INTO `sys_menu` VALUES (1004, '用户删除', 100, 4, '', NULL, 'F', '0', '0', 'system:user:remove', '#', 'system', NOW(), '', NULL);

-- 角色菜单关联（超级管理员拥有全部权限）
INSERT INTO `sys_role_menu` VALUES (1, 1);
INSERT INTO `sys_role_menu` VALUES (1, 100);
INSERT INTO `sys_role_menu` VALUES (1, 101);
INSERT INTO `sys_role_menu` VALUES (1, 102);
INSERT INTO `sys_role_menu` VALUES (1, 103);
INSERT INTO `sys_role_menu` VALUES (1, 1001);
INSERT INTO `sys_role_menu` VALUES (1, 1002);
INSERT INTO `sys_role_menu` VALUES (1, 1003);
INSERT INTO `sys_role_menu` VALUES (1, 1004);

-- Demo 数据库
CREATE DATABASE IF NOT EXISTS `sc_demo` DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
