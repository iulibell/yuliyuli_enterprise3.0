-- 评论点赞与评论回复能力初始化脚本（兼容 MySQL 5.7/8.0）
-- 执行前请确认当前数据库：USE your_database;

-- 1) 补齐 comment.parent_id 字段（用于回复）
SET @has_parent_id := (
  SELECT COUNT(1)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'comment'
    AND COLUMN_NAME = 'parent_id'
);
SET @sql_parent_id := IF(
  @has_parent_id = 0,
  'ALTER TABLE `comment` ADD COLUMN `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT ''父评论ID，0表示一级评论''',
  'SELECT ''skip add parent_id'' '
);
PREPARE stmt_parent_id FROM @sql_parent_id;
EXECUTE stmt_parent_id;
DEALLOCATE PREPARE stmt_parent_id;

-- 2) 补齐 comment_like 表（用于评论点赞）
CREATE TABLE IF NOT EXISTS `comment_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `comment_id` BIGINT NOT NULL COMMENT '评论ID',
  `user_id` BIGINT NOT NULL COMMENT '点赞用户ID（业务user_id）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论点赞表';

-- 3) 可选：给 comment.parent_id 建索引，加快回复查询
SET @has_parent_idx := (
  SELECT COUNT(1)
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'comment'
    AND INDEX_NAME = 'idx_comment_parent_id'
);
SET @sql_parent_idx := IF(
  @has_parent_idx = 0,
  'CREATE INDEX `idx_comment_parent_id` ON `comment` (`parent_id`)',
  'SELECT ''skip create idx_comment_parent_id'' '
);
PREPARE stmt_parent_idx FROM @sql_parent_idx;
EXECUTE stmt_parent_idx;
DEALLOCATE PREPARE stmt_parent_idx;

-- 4) 可选：修复历史脏数据（parent_id 为空时置为0）
UPDATE `comment` SET `parent_id` = 0 WHERE `parent_id` IS NULL;
