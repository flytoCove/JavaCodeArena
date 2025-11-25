DROP TABLE IF EXISTS `tb_sys_user`;
CREATE TABLE `tb_sys_user` (
                               `user_id` bigint unsigned NOT NULL COMMENT '用户id',
                               `user_account` varchar(20) DEFAULT NULL COMMENT '用户账号',
                               `password` varchar(20) DEFAULT NULL COMMENT '用户密码',
                               `nick_name` varchar(32) DEFAULT NULL COMMENT '昵称',
                               `create_by` bigint NOT NULL COMMENT '创建用户',
                               `create_time` datetime NOT NULL COMMENT '创建时间',
                               `update_by` bigint DEFAULT NULL COMMENT '更新用户',
                               `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                               PRIMARY KEY (`user_id`),
                               UNIQUE KEY `user_account` (`user_account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理端用户表'