/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80408 (8.4.8)
 Source Host           : localhost:3306
 Source Schema         : hypblog

 Target Server Type    : MySQL
 Target Server Version : 80408 (8.4.8)
 File Encoding         : 65001

 Date: 02/09/2026 20:42:22
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for todo
-- ----------------------------
DROP TABLE IF EXISTS `todo`;
CREATE TABLE `todo`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Todo事项内容',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'TODO' COMMENT 'TODO待办、IN_PROGRESS进行中、DONE已结束',
  `priority` tinyint NOT NULL DEFAULT 1 COMMENT '优先级星级，1到5',
  `sort` int NOT NULL DEFAULT 0 COMMENT '同状态下排序值',
  `is_published` bit(1) NOT NULL DEFAULT b'1' COMMENT '是否公开',
  `discovered_time` datetime NOT NULL COMMENT '发现时间',
  `completed_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `update_time` datetime NOT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_todo_status_sort`(`status` ASC, `sort` ASC) USING BTREE,
  INDEX `idx_todo_published_status_sort`(`is_published` ASC, `status` ASC, `sort` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of todo
-- ----------------------------
INSERT INTO `todo` VALUES (1, 'test', 'TODO', 4, 2, b'1', '2026-08-18 17:56:58', NULL, '2026-09-01 19:40:17');
INSERT INTO `todo` VALUES (2, '修复：在后端写博客的时候会出现右边预览已经换行，但是文章发布出去以后的效果是不换行的。', 'TODO', 1, 1, b'1', '2026-08-18 19:05:30', NULL, '2026-09-01 19:40:17');
INSERT INTO `todo` VALUES (3, '顶部导航栏建立一个常用网站集合，放一些我常用的网站（常用网站最好是一条一条的，然后样式多样化一点，最好每一条都不同，可以自定义）', 'TODO', 1, 0, b'1', '2026-08-18 19:05:59', NULL, '2026-09-01 19:40:17');
INSERT INTO `todo` VALUES (4, '倒计时，提交日历，统计，参考https://www.jerry-nis.top/', 'TODO', 1, 3, b'1', '2026-08-18 19:07:00', NULL, '2026-09-01 19:40:17');
INSERT INTO `todo` VALUES (5, '创建todo页面', 'IN_PROGRESS', 5, 0, b'1', '2026-08-20 20:24:42', NULL, '2026-09-01 19:40:17');

SET FOREIGN_KEY_CHECKS = 1;
