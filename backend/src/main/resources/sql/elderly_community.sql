/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80033 (8.0.33)
 Source Host           : localhost:3306
 Source Schema         : elderly_community

 Target Server Type    : MySQL
 Target Server Version : 80033 (8.0.33)
 File Encoding         : 65001

 Date: 21/01/2026 16:54:59
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for building_worker
-- ----------------------------
DROP TABLE IF EXISTS `building_worker`;
CREATE TABLE `building_worker`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `building` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '楼栋名称（对应family表的building字段）',
  `worker_id` bigint NOT NULL COMMENT '配送员ID（对应sys_user表）',
  `is_primary` tinyint NULL DEFAULT 0 COMMENT '是否主要负责人: 0-否 1-是',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-停用 1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_building_worker`(`building` ASC, `worker_id` ASC) USING BTREE,
  INDEX `idx_building`(`building` ASC) USING BTREE,
  INDEX `idx_worker_id`(`worker_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '楼栋配送员关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of building_worker
-- ----------------------------

-- ----------------------------
-- Table structure for cleaning_order
-- ----------------------------
DROP TABLE IF EXISTS `cleaning_order`;
CREATE TABLE `cleaning_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `elderly_id` bigint NOT NULL COMMENT '老人ID',
  `booker_id` bigint NULL DEFAULT NULL COMMENT '预约人ID',
  `worker_id` bigint NOT NULL COMMENT '保洁员ID',
  `service_id` bigint NOT NULL COMMENT '保洁服务ID',
  `service_date` date NOT NULL COMMENT '服务日期',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `duration` int NOT NULL COMMENT '服务时长(分钟)',
  `amount` int NOT NULL COMMENT '金额(积分)',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务地址',
  `service_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务码',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态: 0-待服务 1-服务中 2-待确认 3-已完成 4-已取消',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `cancel_deduction` int NULL DEFAULT 0 COMMENT '取消扣除积分',
  `refund_amount` int NULL DEFAULT 0 COMMENT '退还积分',
  `actual_start_time` datetime NULL DEFAULT NULL COMMENT '实际开始时间',
  `actual_end_time` datetime NULL DEFAULT NULL COMMENT '实际结束时间',
  `evidence` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务凭证',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_elderly_id`(`elderly_id` ASC) USING BTREE,
  INDEX `idx_worker_id`(`worker_id` ASC) USING BTREE,
  INDEX `idx_service_date`(`service_date` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '保洁预约订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cleaning_order
-- ----------------------------
INSERT INTO `cleaning_order` VALUES (1, '2012031887238332416', 12, 17, 23, 7, '2026-01-16', '11:00:00', '12:00:00', 60, 40, NULL, '787951', 0, NULL, 0, 0, NULL, NULL, NULL, NULL, '2026-01-16 13:19:18', '2026-01-16 14:50:47', 0);
INSERT INTO `cleaning_order` VALUES (2, '2012044430996467712', 12, 17, 23, 8, '2026-01-16', '11:00:00', '12:00:00', 60, 30, NULL, '961250', 0, NULL, 0, 0, NULL, NULL, NULL, NULL, '2026-01-16 14:09:09', '2026-01-16 14:50:47', 0);

-- ----------------------------
-- Table structure for cleaning_service
-- ----------------------------
DROP TABLE IF EXISTS `cleaning_service`;
CREATE TABLE `cleaning_service`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '服务名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务描述',
  `reference_duration` int NOT NULL COMMENT '参考耗时(分钟)',
  `price_per_30min` int NOT NULL COMMENT '每30分钟价格(积分)',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '保洁服务项目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cleaning_service
-- ----------------------------
INSERT INTO `cleaning_service` VALUES (1, '擦窗户', '室内窗户清洁，包括玻璃和窗框', 30, 20, NULL, 1, 1, '2026-01-11 20:15:25', 0);
INSERT INTO `cleaning_service` VALUES (2, '地面清洁', '地板/地砖拖洗清洁', 30, 15, NULL, 2, 1, '2026-01-11 20:15:25', 0);
INSERT INTO `cleaning_service` VALUES (3, '厨房清洁', '厨房台面、灶台、油烟机表面清洁', 60, 25, NULL, 3, 1, '2026-01-11 20:15:25', 0);
INSERT INTO `cleaning_service` VALUES (4, '卫生间清洁', '马桶、洗手台、浴室清洁', 45, 20, NULL, 4, 1, '2026-01-11 20:15:25', 0);
INSERT INTO `cleaning_service` VALUES (5, '卧室整理', '床铺整理、衣物归纳、除尘', 30, 15, NULL, 5, 1, '2026-01-11 20:15:25', 0);
INSERT INTO `cleaning_service` VALUES (6, '全屋保洁', '全屋综合清洁服务', 120, 50, NULL, 6, 1, '2026-01-11 20:15:25', 0);
INSERT INTO `cleaning_service` VALUES (7, '擦窗户', '室内窗户清洁，包括玻璃和窗框', 30, 20, 'window', 1, 1, '2026-01-13 22:08:03', 0);
INSERT INTO `cleaning_service` VALUES (8, '地面清洁', '地板/地砖拖洗清洁', 30, 15, 'floor', 2, 1, '2026-01-13 22:08:03', 0);
INSERT INTO `cleaning_service` VALUES (9, '厨房清洁', '厨房台面、灶台、油烟机表面清洁', 60, 25, 'kitchen', 3, 1, '2026-01-13 22:08:03', 0);
INSERT INTO `cleaning_service` VALUES (10, '卫生间清洁', '马桶、洗手台、浴室清洁', 45, 20, 'bathroom', 4, 1, '2026-01-13 22:08:03', 0);
INSERT INTO `cleaning_service` VALUES (11, '卧室整理', '床铺整理、衣物归纳、除尘', 30, 15, 'bedroom', 5, 1, '2026-01-13 22:08:03', 0);
INSERT INTO `cleaning_service` VALUES (12, '全屋保洁', '全屋综合清洁服务', 120, 50, 'house', 6, 1, '2026-01-13 22:08:03', 0);

-- ----------------------------
-- Table structure for cleaning_worker_schedule
-- ----------------------------
DROP TABLE IF EXISTS `cleaning_worker_schedule`;
CREATE TABLE `cleaning_worker_schedule`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `worker_id` bigint NOT NULL COMMENT '保洁员ID',
  `schedule_date` date NOT NULL COMMENT '排班日期',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-休息 1-可预约',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_worker_date`(`worker_id` ASC, `schedule_date` ASC) USING BTREE,
  INDEX `idx_schedule_date`(`schedule_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 42 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '保洁员排班表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cleaning_worker_schedule
-- ----------------------------
INSERT INTO `cleaning_worker_schedule` VALUES (16, 20, '2026-01-16', '08:00:00', '18:00:00', 1, '2026-01-16 13:15:33');
INSERT INTO `cleaning_worker_schedule` VALUES (17, 21, '2026-01-16', '08:00:00', '18:00:00', 1, '2026-01-16 13:15:33');
INSERT INTO `cleaning_worker_schedule` VALUES (18, 22, '2026-01-16', '08:00:00', '18:00:00', 1, '2026-01-16 13:15:33');
INSERT INTO `cleaning_worker_schedule` VALUES (19, 20, '2026-01-17', '08:00:00', '18:00:00', 1, '2026-01-16 13:15:33');
INSERT INTO `cleaning_worker_schedule` VALUES (20, 21, '2026-01-17', '08:00:00', '18:00:00', 1, '2026-01-16 13:15:33');
INSERT INTO `cleaning_worker_schedule` VALUES (21, 22, '2026-01-17', '08:00:00', '18:00:00', 1, '2026-01-16 13:15:33');
INSERT INTO `cleaning_worker_schedule` VALUES (22, 20, '2026-01-18', '09:00:00', '17:00:00', 1, '2026-01-16 13:15:33');
INSERT INTO `cleaning_worker_schedule` VALUES (23, 21, '2026-01-18', '09:00:00', '17:00:00', 1, '2026-01-16 13:15:33');
INSERT INTO `cleaning_worker_schedule` VALUES (24, 22, '2026-01-18', '09:00:00', '17:00:00', 1, '2026-01-16 13:15:33');
INSERT INTO `cleaning_worker_schedule` VALUES (25, 20, '2026-01-19', '08:00:00', '18:00:00', 1, '2026-01-16 13:15:33');
INSERT INTO `cleaning_worker_schedule` VALUES (26, 21, '2026-01-19', '08:00:00', '18:00:00', 1, '2026-01-16 13:15:33');
INSERT INTO `cleaning_worker_schedule` VALUES (27, 22, '2026-01-19', '08:00:00', '18:00:00', 1, '2026-01-16 13:15:33');
INSERT INTO `cleaning_worker_schedule` VALUES (28, 20, '2026-01-20', '08:30:00', '17:30:00', 1, '2026-01-16 13:15:33');
INSERT INTO `cleaning_worker_schedule` VALUES (29, 21, '2026-01-20', '08:30:00', '17:30:00', 1, '2026-01-16 13:15:33');
INSERT INTO `cleaning_worker_schedule` VALUES (30, 22, '2026-01-20', '08:30:00', '17:30:00', 1, '2026-01-16 13:15:33');
INSERT INTO `cleaning_worker_schedule` VALUES (31, 20, '2026-01-21', '08:00:00', '18:00:00', 1, '2026-01-16 13:15:33');
INSERT INTO `cleaning_worker_schedule` VALUES (32, 21, '2026-01-21', '08:00:00', '18:00:00', 1, '2026-01-16 13:15:33');
INSERT INTO `cleaning_worker_schedule` VALUES (33, 22, '2026-01-21', '08:00:00', '18:00:00', 1, '2026-01-16 13:15:33');
INSERT INTO `cleaning_worker_schedule` VALUES (34, 20, '2026-01-22', '09:00:00', '17:00:00', 1, '2026-01-16 13:15:33');
INSERT INTO `cleaning_worker_schedule` VALUES (35, 21, '2026-01-22', '09:00:00', '17:00:00', 1, '2026-01-16 13:15:33');
INSERT INTO `cleaning_worker_schedule` VALUES (36, 22, '2026-01-22', '09:00:00', '17:00:00', 1, '2026-01-16 13:15:33');
INSERT INTO `cleaning_worker_schedule` VALUES (37, 23, '2026-01-16', '08:00:00', '18:00:00', 1, '2026-01-16 14:50:47');
INSERT INTO `cleaning_worker_schedule` VALUES (38, 23, '2026-01-17', '08:00:00', '18:00:00', 1, '2026-01-16 14:50:47');
INSERT INTO `cleaning_worker_schedule` VALUES (39, 23, '2026-01-18', '08:00:00', '18:00:00', 1, '2026-01-16 14:50:47');
INSERT INTO `cleaning_worker_schedule` VALUES (40, 23, '2026-01-19', '08:00:00', '18:00:00', 1, '2026-01-16 14:50:47');
INSERT INTO `cleaning_worker_schedule` VALUES (41, 23, '2026-01-20', '08:00:00', '18:00:00', 1, '2026-01-16 14:50:47');

-- ----------------------------
-- Table structure for cleaning_worker_service
-- ----------------------------
DROP TABLE IF EXISTS `cleaning_worker_service`;
CREATE TABLE `cleaning_worker_service`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `worker_id` bigint NOT NULL COMMENT '保洁员ID',
  `service_id` bigint NOT NULL COMMENT '保洁服务ID',
  `custom_duration` int NULL DEFAULT NULL COMMENT '自定义参考耗时(分钟)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_worker_service`(`worker_id` ASC, `service_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '保洁员服务项目关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cleaning_worker_service
-- ----------------------------

-- ----------------------------
-- Table structure for emergency_call
-- ----------------------------
DROP TABLE IF EXISTS `emergency_call`;
CREATE TABLE `emergency_call`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `elderly_id` bigint NOT NULL COMMENT '老人ID',
  `elderly_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '老人姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `event_type` tinyint NULL DEFAULT NULL COMMENT '事件类型: 1-紧急医疗 2-摔倒 3-一般求助',
  `trigger_time` datetime NULL DEFAULT NULL COMMENT '触发时间',
  `responder_id` bigint NULL DEFAULT NULL COMMENT '响应人ID',
  `response_time` datetime NULL DEFAULT NULL COMMENT '响应时间',
  `escalated` tinyint NULL DEFAULT 0 COMMENT '是否升级: 0-否 1-是',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态: 0-待处理 1-已处理',
  `result` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理结果',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_elderly_id`(`elderly_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应急呼叫表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of emergency_call
-- ----------------------------
INSERT INTO `emergency_call` VALUES (1, 1, '张大爷', '13800138001', '幸福社区1栋101室', 1, '2026-01-10 17:03:40', NULL, NULL, 0, 0, NULL, '2026-01-10 17:03:40');
INSERT INTO `emergency_call` VALUES (2, 1, '张大爷', '13800138001', '幸福社区1栋101室', 3, '2026-01-10 17:03:53', NULL, NULL, 0, 0, NULL, '2026-01-10 17:03:53');
INSERT INTO `emergency_call` VALUES (3, 1, '张大爷', '13800138001', '幸福社区1栋101室', 1, '2026-01-10 17:39:19', NULL, NULL, 0, 0, NULL, '2026-01-10 17:39:19');
INSERT INTO `emergency_call` VALUES (4, 1, '张大爷', '13800138001', '幸福社区1栋101室', 2, '2026-01-10 17:50:40', 17, '2026-01-16 10:26:38', 0, 1, NULL, '2026-01-10 17:50:40');

-- ----------------------------
-- Table structure for family
-- ----------------------------
DROP TABLE IF EXISTS `family`;
CREATE TABLE `family`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '完整地址',
  `community_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '社区名称',
  `building` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '楼栋',
  `unit` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单元',
  `room` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '房号',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '家庭表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of family
-- ----------------------------
INSERT INTO `family` VALUES (1, '北京市朝阳区幸福小区1号楼101', '幸福小区', '1号楼', '1单元', '101', '2026-01-14 22:31:02', 0);
INSERT INTO `family` VALUES (2, '北京市朝阳区幸福小区1号楼102', '幸福小区', '1号楼', '1单元', '102', '2026-01-14 22:31:02', 0);
INSERT INTO `family` VALUES (3, '北京市朝阳区幸福小区2号楼201', '幸福小区', '2号楼', '2单元', '201', '2026-01-14 22:31:02', 0);

-- ----------------------------
-- Table structure for health_record
-- ----------------------------
DROP TABLE IF EXISTS `health_record`;
CREATE TABLE `health_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `elderly_id` bigint NOT NULL COMMENT '老人ID',
  `order_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID',
  `symptoms` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '症状描述',
  `allergies` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '过敏史',
  `medications` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '常服药品',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_elderly_id`(`elderly_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '健康档案表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of health_record
-- ----------------------------
INSERT INTO `health_record` VALUES (13, 12, NULL, '头晕、血压偏高', '青霉素过敏', '降压药（每日一次）、阿司匹林', '需定期监测血压', '2025-12-15 22:35:01');
INSERT INTO `health_record` VALUES (14, 12, NULL, '膝关节疼痛', '青霉素过敏', '降压药、止痛贴', '建议减少爬楼', '2025-12-30 22:35:01');
INSERT INTO `health_record` VALUES (15, 12, NULL, '感冒咳嗽', '青霉素过敏', '降压药、感冒灵', '多喝水多休息', '2026-01-09 22:35:01');
INSERT INTO `health_record` VALUES (16, 13, NULL, '失眠、心悸', '无', '安神补脑液', '建议睡前少喝茶', '2025-12-25 22:35:01');
INSERT INTO `health_record` VALUES (17, 13, NULL, '腰酸背痛', '无', '安神补脑液、膏药', '适当活动', '2026-01-07 22:35:01');
INSERT INTO `health_record` VALUES (18, 14, NULL, '糖尿病复查', '磺胺类药物', '二甲双胍、胰岛素', '控制饮食，定期测血糖', '2026-01-04 22:35:01');

-- ----------------------------
-- Table structure for meal_daily_dish
-- ----------------------------
DROP TABLE IF EXISTS `meal_daily_dish`;
CREATE TABLE `meal_daily_dish`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `weekly_menu_id` bigint NOT NULL COMMENT '周食谱ID',
  `dish_date` date NOT NULL COMMENT '日期',
  `meal_type` tinyint NOT NULL COMMENT '餐次: 1-早餐 2-午餐 3-晚餐',
  `dish_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜品名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜品描述',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜品图片',
  `price` int NOT NULL COMMENT '价格(积分)',
  `nutrition_info` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '营养信息',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_weekly_menu`(`weekly_menu_id` ASC) USING BTREE,
  INDEX `idx_dish_date`(`dish_date` ASC) USING BTREE,
  INDEX `idx_meal_type`(`meal_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '每日菜品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of meal_daily_dish
-- ----------------------------
INSERT INTO `meal_daily_dish` VALUES (1, 1, '2026-01-19', 1, '包子', '11', NULL, 10, '22', 0, 1, '2026-01-16 09:34:20', 0);
INSERT INTO `meal_daily_dish` VALUES (2, 1, '2026-01-16', 1, '皮蛋瘦肉粥', '软糯可口，易消化', NULL, 8, '热量: 150kcal, 蛋白质: 8g', 1, 1, '2026-01-16 14:49:07', 0);
INSERT INTO `meal_daily_dish` VALUES (3, 1, '2026-01-16', 1, '豆浆油条套餐', '经典早餐搭配', NULL, 6, '热量: 200kcal, 蛋白质: 6g', 2, 1, '2026-01-16 14:49:07', 0);
INSERT INTO `meal_daily_dish` VALUES (4, 1, '2026-01-16', 2, '红烧肉套餐', '肥而不腻，入口即化', NULL, 15, '热量: 350kcal, 蛋白质: 18g', 1, 1, '2026-01-16 14:49:07', 0);
INSERT INTO `meal_daily_dish` VALUES (5, 1, '2026-01-16', 2, '清蒸鱼套餐', '新鲜鲈鱼，清淡营养', NULL, 18, '热量: 200kcal, 蛋白质: 25g', 2, 1, '2026-01-16 14:49:07', 0);
INSERT INTO `meal_daily_dish` VALUES (6, 1, '2026-01-16', 3, '番茄蛋汤面', '酸甜可口，开胃', NULL, 10, '热量: 180kcal, 蛋白质: 10g', 1, 1, '2026-01-16 14:49:07', 0);
INSERT INTO `meal_daily_dish` VALUES (7, 1, '2026-01-17', 1, '小米粥配咸菜', '养胃佳品', NULL, 5, '热量: 100kcal, 蛋白质: 3g', 1, 1, '2026-01-16 14:49:07', 0);
INSERT INTO `meal_daily_dish` VALUES (8, 1, '2026-01-17', 2, '糖醋排骨套餐', '酸甜适中，肉质鲜嫩', NULL, 16, '热量: 380kcal, 蛋白质: 20g', 1, 1, '2026-01-16 14:49:07', 0);
INSERT INTO `meal_daily_dish` VALUES (9, 1, '2026-01-17', 2, '宫保鸡丁套餐', '微辣开胃', NULL, 14, '热量: 300kcal, 蛋白质: 22g', 2, 1, '2026-01-16 14:49:07', 0);
INSERT INTO `meal_daily_dish` VALUES (10, 1, '2026-01-17', 3, '蔬菜粥', '清淡养生', NULL, 8, '热量: 120kcal, 蛋白质: 4g', 1, 1, '2026-01-16 14:49:07', 0);
INSERT INTO `meal_daily_dish` VALUES (11, 1, '2026-01-18', 2, '鱼香肉丝套餐', '下饭神器', NULL, 13, '热量: 280kcal, 蛋白质: 15g', 1, 1, '2026-01-16 14:49:07', 0);
INSERT INTO `meal_daily_dish` VALUES (12, 1, '2026-01-16', 1, '皮蛋瘦肉粥', '软糯可口，易消化', NULL, 8, '热量: 150kcal, 蛋白质: 8g', 1, 1, '2026-01-16 14:50:47', 0);
INSERT INTO `meal_daily_dish` VALUES (13, 1, '2026-01-16', 1, '豆浆油条套餐', '经典早餐搭配', NULL, 6, '热量: 200kcal, 蛋白质: 6g', 2, 1, '2026-01-16 14:50:47', 0);
INSERT INTO `meal_daily_dish` VALUES (14, 1, '2026-01-16', 2, '红烧肉套餐', '肥而不腻，入口即化', NULL, 15, '热量: 350kcal, 蛋白质: 18g', 1, 1, '2026-01-16 14:50:47', 0);
INSERT INTO `meal_daily_dish` VALUES (15, 1, '2026-01-16', 2, '清蒸鱼套餐', '新鲜鲈鱼，清淡营养', NULL, 18, '热量: 200kcal, 蛋白质: 25g', 2, 1, '2026-01-16 14:50:47', 0);
INSERT INTO `meal_daily_dish` VALUES (16, 1, '2026-01-16', 3, '番茄蛋汤面', '酸甜可口，开胃', NULL, 10, '热量: 180kcal, 蛋白质: 10g', 1, 1, '2026-01-16 14:50:47', 0);
INSERT INTO `meal_daily_dish` VALUES (17, 1, '2026-01-17', 1, '小米粥配咸菜', '养胃佳品', NULL, 5, '热量: 100kcal, 蛋白质: 3g', 1, 1, '2026-01-16 14:50:47', 0);
INSERT INTO `meal_daily_dish` VALUES (18, 1, '2026-01-17', 2, '糖醋排骨套餐', '酸甜适中，肉质鲜嫩', NULL, 16, '热量: 380kcal, 蛋白质: 20g', 1, 1, '2026-01-16 14:50:47', 0);
INSERT INTO `meal_daily_dish` VALUES (19, 1, '2026-01-17', 2, '宫保鸡丁套餐', '微辣开胃', NULL, 14, '热量: 300kcal, 蛋白质: 22g', 2, 1, '2026-01-16 14:50:47', 0);
INSERT INTO `meal_daily_dish` VALUES (20, 1, '2026-01-17', 3, '蔬菜粥', '清淡养生', NULL, 8, '热量: 120kcal, 蛋白质: 4g', 1, 1, '2026-01-16 14:50:47', 0);
INSERT INTO `meal_daily_dish` VALUES (21, 1, '2026-01-18', 2, '鱼香肉丝套餐', '下饭神器', NULL, 13, '热量: 280kcal, 蛋白质: 15g', 1, 1, '2026-01-16 14:50:47', 0);

-- ----------------------------
-- Table structure for meal_delivery_config
-- ----------------------------
DROP TABLE IF EXISTS `meal_delivery_config`;
CREATE TABLE `meal_delivery_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `meal_type` tinyint NOT NULL COMMENT '餐次: 1-早餐 2-午餐 3-晚餐',
  `delivery_start_time` time NOT NULL COMMENT '送餐开始时间',
  `delivery_end_time` time NOT NULL COMMENT '送餐结束时间',
  `booking_deadline_minutes` int NULL DEFAULT 60 COMMENT '预约截止时间(送餐前X分钟)',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_meal_type`(`meal_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '餐饮配送时间配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of meal_delivery_config
-- ----------------------------
INSERT INTO `meal_delivery_config` VALUES (1, 1, '07:00:00', '08:30:00', 60, 1, '2026-01-11 20:15:25');
INSERT INTO `meal_delivery_config` VALUES (2, 2, '11:00:00', '12:30:00', 60, 1, '2026-01-11 20:15:25');
INSERT INTO `meal_delivery_config` VALUES (3, 3, '17:00:00', '18:30:00', 60, 1, '2026-01-11 20:15:25');

-- ----------------------------
-- Table structure for meal_order
-- ----------------------------
DROP TABLE IF EXISTS `meal_order`;
CREATE TABLE `meal_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `elderly_id` bigint NOT NULL COMMENT '老人ID',
  `booker_id` bigint NULL DEFAULT NULL COMMENT '预约人ID',
  `dish_id` bigint NOT NULL COMMENT '菜品ID',
  `dish_date` date NOT NULL COMMENT '用餐日期',
  `meal_type` tinyint NOT NULL COMMENT '餐次: 1-早餐 2-午餐 3-晚餐',
  `quantity` int NULL DEFAULT 1 COMMENT '数量',
  `amount` int NOT NULL COMMENT '金额(积分)',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '送餐地址',
  `worker_id` bigint NULL DEFAULT NULL COMMENT '配送员ID',
  `service_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务码',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态: 0-待配送 1-配送中 2-已送达 3-已取消',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `cancel_deduction` int NULL DEFAULT 0 COMMENT '取消扣除积分',
  `refund_amount` int NULL DEFAULT 0 COMMENT '退还积分',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_elderly_id`(`elderly_id` ASC) USING BTREE,
  INDEX `idx_dish_date`(`dish_date` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '餐饮预约订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of meal_order
-- ----------------------------
INSERT INTO `meal_order` VALUES (1, 'M20260116144907001', 1, 1, 1, '2026-01-16', 1, 1, 8, '阳光小区1栋101室', 23, '621169', 0, NULL, 0, 0, NULL, '2026-01-16 14:49:07', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (2, 'M20260116144907002', 2, 2, 3, '2026-01-16', 2, 1, 15, '阳光小区2栋202室', 23, '496684', 0, NULL, 0, 0, NULL, '2026-01-16 14:49:07', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (3, 'M20260116144907003', 3, 3, 4, '2026-01-16', 2, 2, 36, '幸福花园3栋303室', 23, '619912', 1, NULL, 0, 0, NULL, '2026-01-16 14:49:07', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (4, 'M20260116144907004', 1, 1, 5, '2026-01-16', 3, 1, 10, '阳光小区1栋101室', 23, '609510', 0, NULL, 0, 0, NULL, '2026-01-16 14:49:07', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (5, 'M20260116144907005', 4, 4, 6, '2026-01-17', 1, 1, 5, '和谐家园4栋404室', 23, '187815', 0, NULL, 0, 0, NULL, '2026-01-16 14:49:07', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (6, 'M20260116144907006', 5, 5, 7, '2026-01-17', 2, 1, 16, '温馨苑5栋505室', 23, '110547', 0, NULL, 0, 0, NULL, '2026-01-16 14:49:07', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (7, 'M20260116144907007', 2, 2, 8, '2026-01-17', 2, 1, 14, '阳光小区2栋202室', 23, '989290', 1, NULL, 0, 0, NULL, '2026-01-16 14:49:07', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (8, 'M20260116144907008', 3, 3, 9, '2026-01-17', 3, 1, 8, '幸福花园3栋303室', 23, '614809', 2, NULL, 0, 0, NULL, '2026-01-16 14:49:07', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (9, 'M20260116144907009', 1, 1, 10, '2026-01-18', 2, 1, 13, '阳光小区1栋101室', 23, '106177', 0, NULL, 0, 0, NULL, '2026-01-16 14:49:07', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (10, 'M20260116144907010', 4, 4, 3, '2026-01-16', 2, 2, 30, '和谐家园4栋404室', 23, '686457', 0, NULL, 0, 0, NULL, '2026-01-16 14:49:07', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (11, 'M20260116145047001', 1, 1, 1, '2026-01-16', 1, 1, 8, '阳光小区1栋101室', 23, '113753', 0, NULL, 0, 0, NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (12, 'M20260116145047002', 2, 2, 3, '2026-01-16', 2, 1, 15, '阳光小区2栋202室', 23, '509397', 0, NULL, 0, 0, NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (13, 'M20260116145047003', 3, 3, 4, '2026-01-16', 2, 2, 36, '幸福花园3栋303室', 23, '205726', 1, NULL, 0, 0, NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (14, 'M20260116145047004', 1, 1, 5, '2026-01-16', 3, 1, 10, '阳光小区1栋101室', 23, '500438', 0, NULL, 0, 0, NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (15, 'M20260116145047005', 4, 4, 6, '2026-01-17', 1, 1, 5, '和谐家园4栋404室', 23, '885014', 0, NULL, 0, 0, NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (16, 'M20260116145047006', 5, 5, 7, '2026-01-17', 2, 1, 16, '温馨苑5栋505室', 23, '923754', 0, NULL, 0, 0, NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (17, 'M20260116145047007', 2, 2, 8, '2026-01-17', 2, 1, 14, '阳光小区2栋202室', 23, '963732', 1, NULL, 0, 0, NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (18, 'M20260116145047008', 3, 3, 9, '2026-01-17', 3, 1, 8, '幸福花园3栋303室', 23, '047396', 2, NULL, 0, 0, NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (19, 'M20260116145047009', 1, 1, 10, '2026-01-18', 2, 1, 13, '阳光小区1栋101室', 23, '345787', 0, NULL, 0, 0, NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (20, 'M20260116145047010', 4, 4, 3, '2026-01-16', 2, 2, 30, '和谐家园4栋404室', 23, '586747', 0, NULL, 0, 0, NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (21, '2012057580626001920', 12, 12, 6, '2026-01-16', 3, 1, 10, NULL, 23, '896374', 3, '2026-01-16 15:32:41', 0, 10, NULL, '2026-01-16 15:01:24', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (22, '2012063056122171392', 12, 12, 16, '2026-01-16', 3, 1, 10, NULL, 23, NULL, 3, '2026-01-16 15:32:30', 0, 10, NULL, '2026-01-16 15:23:10', '2026-01-16 15:23:10', 0);
INSERT INTO `meal_order` VALUES (23, '2012065596347838464', 12, 12, 16, '2026-01-16', 3, 1, 10, NULL, 23, '266217', 2, NULL, 0, 0, NULL, '2026-01-16 15:33:15', '2026-01-16 15:37:23', 0);

-- ----------------------------
-- Table structure for meal_weekly_menu
-- ----------------------------
DROP TABLE IF EXISTS `meal_weekly_menu`;
CREATE TABLE `meal_weekly_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `week_start_date` date NOT NULL COMMENT '周开始日期(周一)',
  `week_end_date` date NOT NULL COMMENT '周结束日期(周日)',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态: 0-草稿 1-已发布',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_week`(`week_start_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '周食谱表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of meal_weekly_menu
-- ----------------------------
INSERT INTO `meal_weekly_menu` VALUES (1, '2026-01-19', '2026-01-25', 1, '2026-01-16 09:34:28', '2026-01-16 09:34:02', '2026-01-16 09:34:28', 0);

-- ----------------------------
-- Table structure for medical_appointment
-- ----------------------------
DROP TABLE IF EXISTS `medical_appointment`;
CREATE TABLE `medical_appointment`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `elderly_id` bigint NOT NULL COMMENT '老人ID',
  `booker_id` bigint NULL DEFAULT NULL COMMENT '预约人ID',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `appointment_date` date NOT NULL COMMENT '预约日期',
  `appointment_type` tinyint NULL DEFAULT 1 COMMENT '预约类型: 1-日间巡诊 2-夜间急诊',
  `queue_number` int NULL DEFAULT NULL COMMENT '排队号',
  `symptoms` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '症状描述',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '巡诊地址',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态: 0-排队中 1-巡诊中 2-已完成 3-已取消',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `visit_time` datetime NULL DEFAULT NULL COMMENT '实际巡诊时间',
  `diagnosis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '诊断结果',
  `prescription` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '处方建议',
  `service_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务码',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_elderly_id`(`elderly_id` ASC) USING BTREE,
  INDEX `idx_doctor_id`(`doctor_id` ASC) USING BTREE,
  INDEX `idx_appointment_date`(`appointment_date` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医疗巡诊预约表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of medical_appointment
-- ----------------------------
INSERT INTO `medical_appointment` VALUES (1, 'MED1768401301001', 12, NULL, 1, '2026-01-04', 1, 1, '头晕、血压偏高', '北京市朝阳区幸福小区1号楼101', 2, NULL, NULL, '高血压，需要长期服药控制', '降压药（每日一次）、阿司匹林', '721632', NULL, '2026-01-04 22:35:01', '2026-01-16 15:16:05', 0);
INSERT INTO `medical_appointment` VALUES (2, 'MED1768401301002', 12, NULL, 2, '2026-01-11', 1, 2, '感冒咳嗽、流鼻涕', '北京市朝阳区幸福小区1号楼101', 2, NULL, NULL, '普通感冒', '感冒灵、止咳糖浆', '919035', NULL, '2026-01-11 22:35:01', '2026-01-16 15:16:05', 0);
INSERT INTO `medical_appointment` VALUES (3, 'MED1768401301003', 12, NULL, 1, '2026-01-16', 1, 3, '复查血压', '北京市朝阳区幸福小区1号楼101', 0, NULL, NULL, NULL, NULL, '430280', NULL, '2026-01-14 22:35:01', '2026-01-16 15:16:05', 0);
INSERT INTO `medical_appointment` VALUES (4, 'MD20260116145047001', 1, 1, 4, '2026-01-16', 1, 1, '头晕、乏力', '阳光小区1栋101室', 0, NULL, NULL, NULL, NULL, '394298', NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `medical_appointment` VALUES (5, 'MD20260116145047002', 2, 2, 4, '2026-01-16', 1, 2, '血压偏高', '阳光小区2栋202室', 0, NULL, NULL, NULL, NULL, '680648', NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `medical_appointment` VALUES (6, 'MD20260116145047003', 3, 3, 4, '2026-01-16', 1, 3, '胸闷气短', '幸福花园3栋303室', 1, NULL, NULL, NULL, NULL, '220347', NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `medical_appointment` VALUES (7, 'MD20260116145047004', 4, 4, 4, '2026-01-16', 1, 4, '腰酸背痛', '和谐家园4栋404室', 0, NULL, NULL, NULL, NULL, '059792', NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `medical_appointment` VALUES (8, 'MD20260116145047005', 5, 5, 4, '2026-01-17', 1, 1, '感冒发烧', '温馨苑5栋505室', 0, NULL, NULL, NULL, NULL, '637918', NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `medical_appointment` VALUES (9, 'MD20260116145047006', 1, 1, 4, '2026-01-17', 1, 2, '夜间心悸', '阳光小区1栋101室', 0, NULL, NULL, NULL, NULL, '010217', NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `medical_appointment` VALUES (10, 'MD20260116145047007', 2, 2, 4, '2026-01-18', 1, 1, '关节疼痛', '阳光小区2栋202室', 0, NULL, NULL, NULL, NULL, '137329', NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `medical_appointment` VALUES (11, 'MD20260116145047008', 3, 3, 4, '2026-01-18', 1, 2, '消化不良', '幸福花园3栋303室', 0, NULL, NULL, NULL, NULL, '655996', NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `medical_appointment` VALUES (12, 'MD20260116145047009', 4, 4, 4, '2026-01-16', 1, 5, '失眠多梦', '和谐家园4栋404室', 2, NULL, NULL, NULL, NULL, '867993', NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `medical_appointment` VALUES (13, 'MD20260116145047010', 5, 5, 4, '2026-01-16', 1, 6, '皮肤瘙痒', '温馨苑5栋505室', 0, NULL, NULL, NULL, NULL, '371980', NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);

-- ----------------------------
-- Table structure for medical_circuit_breaker
-- ----------------------------
DROP TABLE IF EXISTS `medical_circuit_breaker`;
CREATE TABLE `medical_circuit_breaker`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `current_queue_count` int NULL DEFAULT 0 COMMENT '当前排队人数',
  `is_open` tinyint NULL DEFAULT 0 COMMENT '熔断是否开启: 0-关闭(可接单) 1-开启(停止接单)',
  `last_update_time` datetime NULL DEFAULT NULL COMMENT '最后更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_doctor_id`(`doctor_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生熔断状态表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of medical_circuit_breaker
-- ----------------------------

-- ----------------------------
-- Table structure for medical_doctor
-- ----------------------------
DROP TABLE IF EXISTS `medical_doctor`;
CREATE TABLE `medical_doctor`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '医生姓名',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职称',
  `specialty` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '专长',
  `introduction` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '简介',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `max_queue_limit` int NULL DEFAULT 10 COMMENT '最大排队人数限制',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-停诊 1-正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of medical_doctor
-- ----------------------------
INSERT INTO `medical_doctor` VALUES (4, 23, '李医生', '主治医师', '全科', '从医20年，擅长老年常见病诊治', NULL, 20, 1, '2026-01-16 14:50:47', 0);

-- ----------------------------
-- Table structure for medical_doctor_duty
-- ----------------------------
DROP TABLE IF EXISTS `medical_doctor_duty`;
CREATE TABLE `medical_doctor_duty`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `duty_date` date NOT NULL COMMENT '值班日期',
  `duty_type` tinyint NOT NULL COMMENT '值班类型: 1-日间(8:00-20:00) 2-夜间急诊',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `is_night_shift` tinyint NULL DEFAULT 0 COMMENT '是否夜间值班: 0-否 1-是',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-休息 1-值班中',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_doctor_date`(`doctor_id` ASC, `duty_date` ASC) USING BTREE,
  INDEX `idx_duty_date`(`duty_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生值班表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of medical_doctor_duty
-- ----------------------------
INSERT INTO `medical_doctor_duty` VALUES (1, 4, '2026-01-16', 2, '20:00:00', '08:00:00', 0, 1, '2026-01-16 14:50:47');
INSERT INTO `medical_doctor_duty` VALUES (2, 4, '2026-01-17', 1, '08:00:00', '20:00:00', 0, 1, '2026-01-16 14:50:47');
INSERT INTO `medical_doctor_duty` VALUES (3, 4, '2026-01-18', 1, '08:00:00', '20:00:00', 0, 1, '2026-01-16 14:50:47');
INSERT INTO `medical_doctor_duty` VALUES (4, 4, '2026-01-19', 1, '08:00:00', '20:00:00', 0, 1, '2026-01-16 14:50:47');
INSERT INTO `medical_doctor_duty` VALUES (5, 4, '2026-01-20', 1, '08:00:00', '20:00:00', 0, 1, '2026-01-16 14:50:47');

-- ----------------------------
-- Table structure for points_transaction
-- ----------------------------
DROP TABLE IF EXISTS `points_transaction`;
CREATE TABLE `points_transaction`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` tinyint NOT NULL COMMENT '类型: 1-充值 2-消费 3-退款 4-扣除',
  `amount` int NOT NULL COMMENT '积分数量',
  `balance` int NOT NULL COMMENT '变动后余额',
  `order_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联订单类型: meal/cleaning/medical',
  `order_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '积分流水表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of points_transaction
-- ----------------------------
INSERT INTO `points_transaction` VALUES (1, 12, 1, 100, 100, NULL, NULL, '管理员充值', '2026-01-16 09:53:09');
INSERT INTO `points_transaction` VALUES (2, 12, 2, 40, 60, 'cleaning', NULL, '保洁预约-擦窗户', '2026-01-16 13:19:18');
INSERT INTO `points_transaction` VALUES (3, 12, 2, 30, 30, 'cleaning', NULL, '保洁预约-地面清洁', '2026-01-16 14:09:09');
INSERT INTO `points_transaction` VALUES (4, 12, 2, 10, 20, 'meal', NULL, '餐饮预约-番茄蛋汤面', '2026-01-16 15:01:24');
INSERT INTO `points_transaction` VALUES (5, 12, 2, 10, 10, 'meal', NULL, '餐饮预约-番茄蛋汤面', '2026-01-16 15:23:09');
INSERT INTO `points_transaction` VALUES (6, 12, 3, 10, 20, 'meal', 22, '餐饮订单取消退款', '2026-01-16 15:32:30');
INSERT INTO `points_transaction` VALUES (7, 12, 3, 10, 30, 'meal', 21, '餐饮订单取消退款', '2026-01-16 15:32:41');
INSERT INTO `points_transaction` VALUES (8, 12, 2, 10, 20, 'meal', NULL, '餐饮预约-番茄蛋汤面', '2026-01-16 15:33:15');

-- ----------------------------
-- Table structure for service_category
-- ----------------------------
DROP TABLE IF EXISTS `service_category`;
CREATE TABLE `service_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `is_medical` tinyint NULL DEFAULT 0 COMMENT '是否医疗类: 0-否 1-是',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of service_category
-- ----------------------------
INSERT INTO `service_category` VALUES (1, '生活保洁', 'clean', 0, 1, 1, '2026-01-05 20:16:00', 0);
INSERT INTO `service_category` VALUES (2, '餐饮配送', 'food', 0, 2, 1, '2026-01-05 20:16:00', 0);
INSERT INTO `service_category` VALUES (3, '社区医疗', 'medical', 1, 3, 1, '2026-01-05 20:16:00', 0);
INSERT INTO `service_category` VALUES (4, '家政服务', 'home', 0, 4, 1, '2026-01-05 20:16:00', 0);

-- ----------------------------
-- Table structure for service_item
-- ----------------------------
DROP TABLE IF EXISTS `service_item`;
CREATE TABLE `service_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '服务名称',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '服务描述',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '价格',
  `duration` int NULL DEFAULT NULL COMMENT '服务时长(分钟)',
  `need_service_code` tinyint NULL DEFAULT 1 COMMENT '是否需要服务码: 0-否 1-是',
  `daily_capacity` int NULL DEFAULT 10 COMMENT '每日容量',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务项目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of service_item
-- ----------------------------
INSERT INTO `service_item` VALUES (1, '日常保洁', 1, '房间清扫、地面清洁、垃圾清理', 50.00, 120, 1, 10, 1, '2026-01-10 19:16:06', '2026-01-14 22:31:02', 0);
INSERT INTO `service_item` VALUES (2, '深度保洁', 1, '全屋深度清洁，包括厨房卫生间', 120.00, 180, 1, 5, 1, '2026-01-10 19:16:06', '2026-01-14 22:31:02', 0);
INSERT INTO `service_item` VALUES (3, '玻璃清洁', 2, '门窗玻璃清洁', 30.00, 30, 1, 50, 1, '2026-01-10 19:16:06', '2026-01-14 22:31:02', 0);
INSERT INTO `service_item` VALUES (4, '早餐配送', 3, '营养早餐配送到家', 15.00, 60, 1, 8, 1, '2026-01-10 19:16:06', '2026-01-14 22:31:02', 0);
INSERT INTO `service_item` VALUES (5, '午餐配送', 3, '营养午餐配送到家', 25.00, 60, 1, 8, 1, '2026-01-10 19:26:41', '2026-01-14 22:31:02', 0);
INSERT INTO `service_item` VALUES (6, '晚餐配送', 3, '营养晚餐配送到家', 25.00, 90, 1, 6, 1, '2026-01-10 19:26:41', '2026-01-14 22:31:02', 0);
INSERT INTO `service_item` VALUES (7, '日间巡诊', 4, '医生上门巡诊服务', 80.00, 60, 1, 10, 1, '2026-01-10 19:26:41', '2026-01-14 22:31:02', 0);
INSERT INTO `service_item` VALUES (8, '夜间急诊', 4, '夜间紧急医疗服务', 150.00, 60, 1, 8, 1, '2026-01-10 19:26:41', '2026-01-14 22:31:02', 0);
INSERT INTO `service_item` VALUES (9, '陪护服务', 4, '专业陪护人员上门服务', 100.00, 120, 1, 8, 1, '2026-01-14 22:31:02', '2026-01-14 22:31:02', 0);
INSERT INTO `service_item` VALUES (10, '代购服务', 4, '代购日常生活用品', 20.00, 60, 1, 20, 1, '2026-01-14 22:31:02', '2026-01-14 22:31:02', 0);

-- ----------------------------
-- Table structure for service_order
-- ----------------------------
DROP TABLE IF EXISTS `service_order`;
CREATE TABLE `service_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `elderly_id` bigint NOT NULL COMMENT '老人ID',
  `service_item_id` bigint NOT NULL COMMENT '服务项目ID',
  `worker_id` bigint NULL DEFAULT NULL COMMENT '服务人员ID',
  `booker_id` bigint NULL DEFAULT NULL COMMENT '预约人ID',
  `appointment_time` datetime NULL DEFAULT NULL COMMENT '预约时间',
  `appointment_date` date NULL DEFAULT NULL COMMENT '预约日期',
  `time_slot_id` bigint NULL DEFAULT NULL COMMENT '时间段ID',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务地址',
  `amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '金额',
  `service_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务码',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态: 0-待接单 1-已接单 2-服务中 3-待确认 4-已完成 5-已取消',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `evidence` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '服务凭证',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  `service_start_time` datetime NULL DEFAULT NULL COMMENT '服务开始时间',
  `service_end_time` datetime NULL DEFAULT NULL COMMENT '服务完成时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_elderly_id`(`elderly_id` ASC) USING BTREE,
  INDEX `idx_worker_id`(`worker_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_appointment_time`(`appointment_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of service_order
-- ----------------------------
INSERT INTO `service_order` VALUES (1, 'ORD20260114001', 12, 1, NULL, 12, '2026-01-15 00:23:58', '2026-01-14', NULL, '北京市朝阳区幸福小区1号楼101', 50.00, '585370', 0, '请带好清洁工具', NULL, '2026-01-14 22:23:58', '2026-01-14 22:23:58', 0, NULL, NULL);
INSERT INTO `service_order` VALUES (2, 'ORD20260114002', 13, 1, NULL, 13, '2026-01-15 01:23:58', '2026-01-14', NULL, '北京市朝阳区幸福小区1号楼102', 60.00, '117123', 1, '重点清洁厨房', NULL, '2026-01-14 22:23:58', '2026-01-14 22:23:58', 0, NULL, NULL);
INSERT INTO `service_order` VALUES (3, 'ORD20260114003', 14, 2, NULL, 14, '2026-01-14 22:23:58', '2026-01-14', NULL, '北京市朝阳区幸福小区2号楼201', 80.00, '829505', 2, '全屋保洁', NULL, '2026-01-14 22:23:58', '2026-01-14 22:23:58', 0, NULL, NULL);
INSERT INTO `service_order` VALUES (4, 'ORD20260114004', 12, 1, NULL, 12, '2026-01-14 20:23:58', '2026-01-14', NULL, '北京市朝阳区幸福小区1号楼101', 45.00, '796156', 3, '窗户清洁', NULL, '2026-01-14 22:23:58', '2026-01-14 22:23:58', 0, NULL, NULL);
INSERT INTO `service_order` VALUES (5, 'ORD20260114005', 13, 2, NULL, 13, '2026-01-13 22:23:58', '2026-01-13', NULL, '北京市朝阳区幸福小区1号楼102', 100.00, '492268', 4, '深度清洁', NULL, '2026-01-13 22:23:58', '2026-01-14 22:23:58', 0, NULL, NULL);
INSERT INTO `service_order` VALUES (6, 'ORD20260114006', 12, 1, NULL, 12, '2026-01-15 10:00:00', '2026-01-15', NULL, '北京市朝阳区幸福小区1号楼101', 55.00, '072870', 1, '日常保洁', NULL, '2026-01-14 22:23:58', '2026-01-14 22:23:58', 0, NULL, NULL);
INSERT INTO `service_order` VALUES (7, 'ORD20260114007', 14, 2, NULL, 14, '2026-01-16 14:00:00', '2026-01-16', NULL, '北京市朝阳区幸福小区2号楼201', 70.00, '887549', 1, '厨房深度清洁', NULL, '2026-01-14 22:23:58', '2026-01-14 22:23:58', 0, NULL, NULL);
INSERT INTO `service_order` VALUES (19, 'CHILD1768401301001', 12, 1, NULL, NULL, '2026-01-15 09:00:00', '2026-01-15', NULL, '北京市朝阳区幸福小区1号楼101', 50.00, NULL, 1, '子女代预约-日常保洁', NULL, '2026-01-14 22:35:01', '2026-01-14 22:35:01', 0, NULL, NULL);
INSERT INTO `service_order` VALUES (20, 'CHILD1768401301002', 12, 5, NULL, NULL, '2026-01-16 11:30:00', '2026-01-16', NULL, '北京市朝阳区幸福小区1号楼101', 25.00, NULL, 0, '子女代预约-午餐配送', NULL, '2026-01-14 22:35:01', '2026-01-14 22:35:01', 0, NULL, NULL);
INSERT INTO `service_order` VALUES (21, 'CHILD1768401301003', 12, 7, NULL, NULL, '2026-01-17 10:00:00', '2026-01-17', NULL, '北京市朝阳区幸福小区1号楼101', 80.00, NULL, 0, '子女代预约-日间巡诊，老人最近头晕', NULL, '2026-01-14 22:35:01', '2026-01-14 22:35:01', 0, NULL, NULL);
INSERT INTO `service_order` VALUES (22, 'CHILD1768401301004', 13, 2, NULL, NULL, '2026-01-15 14:00:00', '2026-01-15', NULL, '北京市朝阳区幸福小区1号楼102', 120.00, NULL, 0, '子女代预约-深度保洁', NULL, '2026-01-14 22:35:01', '2026-01-14 22:35:01', 0, NULL, NULL);
INSERT INTO `service_order` VALUES (23, 'CHILD1768401301005', 13, 9, NULL, NULL, '2026-01-18 08:00:00', '2026-01-18', NULL, '北京市朝阳区幸福小区1号楼102', 100.00, NULL, 0, '子女代预约-陪护服务', NULL, '2026-01-14 22:35:01', '2026-01-14 22:35:01', 0, NULL, NULL);
INSERT INTO `service_order` VALUES (24, 'SELF1768401301006', 12, 4, NULL, 12, '2026-01-15 07:30:00', '2026-01-15', NULL, '北京市朝阳区幸福小区1号楼101', 15.00, NULL, 0, '老人自己预约-早餐配送', NULL, '2026-01-14 22:35:01', '2026-01-14 22:35:01', 0, NULL, NULL);
INSERT INTO `service_order` VALUES (25, 'HIST1768401301007', 12, 1, NULL, NULL, '2026-01-07 09:00:00', '2026-01-07', NULL, '北京市朝阳区幸福小区1号楼101', 50.00, NULL, 4, '子女代预约-日常保洁-已完成', NULL, '2026-01-07 22:35:01', '2026-01-14 22:35:01', 0, NULL, NULL);
INSERT INTO `service_order` VALUES (26, 'HIST1768401301008', 12, 7, NULL, NULL, '2026-01-09 14:00:00', '2026-01-09', NULL, '北京市朝阳区幸福小区1号楼101', 80.00, NULL, 4, '子女代预约-日间巡诊-已完成', NULL, '2026-01-09 22:35:01', '2026-01-14 22:35:01', 0, NULL, NULL);

-- ----------------------------
-- Table structure for service_time_slot
-- ----------------------------
DROP TABLE IF EXISTS `service_time_slot`;
CREATE TABLE `service_time_slot`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `service_item_id` bigint NOT NULL COMMENT '服务项目ID',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `capacity` int NULL DEFAULT 5 COMMENT '该时段容量',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_service_item_id`(`service_item_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 118 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务时间段配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of service_time_slot
-- ----------------------------
INSERT INTO `service_time_slot` VALUES (97, 1, '08:00:00', '10:00:00', 3, 1, '2026-01-14 22:35:01');
INSERT INTO `service_time_slot` VALUES (98, 1, '10:00:00', '12:00:00', 3, 1, '2026-01-14 22:35:01');
INSERT INTO `service_time_slot` VALUES (99, 1, '14:00:00', '16:00:00', 3, 1, '2026-01-14 22:35:01');
INSERT INTO `service_time_slot` VALUES (100, 1, '16:00:00', '18:00:00', 3, 1, '2026-01-14 22:35:01');
INSERT INTO `service_time_slot` VALUES (101, 2, '09:00:00', '12:00:00', 2, 1, '2026-01-14 22:35:01');
INSERT INTO `service_time_slot` VALUES (102, 2, '14:00:00', '17:00:00', 2, 1, '2026-01-14 22:35:01');
INSERT INTO `service_time_slot` VALUES (103, 3, '08:00:00', '10:00:00', 5, 1, '2026-01-14 22:35:01');
INSERT INTO `service_time_slot` VALUES (104, 3, '14:00:00', '16:00:00', 5, 1, '2026-01-14 22:35:01');
INSERT INTO `service_time_slot` VALUES (105, 4, '07:00:00', '08:30:00', 20, 1, '2026-01-14 22:35:01');
INSERT INTO `service_time_slot` VALUES (106, 5, '11:00:00', '12:30:00', 20, 1, '2026-01-14 22:35:01');
INSERT INTO `service_time_slot` VALUES (107, 6, '17:00:00', '18:30:00', 20, 1, '2026-01-14 22:35:01');
INSERT INTO `service_time_slot` VALUES (108, 7, '08:00:00', '10:00:00', 5, 1, '2026-01-14 22:35:01');
INSERT INTO `service_time_slot` VALUES (109, 7, '10:00:00', '12:00:00', 5, 1, '2026-01-14 22:35:01');
INSERT INTO `service_time_slot` VALUES (110, 7, '14:00:00', '16:00:00', 5, 1, '2026-01-14 22:35:01');
INSERT INTO `service_time_slot` VALUES (111, 7, '16:00:00', '18:00:00', 5, 1, '2026-01-14 22:35:01');
INSERT INTO `service_time_slot` VALUES (112, 8, '20:00:00', '22:00:00', 3, 1, '2026-01-14 22:35:01');
INSERT INTO `service_time_slot` VALUES (113, 8, '22:00:00', '00:00:00', 3, 1, '2026-01-14 22:35:01');
INSERT INTO `service_time_slot` VALUES (114, 9, '08:00:00', '12:00:00', 3, 1, '2026-01-14 22:35:01');
INSERT INTO `service_time_slot` VALUES (115, 9, '14:00:00', '18:00:00', 3, 1, '2026-01-14 22:35:01');
INSERT INTO `service_time_slot` VALUES (116, 10, '09:00:00', '11:00:00', 5, 1, '2026-01-14 22:35:01');
INSERT INTO `service_time_slot` VALUES (117, 10, '15:00:00', '17:00:00', 5, 1, '2026-01-14 22:35:01');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `openid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信openid',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码(MD5)',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '姓名',
  `id_card` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `user_type` tinyint NULL DEFAULT NULL COMMENT '用户类型: 1-老人 2-子女 3-服务人员 4-管理员',
  `worker_type` tinyint NULL DEFAULT NULL COMMENT '服务人员类型: 1-配送员 2-保洁员 3-医疗人员 (仅userType=3时有效)',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `family_id` bigint NULL DEFAULT NULL COMMENT '家庭ID',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  `related_id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联的身份证号（老人关联子女/子女关联老人）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `openid`(`openid` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone_type`(`phone` ASC, `user_type` ASC) USING BTREE,
  INDEX `idx_openid`(`openid` ASC) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE,
  INDEX `idx_user_id_card`(`id_card` ASC) USING BTREE,
  INDEX `idx_user_related_id_card`(`related_id_card` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (2, NULL, NULL, 'e10adc3949ba59abbe56e057f20f883e', 'admin', NULL, NULL, 4, NULL, NULL, NULL, 1, '2026-01-10 17:49:58', '2026-01-11 18:07:16', 0, NULL);
INSERT INTO `sys_user` VALUES (12, NULL, '13800000001', 'e10adc3949ba59abbe56e057f20f883e', '张爷爷', '110101193501011234', NULL, 1, NULL, '北京市朝阳区幸福小区1号楼101', 1, 1, '2026-01-11 18:18:30', '2026-01-14 22:31:02', 0, NULL);
INSERT INTO `sys_user` VALUES (13, NULL, '13800000002', 'e10adc3949ba59abbe56e057f20f883e', '李奶奶', '110101194002022345', NULL, 1, NULL, '北京市朝阳区幸福小区1号楼102', 2, 1, '2026-01-11 18:18:30', '2026-01-14 22:31:02', 0, NULL);
INSERT INTO `sys_user` VALUES (14, NULL, '13800000003', 'e10adc3949ba59abbe56e057f20f883e', '王大爷', '110101193803033456', NULL, 1, NULL, '北京市朝阳区幸福小区2号楼201', 3, 1, '2026-01-11 18:18:30', '2026-01-14 22:31:02', 0, NULL);
INSERT INTO `sys_user` VALUES (15, NULL, '13800000004', 'e10adc3949ba59abbe56e057f20f883e', '赵奶奶', '110101194104044567', NULL, 1, NULL, '北京市朝阳区幸福小区2号楼202', NULL, 1, '2026-01-11 18:18:30', '2026-01-11 18:18:30', 0, NULL);
INSERT INTO `sys_user` VALUES (16, NULL, '13800000005', 'e10adc3949ba59abbe56e057f20f883e', '刘爷爷', '110101193605055678', NULL, 1, NULL, '北京市朝阳区幸福小区3号楼301', NULL, 1, '2026-01-11 18:18:30', '2026-01-11 18:18:30', 0, NULL);
INSERT INTO `sys_user` VALUES (17, NULL, '13800000001', 'e10adc3949ba59abbe56e057f20f883e', '张子女', NULL, NULL, 2, NULL, NULL, 1, 1, '2026-01-16 10:16:34', '2026-01-16 10:16:34', 0, NULL);
INSERT INTO `sys_user` VALUES (18, NULL, '13800000002', 'e10adc3949ba59abbe56e057f20f883e', '李子女', NULL, NULL, 2, NULL, NULL, NULL, 1, '2026-01-16 10:16:34', '2026-01-16 10:16:34', 0, NULL);
INSERT INTO `sys_user` VALUES (19, NULL, '13800000003', 'e10adc3949ba59abbe56e057f20f883e', '王子女', NULL, NULL, 2, NULL, NULL, NULL, 1, '2026-01-16 10:16:34', '2026-01-16 10:16:34', 0, NULL);
INSERT INTO `sys_user` VALUES (20, NULL, '13900000001', 'e10adc3949ba59abbe56e057f20f883e', '张保洁', NULL, NULL, 3, 2, NULL, NULL, 1, '2026-01-16 10:16:34', '2026-01-20 20:54:29', 0, NULL);
INSERT INTO `sys_user` VALUES (21, NULL, '13900000002', 'e10adc3949ba59abbe56e057f20f883e', '李保洁', NULL, NULL, 3, 2, NULL, NULL, 1, '2026-01-16 10:16:34', '2026-01-20 20:54:29', 0, NULL);
INSERT INTO `sys_user` VALUES (22, NULL, '13900000003', 'e10adc3949ba59abbe56e057f20f883e', '王保洁', NULL, NULL, 3, 2, NULL, NULL, 1, '2026-01-16 10:16:34', '2026-01-20 20:54:29', 0, NULL);
INSERT INTO `sys_user` VALUES (23, NULL, '13800000101', 'e10adc3949ba59abbe56e057f20f883e', '李阿姨', NULL, NULL, 3, 1, NULL, NULL, 1, '2026-01-16 13:13:44', '2026-01-20 20:54:29', 0, NULL);
INSERT INTO `sys_user` VALUES (24, NULL, '13800000102', 'e10adc3949ba59abbe56e057f20f883e', '王大姐', NULL, NULL, 3, 1, NULL, NULL, 1, '2026-01-16 13:13:44', '2026-01-20 20:54:29', 0, NULL);
INSERT INTO `sys_user` VALUES (25, NULL, '13800000103', 'e10adc3949ba59abbe56e057f20f883e', '张师傅', NULL, NULL, 3, 1, NULL, NULL, 1, '2026-01-16 13:13:44', '2026-01-20 20:54:29', 0, NULL);
INSERT INTO `sys_user` VALUES (26, NULL, '13800000104', 'e10adc3949ba59abbe56e057f20f883e', '刘阿姨', NULL, NULL, 3, 1, NULL, NULL, 1, '2026-01-16 13:13:44', '2026-01-20 20:54:29', 0, NULL);
INSERT INTO `sys_user` VALUES (27, NULL, '13800000105', 'e10adc3949ba59abbe56e057f20f883e', '陈大姐', NULL, NULL, 3, 1, NULL, NULL, 1, '2026-01-16 13:13:44', '2026-01-20 20:54:29', 0, NULL);

-- ----------------------------
-- Table structure for user_points
-- ----------------------------
DROP TABLE IF EXISTS `user_points`;
CREATE TABLE `user_points`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `points` int NULL DEFAULT 0 COMMENT '当前积分',
  `total_earned` int NULL DEFAULT 0 COMMENT '累计获得积分',
  `total_spent` int NULL DEFAULT 0 COMMENT '累计消费积分',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户积分表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_points
-- ----------------------------
INSERT INTO `user_points` VALUES (1, 12, 20, 100, 100, '2026-01-13 21:52:37', '2026-01-13 21:52:37');

SET FOREIGN_KEY_CHECKS = 1;
