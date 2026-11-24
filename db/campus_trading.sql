/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80043
 Source Host           : localhost:3306
 Source Schema         : campus_trading

 Target Server Type    : MySQL
 Target Server Version : 80043
 File Encoding         : 65001

 Date: 22/11/2025 12:17:00
*/

-- ----------------------------
-- 创建数据库
-- ----------------------------
CREATE DATABASE IF NOT EXISTS `campus_trading` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `campus_trading`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admins
-- ----------------------------
DROP TABLE IF EXISTS `admins`;
CREATE TABLE `admins`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '管理员用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ADMIN' COMMENT '角色：ADMIN管理员（拥有所有权限）',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态：1正常，0禁用',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `login_count` int NOT NULL DEFAULT 0 COMMENT '登录次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '管理员表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of admins
-- ----------------------------
INSERT INTO `admins` VALUES (6, 'admin', '$2a$10$mkpEtOPmF.KIHvNaWQ/WWOX6tZrrMxSJWEQrNUpw9yuBS9e/4LITa', '系统管理员', 'admin@campus.com', '13800000000', NULL, 'ADMIN', 1, '2025-11-22 12:14:01', '0:0:0:0:0:0:0:1', 11, '2025-11-21 19:31:28', '2025-11-22 12:14:01');
INSERT INTO `admins` VALUES (11, 'manager1', 'password123', '张经理', 'manager1@campus.com', NULL, NULL, 'ADMIN', 1, NULL, NULL, 0, '2025-11-21 22:04:45', '2025-11-21 22:04:45');
INSERT INTO `admins` VALUES (12, 'manager2', 'password123', '李经理', 'manager2@campus.com', NULL, NULL, 'ADMIN', 1, NULL, NULL, 0, '2025-11-21 22:04:45', '2025-11-21 22:04:45');
INSERT INTO `admins` VALUES (13, 'operator1', 'password123', '王操作员', 'operator1@campus.com', NULL, NULL, 'ADMIN', 0, NULL, NULL, 0, '2025-11-21 22:04:45', '2025-11-21 22:04:45');

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父分类ID',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `status` int NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品分类表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of categories
-- ----------------------------
INSERT INTO `categories` VALUES (1, '电子产品', 0, 1, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (2, '图书教材', 0, 2, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (3, '生活用品', 0, 3, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (4, '服装配饰', 0, 4, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (5, '运动健身', 0, 5, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (6, '其他', 0, 6, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (7, '手机数码', 1, 1, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (8, '电脑配件', 1, 2, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (9, '耳机音响', 1, 3, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (10, '智能设备', 1, 4, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (11, '专业教材', 2, 1, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (12, '考试资料', 2, 2, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (13, '课外读物', 2, 3, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (14, '工具书', 2, 4, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (15, '宿舍用品', 3, 1, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (16, '护肤美妆', 3, 2, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (17, '食品零食', 3, 3, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (18, '日用百货', 3, 4, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (19, '男装', 4, 1, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (20, '女装', 4, 2, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (21, '鞋帽', 4, 3, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (22, '配饰', 4, 4, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (23, '运动器材', 5, 1, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (24, '运动服装', 5, 2, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (25, '户外用品', 5, 3, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11');
INSERT INTO `categories` VALUES (26, '电子产品', NULL, 1, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (27, '服装配饰', NULL, 2, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (28, '图书文具', NULL, 3, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (29, '生活用品', NULL, 4, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (30, '运动户外', NULL, 5, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (31, '美妆护肤', NULL, 6, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (32, '食品饮料', NULL, 7, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (33, '其他物品', NULL, 8, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (34, '手机数码', 26, 1, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (35, '电脑配件', 26, 2, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (36, '耳机音响', 26, 3, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (37, '智能设备', 26, 4, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (38, '男装', 27, 1, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (39, '女装', 27, 2, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (40, '鞋靴', 27, 3, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (41, '包包配饰', 27, 4, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (42, '教材教辅', 28, 1, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (43, '小说文学', 28, 2, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (44, '学习用品', 28, 3, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (45, '办公用品', 28, 4, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (46, '家居用品', 29, 1, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (47, '厨房用品', 29, 2, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (48, '清洁用品', 29, 3, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (49, '收纳整理', 29, 4, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (50, '运动服装', 30, 1, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (51, '运动器材', 30, 2, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (52, '户外装备', 30, 3, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (53, '健身用品', 30, 4, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (54, '护肤品', 31, 1, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (55, '彩妆', 31, 2, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (56, '个人护理', 31, 3, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');
INSERT INTO `categories` VALUES (57, '香水', 31, 4, 1, '2025-11-21 21:09:58', '2025-11-21 21:09:58');



-- ----------------------------
-- Table structure for favorites
-- ----------------------------
DROP TABLE IF EXISTS `favorites`;
CREATE TABLE `favorites`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime(6) NOT NULL,
  `product_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKgh1s14hhb9qb8p2do933hscsf`(`user_id` ASC, `product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of favorites
-- ----------------------------



-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `buyer_id` bigint NOT NULL COMMENT '买家ID',
  `seller_id` bigint NOT NULL COMMENT '卖家ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `amount` decimal(10, 2) NOT NULL COMMENT '交易金额',
  `status` int NULL DEFAULT NULL,
  `buyer_confirm` int NULL DEFAULT NULL,
  `seller_confirm` int NULL DEFAULT NULL,
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `cancel_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `cancel_time` datetime(6) NULL DEFAULT NULL,
  `complete_time` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_orders_buyer_id`(`buyer_id` ASC) USING BTREE,
  INDEX `idx_orders_seller_id`(`seller_id` ASC) USING BTREE,
  INDEX `idx_orders_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_orders_status`(`status` ASC) USING BTREE,
  INDEX `idx_orders_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_orders_order_no`(`order_no` ASC) USING BTREE,
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`buyer_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `orders_ibfk_2` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `orders_ibfk_3` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (5, 'ORD20251119001', 8, 1, 1, 299.00, 2, 1, 1, '交易顺利', '2025-11-15 10:30:00', '2025-11-16 14:20:00', NULL, NULL, NULL);
INSERT INTO `orders` VALUES (6, 'ORD20251119002', 8, 2, 2, 150.00, 2, 1, 1, '商品很好', '2025-11-16 09:15:00', '2025-11-17 11:30:00', NULL, NULL, NULL);
INSERT INTO `orders` VALUES (7, 'ORD20251119003', 8, 3, 3, 88.50, 2, 1, 1, NULL, '2025-11-17 14:20:00', '2025-11-18 09:45:00', NULL, NULL, NULL);
INSERT INTO `orders` VALUES (8, 'ORD20251119004', 8, 4, 4, 199.99, 0, 0, 0, NULL, '2025-11-19 11:52:02', '2025-11-19 11:52:02', NULL, NULL, NULL);
INSERT INTO `orders` VALUES (9, 'ORD20251119005', 8, 5, 5, 45.00, 1, 1, 1, '需要尽快发货', '2025-11-19 11:52:02', '2025-11-19 11:52:02', NULL, NULL, NULL);
INSERT INTO `orders` VALUES (10, 'ORD20251118001', 8, 1, 6, 320.00, 2, 1, 1, '很满意', '2025-11-14 16:40:00', '2025-11-15 10:20:00', NULL, NULL, NULL);
INSERT INTO `orders` VALUES (11, 'ORD20251118002', 8, 2, 7, 128.00, 2, 1, 1, NULL, '2025-11-13 11:25:00', '2025-11-14 15:30:00', NULL, NULL, NULL);
INSERT INTO `orders` VALUES (12, 'ORD20251118003', 8, 3, 8, 75.00, 3, 0, 0, NULL, '2025-11-12 09:10:00', '2025-11-21 20:17:32', '不需要了', NULL, '2025-11-12 10:15:00.000000');
INSERT INTO `orders` VALUES (13, 'ORD20251117001', 8, 4, 9, 256.00, 2, 1, 1, '物美价廉', '2025-11-11 13:50:00', '2025-11-12 16:20:00', NULL, NULL, NULL);
INSERT INTO `orders` VALUES (14, 'ORD20251117002', 8, 5, 10, 89.90, 2, 1, 1, '好评', '2025-11-10 15:30:00', '2025-11-11 10:45:00', NULL, NULL, NULL);
INSERT INTO `orders` VALUES (15, 'ORD20251110001', 8, 1, 11, 450.00, 2, 1, 1, NULL, '2025-11-08 10:20:00', '2025-11-09 14:30:00', NULL, NULL, NULL);
INSERT INTO `orders` VALUES (16, 'ORD20251110002', 8, 2, 12, 180.00, 2, 1, 1, '非常好', '2025-11-07 09:15:00', '2025-11-08 11:20:00', NULL, NULL, NULL);
INSERT INTO `orders` VALUES (17, 'ORD20251105001', 8, 3, 13, 99.00, 2, 1, 1, NULL, '2025-11-05 14:40:00', '2025-11-06 09:50:00', NULL, NULL, NULL);
INSERT INTO `orders` VALUES (18, 'ORD20251105002', 8, 4, 14, 220.00, 2, 1, 1, '推荐', '2025-11-04 11:30:00', '2025-11-05 15:20:00', NULL, NULL, NULL);
INSERT INTO `orders` VALUES (19, 'ORD20251103001', 8, 5, 15, 135.50, 2, 1, 1, NULL, '2025-11-02 16:20:00', '2025-11-03 10:30:00', NULL, NULL, NULL);
INSERT INTO `orders` VALUES (20, 'ORD202411210001', 1, 2, 1, 299.00, 3, 1, 1, '希望尽快发货', '2025-11-21 20:17:32', '2025-11-21 21:05:24', NULL, NULL, '2025-11-21 21:05:24.381035');
INSERT INTO `orders` VALUES (22, 'ORD202411210003', 3, 1, 3, 89.50, 1, 1, 0, '已付款，等待卖家确认', '2025-11-21 20:17:32', '2025-11-21 20:17:32', NULL, NULL, NULL);
INSERT INTO `orders` VALUES (23, 'ORD202411200001', 1, 3, 4, 199.00, 2, 1, 1, '双方已确认，交易进行中', '2025-11-20 20:17:32', '2025-11-21 20:17:32', NULL, NULL, NULL);
INSERT INTO `orders` VALUES (24, 'ORD202411200002', 2, 1, 5, 45.00, 2, 0, 1, '卖家已确认，等待买家确认', '2025-11-20 20:17:32', '2025-11-21 20:17:32', NULL, NULL, NULL);
INSERT INTO `orders` VALUES (25, 'ORD202411200003', 3, 2, 6, 128.80, 2, 1, 1, '商品已交付，等待最终确认', '2025-11-20 20:17:32', '2025-11-21 20:17:32', NULL, NULL, NULL);
INSERT INTO `orders` VALUES (26, 'ORD202411190001', 1, 2, 7, 350.00, 3, 1, 1, '交易顺利完成', '2025-11-19 20:17:32', '2025-11-21 20:17:32', NULL, NULL, '2025-11-20 20:17:32.000000');
INSERT INTO `orders` VALUES (27, 'ORD202411190002', 2, 3, 8, 75.50, 3, 1, 1, '商品质量很好，满意', '2025-11-19 20:17:32', '2025-11-21 20:17:32', NULL, NULL, '2025-11-20 20:17:32.000000');
INSERT INTO `orders` VALUES (28, 'ORD202411190003', 3, 1, 9, 220.00, 3, 1, 1, '快速交易，推荐卖家', '2025-11-19 20:17:32', '2025-11-21 20:17:32', NULL, NULL, '2025-11-20 20:17:32.000000');
INSERT INTO `orders` VALUES (29, 'ORD202411180001', 1, 3, 10, 99.99, 3, 1, 1, '物美价廉', '2025-11-18 20:17:32', '2025-11-21 20:17:32', NULL, NULL, '2025-11-19 20:17:32.000000');
INSERT INTO `orders` VALUES (30, 'ORD202411170001', 2, 1, 11, 180.00, 4, 0, 0, '买家临时有事取消', '2025-11-17 20:17:32', '2025-11-21 20:17:32', '测试取消原因', '2025-11-18 20:17:32.000000', NULL);
INSERT INTO `orders` VALUES (31, 'ORD202411170002', 3, 2, 12, 65.00, 4, 0, 0, '商品有瑕疵，协商取消', '2025-11-17 20:17:32', '2025-11-21 20:17:32', '测试取消原因', '2025-11-18 20:17:32.000000', NULL);
INSERT INTO `orders` VALUES (32, 'ORD202411160001', 1, 2, 13, 420.00, 3, 1, 1, '高价值商品交易成功', '2025-11-16 20:17:32', '2025-11-21 20:17:32', NULL, NULL, '2025-11-17 20:17:32.000000');
INSERT INTO `orders` VALUES (33, 'ORD202411160002', 2, 3, 14, 35.00, 2, 1, 0, '小额交易', '2025-11-16 20:17:32', '2025-11-21 20:17:32', NULL, NULL, NULL);
INSERT INTO `orders` VALUES (34, 'ORD202411150001', 3, 1, 15, 150.00, 1, 0, 0, '新订单', '2025-11-15 20:17:32', '2025-11-15 20:17:32', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for products
-- ----------------------------
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `user_id` bigint NOT NULL COMMENT '发布用户ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '商品描述',
  `price` decimal(10, 2) NOT NULL COMMENT '价格',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '原价',
  `images` json NULL COMMENT '商品图片JSON数组',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览次数',
  `status` int NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `brand` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `condition` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `include_invoice` bit(1) NULL DEFAULT NULL,
  `include_warranty` bit(1) NULL DEFAULT NULL,
  `is_negotiable` bit(1) NULL DEFAULT NULL,
  `model` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `purchase_location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `purchase_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `size` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `tags` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `trade_notes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_products_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_products_category_id`(`category_id` ASC) USING BTREE,
  INDEX `idx_products_status`(`status` ASC) USING BTREE,
  INDEX `idx_products_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_products_price`(`price` ASC) USING BTREE,
  INDEX `idx_products_title`(`title` ASC) USING BTREE,
  CONSTRAINT `products_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `products_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of products
-- ----------------------------
INSERT INTO `products` VALUES (1, 1, 7, 'iPhone 14 Pro 256GB 深空黑', '自用iPhone 14 Pro，9成新，无磕碰，功能完好。原装充电器和数据线齐全，有原装盒子。因换新机出售，支持当面验机。屏幕无划痕，电池健康度92%，Face ID功能正常。', 5999.00, 7999.00, '[\"https://picsum.photos/id/160/800/600\"]', 156, 1, '2025-10-20 10:30:00', '2025-10-26 15:20:00', '苹果', '深空黑', '九成新', b'1', b'1', b'1', 'iPhone 14 Pro', '京东自营', '2023年5月', NULL, '自用,无拆修,原装配件', '仅支持当面交易，可在校内约见验机');
INSERT INTO `products` VALUES (2, 2, 8, '联想小新Pro 16 2023款', '联想小新Pro 16，R7-7840HS处理器，32GB内存，1TB固态硬盘，2.5K 120Hz屏幕。性能强劲，适合编程、设计、游戏。外观完好无划痕，电池健康度95%以上。随机附赠鼠标和电脑包。', 4500.00, 6499.00, '[\"https://picsum.photos/id/0/800/600\"]', 89, 1, '2025-10-18 14:20:00', '2025-10-26 16:10:00', '联想', '星辰灰', '九成新', b'1', b'1', b'1', '小新Pro 16 2023', '联想官网', '2023年8月', '16英寸', 'R7,32G,高性能', '包装盒和所有配件齐全，支持验机');
INSERT INTO `products` VALUES (3, 3, 9, 'Sony WH-1000XM5 降噪耳机', 'Sony顶级降噪耳机，音质出色，降噪效果一流。使用不到半年，成色极新。原装包装、充电线、飞机转接头全部齐全。支持LDAC高清音频传输，电池续航30小时+。', 1899.00, 2499.00, '[\"https://picsum.photos/id/1/800/600\"]', 67, 1, '2025-10-19 09:15:00', '2025-10-26 14:30:00', '索尼', '黑色', '九五成新', b'1', b'1', b'0', 'WH-1000XM5', '天猫旗舰店', '2024年6月', NULL, '降噪,无线,蓝牙', '包装齐全，支持试听');
INSERT INTO `products` VALUES (4, 4, 8, '机械键盘 Cherry MX青轴', 'Cherry原厂青轴机械键盘，87键全键无冲突，手感极佳。清脆的打字声音，适合打字和编程。键帽无打油，使用半年左右，PBT键帽材质耐用。', 299.00, 599.00, '[\"https://picsum.photos/id/2/800/600\"]', 45, 1, '2025-10-21 16:40:00', '2025-10-26 13:20:00', 'Cherry', '黑色', '九成新', b'0', b'0', b'1', 'MX Board 3.0', '淘宝', '2024年3月', '87键', '机械键盘,青轴,无冲', '键盘功能完好，可当面试用');
INSERT INTO `products` VALUES (5, 5, 10, '小米手环8 NFC版', '小米手环8 NFC版，支持门禁卡模拟、公交卡、支付宝支付。几乎全新，带原装充电线和说明书。支持血氧监测、心率监测、睡眠监测等健康功能。', 189.00, 299.00, '[\"https://picsum.photos/id/3/800/600\"]', 34, 1, '2025-10-22 11:20:00', '2025-10-26 12:00:00', '小米', '黑色', '九五成新', b'0', b'0', b'0', '小米手环8 NFC', '小米官网', '2024年9月', NULL, 'NFC,运动手环', '功能完好，外观无划痕');
INSERT INTO `products` VALUES (6, 1, 7, 'iPad Air 5 64GB WiFi版', 'iPad Air 5代，M1芯片，64GB存储。屏幕完美无划痕，配有原装充电器和数据线。附赠保护壳和钢化膜。适合学习、笔记、娱乐。', 3299.00, 4799.00, '[\"https://picsum.photos/id/4/800/600\"]', 72, 1, '2025-10-19 13:25:00', '2025-10-26 16:45:00', '苹果', '星光色', '九成新', b'1', b'1', b'1', 'iPad Air 5', '苹果官网', '2023年9月', '10.9英寸', 'M1芯片,平板', '赠送保护壳和钢化膜');
INSERT INTO `products` VALUES (7, 2, 8, '罗技 MX Master 3S 无线鼠标', '罗技旗舰办公鼠标，人体工学设计，8000DPI，支持多设备切换。静音微动开关，续航70天。适合设计师、程序员使用。', 549.00, 799.00, '[\"https://picsum.photos/id/5/800/600\"]', 28, 1, '2025-10-23 09:50:00', '2025-10-26 10:15:00', '罗技', '石墨黑', '九五成新', b'1', b'0', b'0', 'MX Master 3S', '京东', '2024年5月', NULL, '无线鼠标,办公', '原装充电线和接收器齐全');
INSERT INTO `products` VALUES (8, 2, 11, '数据结构与算法分析（Java语言描述）', '计算机专业经典教材，Mark Allen Weiss著。书本九成新，有少量笔记和重点标记，不影响阅读。适合数据结构课程学习，配套源代码可下载。', 45.00, 89.00, '[\"https://picsum.photos/id/24/800/600\"]', 78, 1, '2025-10-15 10:00:00', '2025-10-26 11:30:00', '机械工业出版社', NULL, '九成新', b'0', b'0', b'1', NULL, '当当网', '2023年9月', NULL, '计算机,数据结构,教材', '包邮，支持线下交易');
INSERT INTO `products` VALUES (9, 3, 11, '深入理解计算机系统（第3版）', 'CSAPP经典教材，中文版，几乎全新。这本书对理解计算机底层原理非常有帮助，是计算机专业必读书籍。涵盖程序结构、处理器体系结构、优化等内容。', 89.00, 139.00, '[\"https://picsum.photos/id/20/800/600\"]', 92, 1, '2025-10-16 14:30:00', '2025-10-26 10:50:00', '机械工业出版社', NULL, '九五成新', b'0', b'0', b'0', NULL, '京东', '2024年2月', NULL, '计算机系统,教材', '书本无损坏，无笔记');
INSERT INTO `products` VALUES (10, 1, 12, '考研英语真题详解（近10年）', '考研英语一真题详解，包含2015-2024年真题及详细解析。书本干净整洁，几乎没做过题，适合25-26考研学生。每道题都有详细的解题思路。', 35.00, 68.00, '[\"https://picsum.photos/id/21/800/600\"]', 56, 1, '2025-10-17 09:20:00', '2025-10-26 09:40:00', NULL, NULL, '九五成新', b'0', b'0', b'1', NULL, '淘宝', '2024年5月', NULL, '考研,英语,真题', '可小刀，诚心要可优惠');
INSERT INTO `products` VALUES (11, 4, 13, '三体（全套3册）', '刘慈欣科幻巨著《三体》三部曲，正版图书。看过一遍，保存完好。科幻迷必读佳作，曾获雨果奖。包含《三体》《黑暗森林》《死神永生》三本。', 55.00, 108.00, '[\"https://picsum.photos/id/22/800/600\"]', 43, 1, '2025-10-18 15:50:00', '2025-10-26 08:30:00', '重庆出版社', NULL, '九成新', b'0', b'0', b'0', NULL, '京东', '2023年12月', NULL, '科幻,小说', '三本书装订完好，无破损');
INSERT INTO `products` VALUES (12, 5, 11, 'Java核心技术 卷I+卷II（原书第11版）', 'Java学习经典书籍，两卷本。涵盖Java基础到高级特性。书本保存完好，适合Java初学者和进阶学习。代码示例丰富，讲解详细。', 128.00, 198.00, '[\"https://picsum.photos/id/23/800/600\"]', 64, 1, '2025-10-20 11:40:00', '2025-10-26 14:20:00', '机械工业出版社', NULL, '九成新', b'0', b'0', b'1', NULL, '当当', '2023年10月', NULL, 'Java,编程,教材', '两卷合售，不单卖');
INSERT INTO `products` VALUES (13, 1, 14, '牛津高阶英汉双解词典（第9版）', '英语学习必备工具书，牛津高阶第9版。收词量大，例句丰富。书本保存完好，封面略有磨损但不影响使用。适合大学生、考研学生使用。', 68.00, 158.00, '[\"https://picsum.photos/id/25/800/600\"]', 39, 1, '2025-10-21 14:15:00', '2025-10-26 09:55:00', '商务印书馆', NULL, '八成新', b'0', b'0', b'1', NULL, '书店', '2022年3月', NULL, '词典,英语,工具书', '页面完整，无缺页');
INSERT INTO `products` VALUES (14, 5, 15, '宿舍小冰箱 迷你单门', '美的小冰箱，50L容量，适合宿舍使用。静音设计，制冷效果好。一级能效，省电环保。因毕业离校出售，九成新，功能完好。', 280.00, 499.00, '[\"https://picsum.photos/id/26/800/600\"]', 71, 1, '2025-10-19 10:30:00', '2025-10-26 17:20:00', '美的', '白色', '九成新', b'0', b'0', b'1', 'BC-50M', '苏宁', '2024年3月', '50L', '小冰箱,宿舍', '需自提，功能完好');
INSERT INTO `products` VALUES (15, 1, 16, '雅诗兰黛小棕瓶眼霜 15ml', '雅诗兰黛小棕瓶眼霜15ml，专柜正品。买重复了全新未拆封，保质期到2026年8月。抗衰老、淡化细纹效果显著，适合20岁以上使用。', 380.00, 580.00, '[\"https://picsum.photos/id/27/800/600\"]', 38, 1, '2025-10-20 13:40:00', '2025-10-26 16:50:00', '雅诗兰黛', NULL, '全新', b'1', b'0', b'0', '小棕瓶眼霜', '专柜', '2024年8月', '15ml', '护肤,眼霜,正品', '全新未拆封，可验真伪');
INSERT INTO `products` VALUES (16, 2, 18, '戴森吸尘器 V8 Fluffy', '戴森V8无线吸尘器，功能完好，吸力强劲。配有多个吸头（地板吸头、缝隙吸头、软毛吸头等），适合清洁床铺、地面、车内。因换新款出售。', 1200.00, 2490.00, '[\"https://picsum.photos/id/28/800/600\"]', 52, 1, '2025-10-21 08:50:00', '2025-10-26 15:30:00', '戴森', '紫色', '八成新', b'0', b'1', b'1', 'V8 Fluffy', '天猫旗舰店', '2023年7月', NULL, '吸尘器,无线', '配件齐全，电池续航正常');
INSERT INTO `products` VALUES (17, 3, 15, '学生宿舍四件套 纯棉', '纯棉四件套，包含被套、床单、2个枕套。全棉材质，亲肤透气。洗涤干净，颜色是简约北欧风格。适合1.2米床使用。', 88.00, 199.00, '[\"https://picsum.photos/id/29/800/600\"]', 23, 1, '2025-10-22 15:30:00', '2025-10-26 11:45:00', NULL, '浅灰色', '九成新', b'0', b'0', b'1', NULL, '淘宝', '2024年3月', '1.2m床', '床上用品,纯棉', '已清洗消毒，干净卫生');
INSERT INTO `products` VALUES (18, 4, 16, 'SK-II神仙水 230ml', 'SK-II神仙水230ml，专柜正品。使用了约1/3，效果很好。保质期到2026年3月。改善肌肤暗沉，调理肤质。因换护肤品出售。', 699.00, 1290.00, '[\"https://picsum.photos/id/30/800/600\"]', 31, 1, '2025-10-23 10:20:00', '2025-10-26 13:10:00', 'SK-II', NULL, '九成新', b'1', b'0', b'1', '神仙水 230ml', '专柜', '2024年10月', '230ml', '护肤品,正品', '保质期充足，可验真伪');
INSERT INTO `products` VALUES (19, 5, 18, '飞利浦电动牙刷 HX6730', '飞利浦声波电动牙刷，深度清洁，呵护牙龈。使用4个月，配2个刷头。充电式，续航时间长。带旅行充电盒，方便携带。', 199.00, 499.00, '[\"https://picsum.photos/id/31/800/600\"]', 27, 1, '2025-10-24 16:05:00', '2025-10-26 10:30:00', '飞利浦', '白色', '九成新', b'0', b'1', b'0', 'HX6730', '京东', '2024年6月', NULL, '电动牙刷,口腔护理', '功能完好，赠送新刷头');
INSERT INTO `products` VALUES (20, 3, 19, 'Uniqlo 优衣库 轻型羽绒服', '优衣库男款轻型羽绒服，L码，深蓝色。90%白鸭绒填充，轻便保暖。穿过一个冬天，保暖效果好，外观无破损。适合身高175-180cm，体重65-75kg。', 199.00, 499.00, '[\"https://picsum.photos/id/32/800/600\"]', 29, 1, '2025-10-22 14:20:00', '2025-10-26 14:10:00', '优衣库', '深蓝色', '九成新', b'0', b'0', b'1', NULL, '优衣库门店', '2023年11月', 'L', '羽绒服,男装', '洗涤干净，可试穿');
INSERT INTO `products` VALUES (21, 4, 21, 'Nike Air Max 90 运动鞋', 'Nike Air Max 90 经典款运动鞋，US 9.5码（43码），黑白配色。穿过几次，鞋面鞋底都很新。气垫缓震，舒适耐穿。鞋盒和配件齐全。', 450.00, 899.00, '[\"https://picsum.photos/id/33/800/600\"]', 36, 1, '2025-10-23 16:10:00', '2025-10-26 13:40:00', 'Nike', '黑白', '九五成新', b'1', b'0', b'1', 'Air Max 90', 'Nike官网', '2024年4月', 'US 9.5', '运动鞋,耐克', '鞋盒和配件齐全');
INSERT INTO `products` VALUES (22, 1, 20, 'Zara 女士风衣外套', 'Zara女款风衣，M码，卡其色经典款。版型好看，适合秋冬穿搭。只穿过2-3次，几乎全新。适合身高160-168cm。', 189.00, 399.00, '[\"https://picsum.photos/id/34/800/600\"]', 19, 1, '2025-10-24 11:35:00', '2025-10-26 09:20:00', 'Zara', '卡其色', '九五成新', b'0', b'0', b'1', NULL, 'Zara门店', '2024年9月', 'M', '风衣,女装', '支持试穿，不合适可退');
INSERT INTO `products` VALUES (23, 2, 22, 'Coach 蔻驰单肩包', 'Coach经典款单肩包，正品。黑色荔枝纹皮质，容量大，实用百搭。使用不到半年，五金件无磨损。内部干净整洁，无瑕疵。', 899.00, 2500.00, '[\"https://picsum.photos/id/35/800/600\"]', 42, 1, '2025-10-20 09:45:00', '2025-10-26 15:55:00', 'Coach', '黑色', '九成新', b'1', b'0', b'1', NULL, '专柜', '2024年4月', NULL, '包包,轻奢', '附购买凭证，可验真');
INSERT INTO `products` VALUES (24, 5, 21, 'Adidas 阿迪达斯 贝壳头板鞋', 'Adidas经典贝壳头板鞋，Superstar系列，42码。白色三条纹，百搭款。穿过几次，鞋面干净，鞋底磨损很少。', 299.00, 599.00, '[\"https://picsum.photos/id/36/800/600\"]', 33, 1, '2025-10-25 13:50:00', '2025-10-26 12:35:00', 'Adidas', '白色', '九成新', b'1', b'0', b'0', 'Superstar', 'Adidas官网', '2024年7月', '42', '板鞋,运动', '鞋盒齐全');
INSERT INTO `products` VALUES (25, 5, 23, 'Lululemon 瑜伽垫 加厚防滑', 'Lululemon专业瑜伽垫，5mm加厚款，防滑效果好。双面可用，天然橡胶材质，环保无异味。用过几次，基本全新。适合瑜伽、健身、拉伸训练。', 180.00, 398.00, '[\"https://picsum.photos/id/37/800/600\"]', 41, 1, '2025-10-24 09:30:00', '2025-10-26 12:20:00', 'Lululemon', '紫色', '九五成新', b'0', b'0', b'0', NULL, '官方旗舰店', '2024年7月', '5mm', '瑜伽垫,健身', '配收纳袋，可送瑜伽带');
INSERT INTO `products` VALUES (26, 1, 23, '哑铃套装 可调节20kg', '可调节哑铃套装，单只最大20kg，2.5kg一档调节。适合宿舍健身练胸、练臂。钢制材质，质量可靠，防滑手柄。因毕业出售，九成新。', 280.00, 499.00, '[\"https://picsum.photos/id/38/800/600\"]', 58, 1, '2025-10-25 11:00:00', '2025-10-26 11:10:00', NULL, '黑色', '九成新', b'0', b'0', b'1', NULL, '淘宝', '2024年1月', '20kg', '哑铃,健身器材', '需自提，重量重不方便快递');
INSERT INTO `products` VALUES (27, 2, 24, 'Nike Pro 运动紧身裤', 'Nike Pro系列运动紧身裤，M码，黑色。Dri-FIT速干面料，透气排汗。穿过几次洗过，弹性好，适合跑步、健身、篮球等运动。', 129.00, 299.00, '[\"https://picsum.photos/id/39/800/600\"]', 22, 1, '2025-10-23 08:40:00', '2025-10-26 08:50:00', 'Nike', '黑色', '九成新', b'0', b'0', b'0', 'Nike Pro', 'Nike官网', '2024年5月', 'M', '运动裤,健身', '洗涤干净，弹性很好');
INSERT INTO `products` VALUES (28, 3, 23, '跳绳 专业竞速跳绳', '专业竞速跳绳，钢丝绳芯，轴承设计，转速快不打结。可调节长度，适合不同身高。配重手柄，持握舒适。健身减脂必备。', 35.00, 89.00, '[\"https://picsum.photos/id/40/800/600\"]', 18, 1, '2025-10-24 14:25:00', '2025-10-26 07:40:00', NULL, '黑红色', '九成新', b'0', b'0', b'1', NULL, '京东', '2024年8月', '可调节', '跳绳,减脂', '赠送备用绳芯');
INSERT INTO `products` VALUES (29, 4, 25, '户外登山包 60L大容量', '专业户外登山包，60L大容量，防水耐磨材质。背负系统舒适，分仓设计合理。适合徒步、露营、旅行。用过1-2次，几乎全新。', 199.00, 499.00, '[\"https://picsum.photos/id/41/800/600\"]', 26, 1, '2025-10-22 10:15:00', '2025-10-26 16:25:00', NULL, '军绿色', '九五成新', b'0', b'0', b'1', NULL, '迪卡侬', '2024年6月', '60L', '登山包,户外', '配防雨罩，功能完好');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `student_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学号',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码(加密)',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `status` int NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `ban_expire_time` datetime(6) NULL DEFAULT NULL,
  `ban_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `banned` bit(1) NULL DEFAULT NULL,
  `last_active_time` datetime(6) NULL DEFAULT NULL,
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `online` bit(1) NULL DEFAULT NULL,
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_users_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_users_status`(`status` ASC) USING BTREE,
  INDEX `idx_users_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, '20210001', '张三', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM5lE5OmzrfzQhFvdHBG', '张三', '13800138001', 'zhangsan@example.com', NULL, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `users` VALUES (2, '20210002', '李四', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM5lE5OmzrfzQhFvdHBG', '李四', '13800138002', 'lisi@example.com', NULL, 0, '2025-09-25 01:35:11', '2025-11-21 19:37:33', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `users` VALUES (3, '20210003', '王五', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM5lE5OmzrfzQhFvdHBG', '王五', '13800138003', 'wangwu@example.com', NULL, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `users` VALUES (4, '20210004', '赵六', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM5lE5OmzrfzQhFvdHBG', '赵六', '13800138004', 'zhaoliu@example.com', NULL, 1, '2025-09-25 01:35:11', '2025-09-25 01:35:11', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `users` VALUES (5, '20231614239', '钱七', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM5lE5OmzrfzQhFvdHBG', '钱七', '13800138005', 'qianqi@example.com', NULL, 1, '2025-09-25 01:35:11', '2025-10-26 21:16:45', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `users` VALUES (8, '20231614241', '李文昊', '$2a$10$8QAQyqKZ/z0lMCkNMSDmDe1Q.c0cA7eXCNHiyqLnAQxLZJSCCgKfm', '李文昊', '15035267995', '15035267995@163.com', '/api/v1/files/view/avatars/84648969929d4b00b9c14ab41b5f296d.jpg', 1, '2025-10-26 20:23:04', '2025-11-21 18:17:37', NULL, NULL, b'0', NULL, NULL, b'0', 'USER', NULL);
INSERT INTO `users` VALUES (9, '20231614244', '孙若栩', '$2a$10$NYcevQ9tpI6F/dvavft1euRfdirEv0bXbH6chqX6LOFlmrdAZ7zmi', '孙若栩', '19722725532', '19722725532@163.com', '/api/v1/files/view/avatars/36b21688e21643eeb396935782cc5eda.jpg', 1, '2025-10-26 21:13:51', '2025-10-26 22:18:29', NULL, NULL, b'0', NULL, NULL, b'0', 'USER', NULL);
INSERT INTO `users` VALUES (10, '20231614234', '睢志轩', '$2a$10$40HRvJJ2.Ade28ubO7hTcej7imdS7F6G1tlEqcOYDMFXaduffpF8u', '睢志轩', '19722726749', '19722726749@163.com', NULL, 1, '2025-10-26 21:15:09', '2025-10-26 21:15:09', NULL, NULL, b'0', NULL, NULL, b'0', 'USER', NULL);
INSERT INTO `users` VALUES (11, '20231614238', '李思恒', '$2a$10$55Q4iI04hgFXUbpdZybPwOFMkz62z/zAkmzxz/JMB8QPzcFLnckxO', '李思恒', '19581910980', '19581910980@163.com', NULL, 1, '2025-10-26 21:22:42', '2025-10-26 21:22:42', NULL, NULL, b'0', NULL, NULL, b'0', 'USER', NULL);
INSERT INTO `users` VALUES (12, '20231614632', '冯树斌', '$2a$10$dIpXwWto6/Aev9x5wjYFqO1Z8cy5QRkjoWOOFYr5jPSBMS3a8w75q', '冯树斌', '18234881380', '18234881380@163.com', NULL, 1, '2025-10-26 21:23:18', '2025-10-26 21:23:18', NULL, NULL, b'0', NULL, NULL, b'0', 'USER', NULL);

SET FOREIGN_KEY_CHECKS = 1;
