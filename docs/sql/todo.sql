CREATE TABLE `todo` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(500) NOT NULL COMMENT 'Todo事项内容',
  `status` varchar(20) NOT NULL DEFAULT 'TODO' COMMENT 'TODO待办、IN_PROGRESS进行中、DONE已结束',
  `priority` tinyint NOT NULL DEFAULT 1 COMMENT '优先级星级，1到5',
  `sort` int NOT NULL DEFAULT 0 COMMENT '同状态下排序值',
  `is_published` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否公开',
  `discovered_time` datetime NOT NULL COMMENT '发现时间',
  `completed_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `update_time` datetime NOT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_todo_status_sort` (`status`, `sort`),
  INDEX `idx_todo_published_status_sort` (`is_published`, `status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
