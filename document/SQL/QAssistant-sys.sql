drop schema if exists 'QAssistant';
create schema 'QAssistant' default character set utf8mb4 collate utf8mb4_general_ci;
use QAssistant;

DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log`  (
                            `id` bigint(20) NOT NULL COMMENT '主键',
                            `user_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户id',
                            `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户名',
                            `operation` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户操作：DELETE ADD GET UPDATE',
                            `time` int(11) NULL DEFAULT NULL COMMENT '响应时间,单位毫秒',
                            `method` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求方法（控制层方法全限定名）',
                            `params` varchar(5000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求参数',
                            `ip` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'IP地址',
                            `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
                            PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '系统日志' ROW_FORMAT = COMPACT;

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
                             `id` bigint(20) NOT NULL COMMENT '用户id',
                             `username` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '账户',
                             `password` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '用户密码密文',
                             `phone` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机号码',
                             `real_name` varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '真实名称',
                             `nick_name` varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '昵称',
                             `email` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '邮箱(唯一)',
                             `status` tinyint(4) NULL DEFAULT 1 COMMENT '账户状态(1.正常 2.锁定 )',
                             `sex` tinyint(4) NULL DEFAULT 0 COMMENT '性别(0.未知 1.男 2.女)',
                             `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '是否删除(0未删除；1已删除)',
                             `create_id` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
                             `update_id` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
                             `create_where` tinyint(4) NULL DEFAULT 1 COMMENT '创建来源(1.web 2.android 3.ios )',
                             `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
                             `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
                             PRIMARY KEY (`id`) USING BTREE,
                             UNIQUE INDEX `unique_username`(`username`) USING BTREE COMMENT '用户名唯一',
                             UNIQUE INDEX `unique_email`(`email`) USING BTREE COMMENT '邮箱唯一'
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户表' ROW_FORMAT = COMPACT;

DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
                                  `id` bigint(20) NOT NULL COMMENT '主键',
                                  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户id',
                                  `role_id` bigint(20) NULL DEFAULT NULL COMMENT '角色id',
                                  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
                                  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '用户角色表' ROW_FORMAT = COMPACT;

DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
                             `id` bigint(20) NOT NULL COMMENT '主键',
                             `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '角色名称',
                             `description` varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '描述',
                             `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态(0:正常 1:弃用)',
                             `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
                             `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
                             `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '是否删除(0未删除；1已删除)',
                             PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '角色表' ROW_FORMAT = COMPACT;

DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
                                   `id` bigint(20) NOT NULL COMMENT '主键',
                                   `code` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '菜单权限编码(前端按钮权限标识)',
                                   `title` varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '菜单权限名称',
                                   `icon` varchar(60) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT '菜单图标(侧边导航栏图标)',
                                   `perms` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'SpringSecurity授权标识(如：sys:user:add)',
                                   `url` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '访问地址URL',
                                   `method` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '资源请求类型',
                                   `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '' COMMENT 'name与前端vue路由name约定一致',
                                   `pid` bigint(20) NULL DEFAULT 0 COMMENT '父级菜单权限id，pid等于0 为顶层权限',
                                   `order_num` int(11) NULL DEFAULT 0 COMMENT '排序',
                                   `type` tinyint(4) UNSIGNED ZEROFILL NULL DEFAULT 0001 COMMENT '菜单权限类型(1:目录;2:菜单;3:按钮)',
                                   `status` tinyint(4) NULL DEFAULT 0 COMMENT '状态(0:正常 1:禁用)',
                                   `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
                                   `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
                                   `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '是否删除(0未删除；1已删除)',
                                   PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '权限表（菜单）' ROW_FORMAT = COMPACT;



DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
                                        `id` bigint(20) NOT NULL COMMENT '主键',
                                        `role_id` bigint(20) NULL DEFAULT NULL COMMENT '角色id',
                                        `permission_id` bigint(20) NULL DEFAULT NULL COMMENT '菜单权限id',
                                        `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
                                        PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '角色权限表' ROW_FORMAT = COMPACT;
