-- 私信功能初始化脚本（兼容 MySQL 5.7/8.0）
-- 执行前请确认当前数据库：USE your_database;

CREATE TABLE IF NOT EXISTS `private_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `from_user_id` BIGINT NOT NULL COMMENT '发送方用户ID（业务user_id）',
  `to_user_id` BIGINT NOT NULL COMMENT '接收方用户ID（业务user_id）',
  `content` VARCHAR(500) NOT NULL COMMENT '消息内容',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读:0未读,1已读',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_pair_time` (`from_user_id`, `to_user_id`, `create_time`),
  KEY `idx_to_read` (`to_user_id`, `is_read`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户私信表';
