/*
 Navicat Premium Data Transfer

 Source Server         : Midsummer
 Source Server Type    : MySQL
 Source Server Version : 80032 (8.0.32)
 Source Host           : localhost:3306
 Source Schema         : elderly_community

 Target Server Type    : MySQL
 Target Server Version : 80032 (8.0.32)
 File Encoding         : 65001

 Date: 03/02/2026 16:13:10
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin_log
-- ----------------------------
DROP TABLE IF EXISTS `admin_log`;
CREATE TABLE `admin_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint NULL DEFAULT NULL COMMENT '操作管理员ID',
  `admin_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作管理员姓名',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作模块',
  `operation_type` int NULL DEFAULT NULL COMMENT '操作类型: 1-新增 2-修改 3-删除 4-查询 5-其他',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作描述',
  `method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求方法',
  `request_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求URL',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求参数',
  `response_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '响应结果',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `status` int NULL DEFAULT 1 COMMENT '操作状态: 0-失败 1-成功',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误信息',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_admin_id`(`admin_id` ASC) USING BTREE,
  INDEX `idx_module`(`module` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 133 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员操作日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of admin_log
-- ----------------------------
INSERT INTO `admin_log` VALUES (1, 2, 'admin', '系统登录', 5, '超级管理员登录系统', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-25 20:26:39');
INSERT INTO `admin_log` VALUES (2, 2, 'admin', '楼栋管理', 5, '移除楼栋配送员: 1号楼 - ID=23', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-25 20:32:38');
INSERT INTO `admin_log` VALUES (3, 2, 'admin', '楼栋管理', 5, '为楼栋分配配送员: 1号楼 - 张伟', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-25 20:32:42');
INSERT INTO `admin_log` VALUES (4, 2, 'admin', '楼栋管理', 5, '为楼栋分配配送员: 1号楼 - 李娜', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-25 20:32:45');
INSERT INTO `admin_log` VALUES (5, 2, 'admin', '楼栋管理', 5, '为楼栋分配配送员: 1号楼 - 王强', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-25 20:32:49');
INSERT INTO `admin_log` VALUES (6, 2, 'admin', '楼栋管理', 5, '为楼栋分配配送员: 1号楼 - 刘敏', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-25 20:32:53');
INSERT INTO `admin_log` VALUES (7, 2, 'admin', '楼栋管理', 5, '为楼栋分配配送员: 1号楼 - 陈涛', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-25 20:32:57');
INSERT INTO `admin_log` VALUES (8, 2, 'admin', '楼栋管理', 5, '为楼栋分配配送员: 2号楼 - 赵静', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-25 20:33:02');
INSERT INTO `admin_log` VALUES (9, 2, 'admin', '楼栋管理', 5, '为楼栋分配配送员: 2号楼 - 孙浩', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-25 20:33:06');
INSERT INTO `admin_log` VALUES (10, 2, 'admin', '楼栋管理', 5, '为楼栋分配配送员: 2号楼 - 周丽', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-25 20:33:10');
INSERT INTO `admin_log` VALUES (11, 2, 'admin', '楼栋管理', 5, '为楼栋分配配送员: 2号楼 - 吴刚', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-25 20:33:15');
INSERT INTO `admin_log` VALUES (12, 2, 'admin', '楼栋管理', 5, '为楼栋分配配送员: 2号楼 - 郑晓', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-25 20:33:19');
INSERT INTO `admin_log` VALUES (13, 2, 'admin', '餐饮管理', 5, '修改周食谱: ID=1', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-25 21:20:29');
INSERT INTO `admin_log` VALUES (14, 2, 'admin', '用户管理', 5, '修改用户: 李泽轩', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-25 21:31:10');
INSERT INTO `admin_log` VALUES (15, 2, 'admin', '系统登录', 5, '超级管理员登录系统', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-26 21:03:29');
INSERT INTO `admin_log` VALUES (16, 62, 'admin1', '系统登录', 5, '普通管理员登录系统', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-26 21:03:48');
INSERT INTO `admin_log` VALUES (17, 2, 'admin', '系统登录', 5, '超级管理员登录系统', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-28 16:05:20');
INSERT INTO `admin_log` VALUES (18, 2, 'admin', '系统登录', 5, '超级管理员登录系统', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-28 16:11:10');
INSERT INTO `admin_log` VALUES (19, 62, 'admin1', '系统登录', 5, '普通管理员登录系统', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-30 14:09:40');
INSERT INTO `admin_log` VALUES (20, 2, 'admin', '系统登录', 5, '超级管理员登录系统', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-30 14:10:58');
INSERT INTO `admin_log` VALUES (21, 2, 'admin', '楼栋管理', 3, '删除楼栋', 'com.elderly.controller.AdminBuildingController.deleteBuilding', '/api/admin/building/5', '[5]', NULL, '0:0:0:0:0:0:0:1', 0, '\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'3号楼-1\' for key \'building.uk_name\'\r\n### The error may exist in com/elderly/mapper/BuildingMapper.java (best guess)\r\n### The error may involve com.elderly.mapper.BuildingMapper.deleteById-Inline\r\n### The error occurred while setting parameters\r\n### SQL: UPDATE building SET  deleted=1 WHERE id=? AND deleted=0\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'3号楼-1\' for key \'building.uk_name\'\n; Duplicate entry \'3号楼-1\' for key \'building.uk_name\'', '2026-01-30 14:36:29');
INSERT INTO `admin_log` VALUES (22, 2, 'admin', '楼栋管理', 3, '删除楼栋', 'com.elderly.controller.AdminBuildingController.deleteBuilding', '/api/admin/building/5', '[5]', NULL, '127.0.0.1', 0, '\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'3号楼-1\' for key \'building.uk_name\'\r\n### The error may exist in com/elderly/mapper/BuildingMapper.java (best guess)\r\n### The error may involve com.elderly.mapper.BuildingMapper.deleteById-Inline\r\n### The error occurred while setting parameters\r\n### SQL: UPDATE building SET  deleted=1 WHERE id=? AND deleted=0\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'3号楼-1\' for key \'building.uk_name\'\n; Duplicate entry \'3号楼-1\' for key \'building.uk_name\'', '2026-01-30 14:37:06');
INSERT INTO `admin_log` VALUES (23, 2, 'admin', '楼栋管理', 3, '删除楼栋', 'com.elderly.controller.AdminBuildingController.deleteBuilding', '/api/admin/building/5', '[5]', NULL, '127.0.0.1', 0, '\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'3号楼-1\' for key \'building.uk_name\'\r\n### The error may exist in com/elderly/mapper/BuildingMapper.java (best guess)\r\n### The error may involve com.elderly.mapper.BuildingMapper.deleteById-Inline\r\n### The error occurred while setting parameters\r\n### SQL: UPDATE building SET  deleted=1 WHERE id=? AND deleted=0\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'3号楼-1\' for key \'building.uk_name\'\n; Duplicate entry \'3号楼-1\' for key \'building.uk_name\'', '2026-01-30 14:37:11');
INSERT INTO `admin_log` VALUES (24, 2, 'admin', '楼栋管理', 1, '新增楼栋', 'com.elderly.controller.AdminBuildingController.addBuilding', '/api/admin/building', '[{\"code\":\"3号楼\",\"createTime\":\"2026-01-30 14:39:12.107231400\",\"deleted\":0,\"id\":6,\"name\":\"3号楼\",\"remark\":\"\",\"status\":1,\"updateTime\":\"2026-01-30 14:39:12.107231400\"}]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 14:39:12');
INSERT INTO `admin_log` VALUES (25, 2, 'admin', '楼栋管理', 3, '删除楼栋', 'com.elderly.controller.AdminBuildingController.deleteBuilding', '/api/admin/building/6', '[6]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 14:39:19');
INSERT INTO `admin_log` VALUES (26, 2, 'admin', '楼栋管理', 3, '移除配送员', 'com.elderly.controller.AdminBuildingController.removeWorker', '/api/admin/building/remove', '[\"2号楼\",42]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 14:39:32');
INSERT INTO `admin_log` VALUES (27, 2, 'admin', '楼栋管理', 2, '分配配送员', 'com.elderly.controller.AdminBuildingController.assignWorker', '/api/admin/building/assign', '[{\"building\":\"2号楼\",\"workerId\":42,\"isPrimary\":0}]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 14:39:36');
INSERT INTO `admin_log` VALUES (28, 62, 'admin1', '系统登录', 5, '普通管理员登录系统', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-30 14:39:47');
INSERT INTO `admin_log` VALUES (29, 62, 'admin1', '楼栋管理', 1, '新增楼栋', 'com.elderly.controller.AdminBuildingController.addBuilding', '/api/admin/building', '[{\"code\":\"3号楼\",\"createTime\":\"2026-01-30 14:40:02.682197900\",\"deleted\":0,\"id\":7,\"name\":\"3号楼\",\"remark\":\"\",\"status\":1,\"updateTime\":\"2026-01-30 14:40:02.682197900\"}]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 14:40:03');
INSERT INTO `admin_log` VALUES (30, 62, 'admin1', '楼栋管理', 3, '删除楼栋', 'com.elderly.controller.AdminBuildingController.deleteBuilding', '/api/admin/building/7', '[7]', NULL, '127.0.0.1', 0, '\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'3号楼-1\' for key \'building.uk_name\'\r\n### The error may exist in com/elderly/mapper/BuildingMapper.java (best guess)\r\n### The error may involve com.elderly.mapper.BuildingMapper.deleteById-Inline\r\n### The error occurred while setting parameters\r\n### SQL: UPDATE building SET  deleted=1 WHERE id=? AND deleted=0\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'3号楼-1\' for key \'building.uk_name\'\n; Duplicate entry \'3号楼-1\' for key \'building.uk_name\'', '2026-01-30 14:40:09');
INSERT INTO `admin_log` VALUES (31, 62, 'admin1', '楼栋管理', 3, '删除楼栋', 'com.elderly.controller.AdminBuildingController.deleteBuilding', '/api/admin/building/7', '[7]', '{\"code\":500,\"message\":\"楼栋不存在\"}', '127.0.0.1', 1, NULL, '2026-01-30 14:40:27');
INSERT INTO `admin_log` VALUES (32, 62, 'admin1', '楼栋管理', 1, '新增楼栋', 'com.elderly.controller.AdminBuildingController.addBuilding', '/api/admin/building', '[{\"code\":\"3号楼\",\"createTime\":\"2026-01-30 14:40:48.158182400\",\"deleted\":0,\"id\":8,\"name\":\"3号楼\",\"remark\":\"\",\"status\":1,\"updateTime\":\"2026-01-30 14:40:48.158182400\"}]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 14:40:48');
INSERT INTO `admin_log` VALUES (33, 62, 'admin1', '楼栋管理', 3, '删除楼栋', 'com.elderly.controller.AdminBuildingController.deleteBuilding', '/api/admin/building/8', '[8]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 14:40:51');
INSERT INTO `admin_log` VALUES (34, 62, 'admin1', '楼栋管理', 1, '新增楼栋', 'com.elderly.controller.AdminBuildingController.addBuilding', '/api/admin/building', '[{\"code\":\"3号楼\",\"createTime\":\"2026-01-30 14:53:54.299887200\",\"deleted\":0,\"id\":9,\"name\":\"3号楼\",\"remark\":\"\",\"status\":1,\"updateTime\":\"2026-01-30 14:53:54.300888200\"}]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 14:53:54');
INSERT INTO `admin_log` VALUES (35, 62, 'admin1', '楼栋管理', 3, '删除楼栋', 'com.elderly.controller.AdminBuildingController.deleteBuilding', '/api/admin/building/9', '[9]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 14:54:02');
INSERT INTO `admin_log` VALUES (36, 62, 'admin1', '楼栋管理', 1, '新增楼栋', 'com.elderly.controller.AdminBuildingController.addBuilding', '/api/admin/building', '[{\"code\":\"3号楼\",\"createTime\":\"2026-01-30 14:54:18.834221400\",\"deleted\":0,\"id\":10,\"name\":\"3号楼\",\"remark\":\"\",\"status\":1,\"updateTime\":\"2026-01-30 14:54:18.835220900\"}]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 14:54:19');
INSERT INTO `admin_log` VALUES (37, 62, 'admin1', '楼栋管理', 3, '删除楼栋', 'com.elderly.controller.AdminBuildingController.deleteBuilding', '/api/admin/building/10', '[10]', NULL, '127.0.0.1', 0, '\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'3号楼-1\' for key \'building.uk_name\'\r\n### The error may exist in com/elderly/mapper/BuildingMapper.java (best guess)\r\n### The error may involve com.elderly.mapper.BuildingMapper.deleteById-Inline\r\n### The error occurred while setting parameters\r\n### SQL: UPDATE building SET  deleted=1 WHERE id=? AND deleted=0\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'3号楼-1\' for key \'building.uk_name\'\n; Duplicate entry \'3号楼-1\' for key \'building.uk_name\'', '2026-01-30 14:54:21');
INSERT INTO `admin_log` VALUES (38, 62, 'admin1', '楼栋管理', 3, '删除楼栋', 'com.elderly.controller.AdminBuildingController.deleteBuilding', '/api/admin/building/10', '[10]', NULL, '127.0.0.1', 0, '\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'3号楼-1\' for key \'building.uk_name_deleted\'\r\n### The error may exist in com/elderly/mapper/BuildingMapper.java (best guess)\r\n### The error may involve com.elderly.mapper.BuildingMapper.deleteById-Inline\r\n### The error occurred while setting parameters\r\n### SQL: UPDATE building SET  deleted=1 WHERE id=? AND deleted=0\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'3号楼-1\' for key \'building.uk_name_deleted\'\n; Duplicate entry \'3号楼-1\' for key \'building.uk_name_deleted\'', '2026-01-30 14:56:44');
INSERT INTO `admin_log` VALUES (39, 62, 'admin1', '楼栋管理', 1, '新增楼栋', 'com.elderly.controller.AdminBuildingController.addBuilding', '/api/admin/building', '[{\"code\":\"3号楼\",\"createTime\":\"2026-01-30 14:57:09.354488800\",\"deleted\":0,\"id\":11,\"name\":\"3号楼\",\"remark\":\"\",\"status\":1,\"updateTime\":\"2026-01-30 14:57:09.354488800\"}]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 14:57:09');
INSERT INTO `admin_log` VALUES (40, 62, 'admin1', '楼栋管理', 3, '删除楼栋', 'com.elderly.controller.AdminBuildingController.deleteBuilding', '/api/admin/building/11', '[11]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 14:57:12');
INSERT INTO `admin_log` VALUES (41, 62, 'admin1', '楼栋管理', 1, '新增楼栋', 'com.elderly.controller.AdminBuildingController.addBuilding', '/api/admin/building', '[{\"code\":\"3号楼\",\"createTime\":\"2026-01-30 14:57:27.557350400\",\"deleted\":0,\"id\":12,\"name\":\"3号楼\",\"remark\":\"\",\"status\":1,\"updateTime\":\"2026-01-30 14:57:27.557350400\"}]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 14:57:28');
INSERT INTO `admin_log` VALUES (42, 62, 'admin1', '楼栋管理', 3, '删除楼栋', 'com.elderly.controller.AdminBuildingController.deleteBuilding', '/api/admin/building/12', '[12]', NULL, '127.0.0.1', 0, '\r\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'3号楼-1\' for key \'building.uk_name_deleted\'\r\n### The error may exist in com/elderly/mapper/BuildingMapper.java (best guess)\r\n### The error may involve com.elderly.mapper.BuildingMapper.deleteById-Inline\r\n### The error occurred while setting parameters\r\n### SQL: UPDATE building SET  deleted=1 WHERE id=? AND deleted=0\r\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'3号楼-1\' for key \'building.uk_name_deleted\'\n; Duplicate entry \'3号楼-1\' for key \'building.uk_name_deleted\'', '2026-01-30 14:57:29');
INSERT INTO `admin_log` VALUES (43, 62, 'admin1', '楼栋管理', 3, '移除配送员', 'com.elderly.controller.AdminBuildingController.removeWorker', '/api/admin/building/remove', '[\"1号楼\",36]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:09:32');
INSERT INTO `admin_log` VALUES (44, 62, 'admin1', '楼栋管理', 3, '移除配送员', 'com.elderly.controller.AdminBuildingController.removeWorker', '/api/admin/building/remove', '[\"1号楼\",35]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:09:34');
INSERT INTO `admin_log` VALUES (45, 62, 'admin1', '楼栋管理', 3, '移除配送员', 'com.elderly.controller.AdminBuildingController.removeWorker', '/api/admin/building/remove', '[\"1号楼\",34]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:09:35');
INSERT INTO `admin_log` VALUES (46, 62, 'admin1', '楼栋管理', 3, '移除配送员', 'com.elderly.controller.AdminBuildingController.removeWorker', '/api/admin/building/remove', '[\"1号楼\",33]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:09:38');
INSERT INTO `admin_log` VALUES (47, 62, 'admin1', '楼栋管理', 3, '移除配送员', 'com.elderly.controller.AdminBuildingController.removeWorker', '/api/admin/building/remove', '[\"1号楼\",37]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:09:40');
INSERT INTO `admin_log` VALUES (48, 62, 'admin1', '楼栋管理', 3, '移除配送员', 'com.elderly.controller.AdminBuildingController.removeWorker', '/api/admin/building/remove', '[\"2号楼\",38]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:09:42');
INSERT INTO `admin_log` VALUES (49, 62, 'admin1', '楼栋管理', 3, '移除配送员', 'com.elderly.controller.AdminBuildingController.removeWorker', '/api/admin/building/remove', '[\"2号楼\",39]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:09:45');
INSERT INTO `admin_log` VALUES (50, 62, 'admin1', '楼栋管理', 3, '移除配送员', 'com.elderly.controller.AdminBuildingController.removeWorker', '/api/admin/building/remove', '[\"2号楼\",40]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:09:48');
INSERT INTO `admin_log` VALUES (51, 62, 'admin1', '楼栋管理', 3, '移除配送员', 'com.elderly.controller.AdminBuildingController.removeWorker', '/api/admin/building/remove', '[\"2号楼\",41]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:09:50');
INSERT INTO `admin_log` VALUES (52, 62, 'admin1', '楼栋管理', 3, '移除配送员', 'com.elderly.controller.AdminBuildingController.removeWorker', '/api/admin/building/remove', '[\"2号楼\",42]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:09:52');
INSERT INTO `admin_log` VALUES (53, 62, 'admin1', '楼栋管理', 1, '新增楼栋', 'com.elderly.controller.AdminBuildingController.addBuilding', '/api/admin/building', '[{\"code\":\"1号楼\",\"createTime\":\"2026-01-30 15:10:22.710195500\",\"deleted\":0,\"id\":13,\"name\":\"1号楼\",\"remark\":\"\",\"status\":1,\"updateTime\":\"2026-01-30 15:10:22.710195500\"}]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:10:23');
INSERT INTO `admin_log` VALUES (54, 62, 'admin1', '楼栋管理', 1, '新增楼栋', 'com.elderly.controller.AdminBuildingController.addBuilding', '/api/admin/building', '[{\"code\":\"2号楼\",\"createTime\":\"2026-01-30 15:10:36.539213300\",\"deleted\":0,\"id\":14,\"name\":\"2号楼\",\"remark\":\"\",\"status\":1,\"updateTime\":\"2026-01-30 15:10:36.540213300\"}]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:10:37');
INSERT INTO `admin_log` VALUES (55, 62, 'admin1', '楼栋管理', 1, '新增楼栋', 'com.elderly.controller.AdminBuildingController.addBuilding', '/api/admin/building', '[{\"code\":\"3号楼\",\"createTime\":\"2026-01-30 15:10:52.252455100\",\"deleted\":0,\"id\":15,\"name\":\"3号楼\",\"remark\":\"\",\"status\":1,\"updateTime\":\"2026-01-30 15:10:52.252455100\"}]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:10:52');
INSERT INTO `admin_log` VALUES (56, 62, 'admin1', '楼栋管理', 3, '删除楼栋', 'com.elderly.controller.AdminBuildingController.deleteBuilding', '/api/admin/building/15', '[15]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:11:37');
INSERT INTO `admin_log` VALUES (57, 62, 'admin1', '楼栋管理', 1, '新增楼栋', 'com.elderly.controller.AdminBuildingController.addBuilding', '/api/admin/building', '[{\"code\":\"3号楼\",\"createTime\":\"2026-01-30 15:11:59.889861900\",\"deleted\":0,\"id\":16,\"name\":\"3号楼\",\"remark\":\"\",\"status\":1,\"updateTime\":\"2026-01-30 15:11:59.889861900\"}]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:12:00');
INSERT INTO `admin_log` VALUES (58, 62, 'admin1', '楼栋管理', 3, '删除楼栋', 'com.elderly.controller.AdminBuildingController.deleteBuilding', '/api/admin/building/16', '[16]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:12:03');
INSERT INTO `admin_log` VALUES (59, 62, 'admin1', '楼栋管理', 1, '新增楼栋', 'com.elderly.controller.AdminBuildingController.addBuilding', '/api/admin/building', '[{\"code\":\"3号楼\",\"createTime\":\"2026-01-30 15:13:48.190900100\",\"deleted\":0,\"id\":17,\"name\":\"3号楼\",\"remark\":\"\",\"status\":1,\"updateTime\":\"2026-01-30 15:13:48.190900100\"}]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:13:48');
INSERT INTO `admin_log` VALUES (60, 62, 'admin1', '楼栋管理', 3, '删除楼栋', 'com.elderly.controller.AdminBuildingController.deleteBuilding', '/api/admin/building/17', '[17]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:18:13');
INSERT INTO `admin_log` VALUES (61, 62, 'admin1', '楼栋管理', 1, '新增楼栋', 'com.elderly.controller.AdminBuildingController.addBuilding', '/api/admin/building', '[{\"code\":\"3号楼\",\"createTime\":\"2026-01-30 15:18:29.612544600\",\"id\":18,\"name\":\"3号楼\",\"remark\":\"\",\"status\":1,\"updateTime\":\"2026-01-30 15:18:29.612544600\"}]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:18:30');
INSERT INTO `admin_log` VALUES (62, 62, 'admin1', '楼栋管理', 1, '新增楼栋', 'com.elderly.controller.AdminBuildingController.addBuilding', '/api/admin/building', '[{\"code\":\"1号楼\",\"createTime\":\"2026-01-30 15:19:55.249424800\",\"id\":16,\"name\":\"1号楼\",\"remark\":\"\",\"status\":1,\"updateTime\":\"2026-01-30 15:19:55.249424800\"}]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:19:55');
INSERT INTO `admin_log` VALUES (63, 62, 'admin1', '楼栋管理', 1, '新增楼栋', 'com.elderly.controller.AdminBuildingController.addBuilding', '/api/admin/building', '[{\"code\":\"2号楼\",\"createTime\":\"2026-01-30 15:20:10.855002300\",\"id\":17,\"name\":\"2号楼\",\"remark\":\"\",\"status\":1,\"updateTime\":\"2026-01-30 15:20:10.855002300\"}]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:20:11');
INSERT INTO `admin_log` VALUES (64, 62, 'admin1', '楼栋管理', 2, '分配配送员', 'com.elderly.controller.AdminBuildingController.assignWorker', '/api/admin/building/assign', '[{\"building\":\"1号楼\",\"workerId\":33,\"isPrimary\":1}]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:20:28');
INSERT INTO `admin_log` VALUES (65, 62, 'admin1', '楼栋管理', 2, '分配配送员', 'com.elderly.controller.AdminBuildingController.assignWorker', '/api/admin/building/assign', '[{\"building\":\"1号楼\",\"workerId\":34,\"isPrimary\":0}]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:20:31');
INSERT INTO `admin_log` VALUES (66, 62, 'admin1', '楼栋管理', 2, '分配配送员', 'com.elderly.controller.AdminBuildingController.assignWorker', '/api/admin/building/assign', '[{\"building\":\"2号楼\",\"workerId\":35,\"isPrimary\":1}]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:20:34');
INSERT INTO `admin_log` VALUES (67, 62, 'admin1', '楼栋管理', 2, '分配配送员', 'com.elderly.controller.AdminBuildingController.assignWorker', '/api/admin/building/assign', '[{\"building\":\"2号楼\",\"workerId\":36,\"isPrimary\":0}]', '{\"code\":200,\"message\":\"success\"}', '127.0.0.1', 1, NULL, '2026-01-30 15:20:39');
INSERT INTO `admin_log` VALUES (68, 62, 'admin1', '保洁管理', 1, '新增保洁排班, 员工ID: 47 日期: 2026-01-31', 'com.elderly.controller.AdminCleaningController.addSchedule', '/api/admin/cleaning/schedules', '[{\"createTime\":\"2026-01-30 19:44:14.073771300\",\"endTime\":\"18:00:00\",\"id\":45,\"scheduleDate\":\"2026-01-31\",\"startTime\":\"08:00:00\",\"status\":1,\"workerId\":47}]', '{\"code\":200,\"data\":{\"createTime\":\"2026-01-30 19:44:14.073771300\",\"endTime\":\"18:00:00\",\"id\":45,\"scheduleDate\":\"2026-01-31\",\"startTime\":\"08:00:00\",\"status\":1,\"workerId\":47},\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 19:44:14');
INSERT INTO `admin_log` VALUES (69, 2, 'admin', '系统登录', 5, '超级管理员登录系统', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-30 19:44:27');
INSERT INTO `admin_log` VALUES (70, 2, 'admin', '管理员管理', 1, '新增管理员: test', 'com.elderly.controller.AdminUserController.addAdmin', '/api/admin/user/admin', '[{\"phone\":\"13500000002\",\"name\":\"test\",\"password\":\"123456\",\"adminRole\":2},\"Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIyIiwidXNlclR5cGUiOjQsImlhdCI6MTc2OTc3MzQ2NywiZXhwIjoxNzY5ODU5ODY3fQ.0-ONqrpHvhkWZzE_lRNAaFEEh8DiLhVDdrkUc0SvArA\"]', '{\"code\":200,\"data\":67,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 19:45:03');
INSERT INTO `admin_log` VALUES (71, 2, 'admin', '管理员管理', 1, '新增管理员: test', 'com.elderly.controller.AdminUserController.addAdmin', '/api/admin/user/admin', '[{\"phone\":\"13500002000\",\"name\":\"test\",\"password\":\"123456\",\"adminRole\":2}]', '{\"code\":200,\"data\":68,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 20:04:52');
INSERT INTO `admin_log` VALUES (72, 2, 'admin', '管理员管理', 2, '修改管理员状态 ID: 62 状态: 启用', 'com.elderly.controller.AdminUserController.toggleAdminStatus', '/api/admin/user/admin/62/status', '[62,{\"status\":1}]', '{\"code\":200,\"data\":true,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 20:21:33');
INSERT INTO `admin_log` VALUES (73, 2, 'admin', '管理员管理', 2, '修改管理员状态 ID: 62 状态: 禁用', 'com.elderly.controller.AdminUserController.toggleAdminStatus', '/api/admin/user/admin/62/status', '[62,{\"status\":0}]', '{\"code\":200,\"data\":true,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 20:22:02');
INSERT INTO `admin_log` VALUES (74, 2, 'admin', '管理员管理', 2, '修改管理员状态 ID: 62 状态: 启用', 'com.elderly.controller.AdminUserController.toggleAdminStatus', '/api/admin/user/admin/62/status', '[62,{\"status\":1}]', '{\"code\":200,\"data\":true,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 20:22:07');
INSERT INTO `admin_log` VALUES (75, 2, 'admin', '管理员管理', 2, '修改管理员状态 ID: 62 状态: 禁用', 'com.elderly.controller.AdminUserController.toggleAdminStatus', '/api/admin/user/admin/62/status', '[62,{\"status\":0}]', '{\"code\":200,\"data\":true,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 20:22:14');
INSERT INTO `admin_log` VALUES (76, 2, 'admin', '管理员管理', 2, '修改管理员状态 ID: 62 状态: 启用', 'com.elderly.controller.AdminUserController.toggleAdminStatus', '/api/admin/user/admin/62/status', '[62,{\"status\":1}]', '{\"code\":200,\"data\":true,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 20:22:19');
INSERT INTO `admin_log` VALUES (77, 2, 'admin', '管理员管理', 2, '修改管理员状态 ID: 68 状态: 禁用', 'com.elderly.controller.AdminUserController.toggleAdminStatus', '/api/admin/user/admin/68/status', '[68,{\"status\":0}]', '{\"code\":200,\"data\":true,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 20:22:35');
INSERT INTO `admin_log` VALUES (78, 2, 'admin', '系统登录', 5, '超级管理员登录系统', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-30 20:22:47');
INSERT INTO `admin_log` VALUES (79, 2, 'admin', '管理员管理', 2, '修改管理员状态 ID: 68 状态: 启用', 'com.elderly.controller.AdminUserController.toggleAdminStatus', '/api/admin/user/admin/68/status', '[68,{\"status\":1}]', '{\"code\":200,\"data\":true,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 20:22:51');
INSERT INTO `admin_log` VALUES (80, 2, 'admin', '应急管理', 2, '处理应急呼叫 ID: 3', 'com.elderly.controller.EmergencyController.process', '/api/emergency/3/process', '[3]', '{\"code\":200,\"data\":true,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 20:22:59');
INSERT INTO `admin_log` VALUES (81, 2, 'admin', '应急管理', 2, '处理应急呼叫 ID: 2', 'com.elderly.controller.EmergencyController.process', '/api/emergency/2/process', '[2]', '{\"code\":200,\"data\":true,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 20:32:47');
INSERT INTO `admin_log` VALUES (82, 2, 'admin', '管理员管理', 2, '修改管理员状态 ID: 68 状态: 禁用', 'com.elderly.controller.AdminUserController.toggleAdminStatus', '/api/admin/user/admin/68/status', '[68,{\"status\":0}]', '{\"code\":200,\"data\":true,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 20:32:55');
INSERT INTO `admin_log` VALUES (83, 2, 'admin', '管理员管理', 2, '修改管理员状态 ID: 68 状态: 启用', 'com.elderly.controller.AdminUserController.toggleAdminStatus', '/api/admin/user/admin/68/status', '[68,{\"status\":1}]', '{\"code\":200,\"data\":true,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 20:32:58');
INSERT INTO `admin_log` VALUES (84, 2, 'admin', '积分管理', 2, '积分充值, 用户ID: 66 金额: 50', 'com.elderly.controller.AdminPointsController.recharge', '/api/admin/points/recharge', '[{\"userId\":66,\"amount\":50,\"remark\":\"管理员充值\"}]', '{\"code\":200,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 20:33:13');
INSERT INTO `admin_log` VALUES (85, 2, 'admin', '应急管理', 2, '处理应急呼叫 ID: 1', 'com.elderly.controller.EmergencyController.process', '/api/emergency/1/process', '[1]', '{\"code\":200,\"data\":true,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 22:55:27');
INSERT INTO `admin_log` VALUES (86, 2, 'admin', '餐饮管理', 2, '修改菜品模板: 免费白米饭', 'com.elderly.controller.AdminMealController.updateDishTemplate', '/api/admin/meal/dish-templates/1', '[1,{\"category\":1,\"createTime\":\"2026-01-22 18:30:14\",\"deleted\":0,\"description\":\"软糯香甜的白米饭\",\"dishName\":\"免费白米饭\",\"id\":1,\"mealTypes\":\"2,3\",\"nutritionInfo\":\"碳水化合物为主\",\"price\":5,\"status\":1,\"updateTime\":\"2026-01-22 18:30:14\"}]', '{\"code\":200,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 22:56:15');
INSERT INTO `admin_log` VALUES (87, 2, 'admin', '餐饮管理', 1, '新增周食谱: 2026-03-02~2026-02-08', 'com.elderly.controller.AdminMealController.addWeeklyMenu', '/api/admin/meal/weekly-menus', '[{\"createTime\":\"2026-01-30 22:56:45.036350300\",\"id\":2,\"status\":0,\"updateTime\":\"2026-01-30 22:56:45.036350300\",\"weekEndDate\":\"2026-02-08\",\"weekStartDate\":\"2026-03-02\"}]', '{\"code\":200,\"data\":{\"createTime\":\"2026-01-30 22:56:45.036350300\",\"id\":2,\"status\":0,\"updateTime\":\"2026-01-30 22:56:45.036350300\",\"weekEndDate\":\"2026-02-08\",\"weekStartDate\":\"2026-03-02\"},\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 22:56:45');
INSERT INTO `admin_log` VALUES (88, 2, 'admin', '餐饮管理', 1, '从模板添加菜品, 菜单ID: 1 模板ID: 3', 'com.elderly.controller.AdminMealController.addDishFromTemplate', '/api/admin/meal/weekly-menus/1/add-from-template', '[1,{\"templateId\":3,\"dishDate\":\"2026-01-19\",\"mealType\":1}]', '{\"code\":200,\"data\":{\"createTime\":\"2026-01-30 22:56:53.710183800\",\"description\":\"养胃小米粥\",\"dishDate\":\"2026-01-19\",\"dishName\":\"小米粥\",\"id\":27,\"mealType\":1,\"nutritionInfo\":\"易消化，适合老年人\",\"price\":5,\"sort\":0,\"status\":1,\"weeklyMenuId\":1},\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 22:56:54');
INSERT INTO `admin_log` VALUES (89, 2, 'admin', '餐饮管理', 1, '从模板添加菜品, 菜单ID: 1 模板ID: 2', 'com.elderly.controller.AdminMealController.addDishFromTemplate', '/api/admin/meal/weekly-menus/1/add-from-template', '[1,{\"templateId\":2,\"dishDate\":\"2026-01-19\",\"mealType\":1}]', '{\"code\":200,\"data\":{\"createTime\":\"2026-01-30 22:56:53.733010700\",\"description\":\"松软可口的馒头\",\"dishDate\":\"2026-01-19\",\"dishName\":\"馒头\",\"id\":28,\"mealType\":1,\"nutritionInfo\":\"碳水化合物为主\",\"price\":3,\"sort\":0,\"status\":1,\"weeklyMenuId\":1},\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 22:56:54');
INSERT INTO `admin_log` VALUES (90, 2, 'admin', '餐饮管理', 1, '从模板添加菜品, 菜单ID: 1 模板ID: 12', 'com.elderly.controller.AdminMealController.addDishFromTemplate', '/api/admin/meal/weekly-menus/1/add-from-template', '[1,{\"templateId\":12,\"dishDate\":\"2026-01-19\",\"mealType\":1}]', '{\"code\":200,\"data\":{\"createTime\":\"2026-01-30 22:56:53.764749200\",\"description\":\"松软蒸蛋糕\",\"dishDate\":\"2026-01-19\",\"dishName\":\"蒸蛋糕\",\"id\":29,\"mealType\":1,\"nutritionInfo\":\"易消化\",\"price\":6,\"sort\":0,\"status\":1,\"weeklyMenuId\":1},\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 22:56:54');
INSERT INTO `admin_log` VALUES (91, 2, 'admin', '餐饮管理', 1, '从模板添加菜品, 菜单ID: 1 模板ID: 11', 'com.elderly.controller.AdminMealController.addDishFromTemplate', '/api/admin/meal/weekly-menus/1/add-from-template', '[1,{\"templateId\":11,\"dishDate\":\"2026-01-19\",\"mealType\":1}]', '{\"code\":200,\"data\":{\"createTime\":\"2026-01-30 22:56:53.798655500\",\"description\":\"香甜豆沙包\",\"dishDate\":\"2026-01-19\",\"dishName\":\"豆沙包\",\"id\":30,\"mealType\":1,\"nutritionInfo\":\"适量糖分\",\"price\":5,\"sort\":0,\"status\":1,\"weeklyMenuId\":1},\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 22:56:54');
INSERT INTO `admin_log` VALUES (92, 2, 'admin', '餐饮管理', 1, '从模板添加菜品, 菜单ID: 1 模板ID: 3', 'com.elderly.controller.AdminMealController.addDishFromTemplate', '/api/admin/meal/weekly-menus/1/add-from-template', '[1,{\"templateId\":3,\"dishDate\":\"2026-02-02\",\"mealType\":1}]', '{\"code\":200,\"data\":{\"createTime\":\"2026-01-30 22:57:05.471238400\",\"description\":\"养胃小米粥\",\"dishDate\":\"2026-02-02\",\"dishName\":\"小米粥\",\"id\":31,\"mealType\":1,\"nutritionInfo\":\"易消化，适合老年人\",\"price\":5,\"sort\":0,\"status\":1,\"weeklyMenuId\":1},\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 22:57:05');
INSERT INTO `admin_log` VALUES (93, 2, 'admin', '餐饮管理', 1, '从模板添加菜品, 菜单ID: 1 模板ID: 2', 'com.elderly.controller.AdminMealController.addDishFromTemplate', '/api/admin/meal/weekly-menus/1/add-from-template', '[1,{\"templateId\":2,\"dishDate\":\"2026-02-02\",\"mealType\":1}]', '{\"code\":200,\"data\":{\"createTime\":\"2026-01-30 22:57:05.503336700\",\"description\":\"松软可口的馒头\",\"dishDate\":\"2026-02-02\",\"dishName\":\"馒头\",\"id\":32,\"mealType\":1,\"nutritionInfo\":\"碳水化合物为主\",\"price\":3,\"sort\":0,\"status\":1,\"weeklyMenuId\":1},\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 22:57:06');
INSERT INTO `admin_log` VALUES (94, 2, 'admin', '餐饮管理', 1, '从模板添加菜品, 菜单ID: 1 模板ID: 12', 'com.elderly.controller.AdminMealController.addDishFromTemplate', '/api/admin/meal/weekly-menus/1/add-from-template', '[1,{\"templateId\":12,\"dishDate\":\"2026-02-02\",\"mealType\":1}]', '{\"code\":200,\"data\":{\"createTime\":\"2026-01-30 22:57:05.571780600\",\"description\":\"松软蒸蛋糕\",\"dishDate\":\"2026-02-02\",\"dishName\":\"蒸蛋糕\",\"id\":33,\"mealType\":1,\"nutritionInfo\":\"易消化\",\"price\":6,\"sort\":0,\"status\":1,\"weeklyMenuId\":1},\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 22:57:06');
INSERT INTO `admin_log` VALUES (95, 2, 'admin', '餐饮管理', 1, '从模板添加菜品, 菜单ID: 1 模板ID: 11', 'com.elderly.controller.AdminMealController.addDishFromTemplate', '/api/admin/meal/weekly-menus/1/add-from-template', '[1,{\"templateId\":11,\"dishDate\":\"2026-02-02\",\"mealType\":1}]', '{\"code\":200,\"data\":{\"createTime\":\"2026-01-30 22:57:05.595854600\",\"description\":\"香甜豆沙包\",\"dishDate\":\"2026-02-02\",\"dishName\":\"豆沙包\",\"id\":34,\"mealType\":1,\"nutritionInfo\":\"适量糖分\",\"price\":5,\"sort\":0,\"status\":1,\"weeklyMenuId\":1},\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 22:57:06');
INSERT INTO `admin_log` VALUES (96, 2, 'admin', '用户管理', 1, '新增用户: 杨梓帆 (13622598933)', 'com.elderly.controller.UserController.addUser', '/api/user', '[{\"phone\":\"13622598933\",\"name\":\"杨梓帆\",\"gender\":1,\"idCard\":\"440513200402201456\",\"relatedIdCard\":\"\",\"userType\":2,\"address\":\"1号楼 201\"}]', '{\"code\":200,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 22:58:38');
INSERT INTO `admin_log` VALUES (97, 2, 'admin', '用户管理', 3, '删除用户 ID: 69', 'com.elderly.controller.UserController.deleteUser', '/api/user/69', '[69]', '{\"code\":200,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 22:59:14');
INSERT INTO `admin_log` VALUES (98, 2, 'admin', '用户管理', 1, '新增用户: 杨梓帆 (13622598933)', 'com.elderly.controller.UserController.addUser', '/api/user', '[{\"phone\":\"13622598933\",\"name\":\"杨梓帆\",\"gender\":1,\"idCard\":\"440513200402201245\",\"relatedIdCard\":\"\",\"userType\":3,\"workerType\":3,\"address\":\"\"}]', '{\"code\":200,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 22:59:43');
INSERT INTO `admin_log` VALUES (99, 2, 'admin', '保洁管理', 1, '新增保洁排班, 员工ID: 44 日期: 2026-01-31', 'com.elderly.controller.AdminCleaningController.addSchedule', '/api/admin/cleaning/schedules', '[{\"createTime\":\"2026-01-30 23:00:01.759703200\",\"endTime\":\"18:00:00\",\"id\":46,\"scheduleDate\":\"2026-01-31\",\"startTime\":\"08:00:00\",\"status\":1,\"workerId\":44}]', '{\"code\":200,\"data\":{\"createTime\":\"2026-01-30 23:00:01.759703200\",\"endTime\":\"18:00:00\",\"id\":46,\"scheduleDate\":\"2026-01-31\",\"startTime\":\"08:00:00\",\"status\":1,\"workerId\":44},\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 23:00:02');
INSERT INTO `admin_log` VALUES (100, 2, 'admin', '医疗管理', 1, '新增医生: 杨梓帆', 'com.elderly.controller.AdminMedicalController.addDoctor', '/api/admin/medical/doctors', '[{\"createTime\":\"2026-01-30 23:01:49.857180700\",\"id\":14,\"introduction\":\"已给窝里giaogiao\",\"maxQueueLimit\":10,\"name\":\"杨梓帆\",\"specialty\":\"外科\",\"status\":1,\"title\":\"主治医师\",\"userId\":70}]', '{\"code\":200,\"data\":{\"createTime\":\"2026-01-30 23:01:49.857180700\",\"id\":14,\"introduction\":\"已给窝里giaogiao\",\"maxQueueLimit\":10,\"name\":\"杨梓帆\",\"specialty\":\"外科\",\"status\":1,\"title\":\"主治医师\",\"userId\":70},\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 23:01:50');
INSERT INTO `admin_log` VALUES (101, 2, 'admin', '医疗管理', 1, '新增医生排班, 医生ID: 14 日期: 2026-01-30', 'com.elderly.controller.AdminMedicalController.addDuty', '/api/admin/medical/duties', '[{\"createTime\":\"2026-01-30 23:02:10.418437900\",\"doctorId\":14,\"dutyDate\":\"2026-01-30\",\"dutyType\":2,\"endTime\":\"08:00:00\",\"id\":9,\"isNightShift\":1,\"startTime\":\"20:00:00\",\"status\":1}]', '{\"code\":200,\"data\":{\"createTime\":\"2026-01-30 23:02:10.418437900\",\"doctorId\":14,\"dutyDate\":\"2026-01-30\",\"dutyType\":2,\"endTime\":\"08:00:00\",\"id\":9,\"isNightShift\":1,\"startTime\":\"20:00:00\",\"status\":1},\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 23:02:10');
INSERT INTO `admin_log` VALUES (102, 2, 'admin', '用户管理', 2, '修改用户信息 ID: 28', 'com.elderly.controller.UserController.updateUser', '/api/user/28', '[28,{\"address\":\"1号楼 101\",\"gender\":1,\"id\":28,\"idCard\":\"440203196507180069\",\"name\":\"李四\",\"relatedIdCard\":\"440203199908140123\",\"updateTime\":\"2026-01-30 23:13:25.786470400\"}]', '{\"code\":200,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-30 23:13:26');
INSERT INTO `admin_log` VALUES (103, 2, 'admin', '系统登录', 5, '超级管理员登录系统', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-01-31 18:52:36');
INSERT INTO `admin_log` VALUES (104, 2, 'admin', '用户管理', 1, '\'批量导入用户\'', 'com.elderly.controller.AdminUserController.importUsers', '/api/admin/user/import', '[]', '{\"code\":500,\"message\":\"导入完成。成功：6 条。 失败详情：第2行保存失败：\\r\\n### Error updating database.  Cause: com.mysql.cj.jdbc.exceptions.MysqlDataTruncation: Data truncation: Data too long for column \'related_id_card\' at row 1\\r\\n### The error may exist in com/elderly/mapper/UserMapper.java (best guess)\\r\\n### The error may involve com.elderly.mapper.UserMapper.insert-Inline\\r\\n### The error occurred while setting parameters\\r\\n### SQL: INSERT INTO sys_user  ( phone, password, name,  id_card,  user_type,   address,  related_id_card, status, create_time, update_time, deleted )  VALUES (  ?, ?, ?,  ?,  ?,   ?,  ?, ?, ?, ?, ?  )\\r\\n### Cause: com.mysql.cj.jdbc.exceptions.MysqlDataTruncation: Data truncation: Data too long for column \'related_id_card\' at row 1\\n; Data truncation: Data too long for column \'related_id_card\' at row 1；第6行保存失败：\\r\\n### Error updating database.  Cause: com.mysql.cj.jdbc.exceptions.MysqlDataTruncation: Data truncation: Data too long for column \'related_id_card\' at row 1\\r\\n### The error may exist in com/elderly/mapper/UserMapper.java (best guess)\\r\\n### The error may involve com.elderly.mapper.UserMapper.insert-Inline\\r\\n### The error occurred while setting parameters\\r\\n### SQL: INSERT INTO sys_user  ( phone, password, name,  id_card,  user_type,   address,  related_id_card, status, create_time, update_time, deleted )  VALUES (  ?, ?, ?,  ?,  ?,   ?,  ?, ?, ?, ?, ?  )\\r\\n### Cause: com.mysql.cj.jdbc.exceptions.MysqlDataTruncation: Data truncation: Data too long for column \'related_id_card\' at row 1\\n; Data truncation: Data too long for column \'related_id_card\' at row 1\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 19:30:13');
INSERT INTO `admin_log` VALUES (105, 2, 'admin', '用户管理', 2, '修改用户信息 ID: 73', 'com.elderly.controller.UserController.updateUser', '/api/user/73', '[73,{\"address\":\"2号楼 501\",\"gender\":1,\"id\":73,\"idCard\":\"310101196010122568\",\"name\":\"钱桂兰\",\"relatedIdCard\":\"130101198503086321\",\"updateTime\":\"2026-01-31 19:31:04.318460300\"}]', '{\"code\":200,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 19:31:04');
INSERT INTO `admin_log` VALUES (106, 2, 'admin', '用户管理', 2, '修改用户信息 ID: 74', 'com.elderly.controller.UserController.updateUser', '/api/user/74', '[74,{\"address\":\"2号楼 501\",\"gender\":1,\"id\":74,\"idCard\":\"440101195807093652\",\"name\":\"孙德福\",\"relatedIdCard\":\"130101198503086321\",\"updateTime\":\"2026-01-31 19:31:15.392685\"}]', '{\"code\":200,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 19:31:15');
INSERT INTO `admin_log` VALUES (107, 2, 'admin', '用户管理', 3, '批量删除用户 ID: 73,74,75,76,71,72', 'com.elderly.controller.AdminUserController.batchDelete', '/api/admin/user/batch', '[[73,74,75,76,71,72]]', '{\"code\":200,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 19:44:58');
INSERT INTO `admin_log` VALUES (108, 2, 'admin', '用户管理', 1, '\'批量导入用户\'', 'com.elderly.controller.AdminUserController.importUsers', '/api/admin/user/import', '[]', '{\"code\":500,\"message\":\"导入完成。成功：2 条。 失败详情：第3行保存失败：\\r\\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'13410000002-2\' for key \'sys_user.uk_phone_type\'\\r\\n### The error may exist in com/elderly/mapper/UserMapper.java (best guess)\\r\\n### The error may involve com.elderly.mapper.UserMapper.insert-Inline\\r\\n### The error occurred while setting parameters\\r\\n### SQL: INSERT INTO sys_user  ( phone, password, name,  id_card,  user_type,   address,  related_id_card, status, create_time, update_time, deleted )  VALUES (  ?, ?, ?,  ?,  ?,   ?,  ?, ?, ?, ?, ?  )\\r\\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'13410000002-2\' for key \'sys_user.uk_phone_type\'\\n; Duplicate entry \'13410000002-2\' for key \'sys_user.uk_phone_type\'；第4行保存失败：\\r\\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'13410000003-2\' for key \'sys_user.uk_phone_type\'\\r\\n### The error may exist in com/elderly/mapper/UserMapper.java (best guess)\\r\\n### The error may involve com.elderly.mapper.UserMapper.insert-Inline\\r\\n### The error occurred while setting parameters\\r\\n### SQL: INSERT INTO sys_user  ( phone, password, name,  id_card,  user_type,   address,  related_id_card, status, create_time, update_time, deleted )  VALUES (  ?, ?, ?,  ?,  ?,   ?,  ?, ?, ?, ?, ?  )\\r\\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'13410000003-2\' for key \'sys_user.uk_phone_type\'\\n; Duplicate entry \'13410000003-2\' for key \'sys_user.uk_phone_type\'；第5行保存失败：\\r\\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'13410000004-1\' for key \'sys_user.uk_phone_type\'\\r\\n### The error may exist in com/elderly/mapper/UserMapper.java (best guess)\\r\\n### The error may involve com.elderly.mapper.UserMapper.insert-Inline\\r\\n### The error occurred while setting parameters\\r\\n### SQL: INSERT INTO sys_user  ( phone, password,', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 19:45:29');
INSERT INTO `admin_log` VALUES (109, 2, 'admin', '用户管理', 3, '批量删除用户 ID: 81,77', 'com.elderly.controller.AdminUserController.batchDelete', '/api/admin/user/batch', '[[81,77]]', '{\"code\":200,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 19:48:03');
INSERT INTO `admin_log` VALUES (110, 2, 'admin', '用户管理', 1, '\'批量导入用户\'', 'com.elderly.controller.AdminUserController.importUsers', '/api/admin/user/import', '[]', '{\"code\":200,\"data\":\"导入完成。成功：8 条。\",\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 20:32:05');
INSERT INTO `admin_log` VALUES (111, 2, 'admin', '用户管理', 3, '批量删除用户 ID: 87,88,89,90,86,85,92,91', 'com.elderly.controller.AdminUserController.batchDelete', '/api/admin/user/batch', '[[87,88,89,90,86,85,92,91]]', '{\"code\":200,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 20:34:28');
INSERT INTO `admin_log` VALUES (112, 2, 'admin', '用户管理', 1, '\'批量导入用户\'', 'com.elderly.controller.AdminUserController.importUsers', '/api/admin/user/import', '[]', '{\"code\":500,\"message\":\"导入完成。成功：0 条。 失败详情：第2行保存失败：\\r\\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'13410000001-1\' for key \'sys_user.uk_phone_type\'\\r\\n### The error may exist in com/elderly/mapper/UserMapper.java (best guess)\\r\\n### The error may involve com.elderly.mapper.UserMapper.insert-Inline\\r\\n### The error occurred while setting parameters\\r\\n### SQL: INSERT INTO sys_user  ( phone, password, name,  id_card,  user_type,   address,  related_id_card, status, create_time, update_time, deleted )  VALUES (  ?, ?, ?,  ?,  ?,   ?,  ?, ?, ?, ?, ?  )\\r\\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'13410000001-1\' for key \'sys_user.uk_phone_type\'\\n; Duplicate entry \'13410000001-1\' for key \'sys_user.uk_phone_type\'；第3行保存失败：\\r\\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'13410000002-2\' for key \'sys_user.uk_phone_type\'\\r\\n### The error may exist in com/elderly/mapper/UserMapper.java (best guess)\\r\\n### The error may involve com.elderly.mapper.UserMapper.insert-Inline\\r\\n### The error occurred while setting parameters\\r\\n### SQL: INSERT INTO sys_user  ( phone, password, name,  id_card,  user_type,   address,  related_id_card, status, create_time, update_time, deleted )  VALUES (  ?, ?, ?,  ?,  ?,   ?,  ?, ?, ?, ?, ?  )\\r\\n### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'13410000002-2\' for key \'sys_user.uk_phone_type\'\\n; Duplicate entry \'13410000002-2\' for key \'sys_user.uk_phone_type\'；第4行保存失败：\\r\\n### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry \'13410000003-2\' for key \'sys_user.uk_phone_type\'\\r\\n### The error may exist in com/elderly/mapper/UserMapper.java (best guess)\\r\\n### The error may involve com.elderly.mapper.UserMapper.insert-Inline\\r\\n### The error occurred while setting parameters\\r\\n### SQL: INSERT INTO sys_user  ( phone, password,', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 20:43:53');
INSERT INTO `admin_log` VALUES (113, 2, 'admin', '用户管理', 1, '\'批量导入用户\'', 'com.elderly.controller.AdminUserController.importUsers', '/api/admin/user/import', '[]', '{\"code\":200,\"data\":\"导入成功：8 条。\",\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 20:51:57');
INSERT INTO `admin_log` VALUES (114, 2, 'admin', '用户管理', 3, '批量删除用户 ID: 103,104,105,106,107,101,108,102', 'com.elderly.controller.AdminUserController.batchDelete', '/api/admin/user/batch', '[[103,104,105,106,107,101,108,102]]', '{\"code\":200,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 20:52:31');
INSERT INTO `admin_log` VALUES (115, 2, 'admin', '用户管理', 1, '\'批量导入用户\'', 'com.elderly.controller.AdminUserController.importUsers', '/api/admin/user/import', '[]', '{\"code\":200,\"data\":\"导入成功：8 条。\",\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 20:52:39');
INSERT INTO `admin_log` VALUES (116, 2, 'admin', '用户管理', 3, '批量删除用户 ID: 111,112,113,114,115,116,109,110', 'com.elderly.controller.AdminUserController.batchDelete', '/api/admin/user/batch', '[[111,112,113,114,115,116,109,110]]', '{\"code\":200,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 20:52:50');
INSERT INTO `admin_log` VALUES (117, 2, 'admin', '用户管理', 1, '\'批量导入用户\'', 'com.elderly.controller.AdminUserController.importUsers', '/api/admin/user/import', '[]', '{\"code\":200,\"data\":\"导入成功：6 条。\",\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 21:11:14');
INSERT INTO `admin_log` VALUES (118, 2, 'admin', '用户管理', 3, '批量删除用户 ID: 119,120,121,117,122,118', 'com.elderly.controller.AdminUserController.batchDelete', '/api/admin/user/batch', '[[119,120,121,117,122,118]]', '{\"code\":200,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 21:11:30');
INSERT INTO `admin_log` VALUES (119, 2, 'admin', '用户管理', 1, '\'批量导入用户\'', 'com.elderly.controller.AdminUserController.importUsers', '/api/admin/user/import', '[]', '{\"code\":200,\"data\":\"导入操作完成。成功插入：6 条。\",\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 21:16:19');
INSERT INTO `admin_log` VALUES (120, 2, 'admin', '用户管理', 3, '批量删除用户 ID: 124,123,128,127,126,125', 'com.elderly.controller.AdminUserController.batchDelete', '/api/admin/user/batch', '[[124,123,128,127,126,125]]', '{\"code\":200,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 21:16:27');
INSERT INTO `admin_log` VALUES (121, 2, 'admin', '用户管理', 1, '\'批量导入用户\'', 'com.elderly.controller.AdminUserController.importUsers', '/api/admin/user/import', '[]', '{\"code\":500,\"message\":\"导入中止，发现数据格式错误（未执行任何写入）：\\n第2行：手机号缺失; \"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 21:16:37');
INSERT INTO `admin_log` VALUES (122, 2, 'admin', '用户管理', 1, '\'批量导入用户\'', 'com.elderly.controller.AdminUserController.importUsers', '/api/admin/user/import', '[]', '{\"code\":200,\"data\":\"导入操作完成。成功插入：3 条。\",\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 21:17:03');
INSERT INTO `admin_log` VALUES (123, 2, 'admin', '用户管理', 3, '批量删除用户 ID: 130,129,131', 'com.elderly.controller.AdminUserController.batchDelete', '/api/admin/user/batch', '[[130,129,131]]', '{\"code\":200,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 21:17:11');
INSERT INTO `admin_log` VALUES (124, 2, 'admin', '用户管理', 1, '\'批量导入用户\'', 'com.elderly.controller.AdminUserController.importUsers', '/api/admin/user/import', '[]', '{\"code\":500,\"message\":\"导入中止，发现数据格式错误（未执行任何写入）：\\n第2行：用户类型缺失; \\n第3行：用户类型缺失; \\n第4行：用户类型缺失; \"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 21:17:41');
INSERT INTO `admin_log` VALUES (125, 2, 'admin', '用户管理', 1, '\'批量导入用户\'', 'com.elderly.controller.AdminUserController.importUsers', '/api/admin/user/import', '[]', '{\"code\":500,\"message\":\"导入中止，发现数据格式错误（未执行任何写入）：\\n第8行：服务人员类型缺失; \"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 21:19:58');
INSERT INTO `admin_log` VALUES (126, 2, 'admin', '用户管理', 1, '\'批量导入用户\'', 'com.elderly.controller.AdminUserController.importUsers', '/api/admin/user/import', '[]', '{\"code\":200,\"data\":\"导入操作完成。成功插入：6 条。\\n已自动跳过已存在的账号（1条）：杨梓帆(13622598933)\",\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 21:20:12');
INSERT INTO `admin_log` VALUES (127, 2, 'admin', '用户管理', 3, '批量删除用户 ID: 133,132,137,135,136,134', 'com.elderly.controller.AdminUserController.batchDelete', '/api/admin/user/batch', '[[133,132,137,135,136,134]]', '{\"code\":200,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 21:20:22');
INSERT INTO `admin_log` VALUES (128, 2, 'admin', '用户管理', 1, '\'批量导入用户\'', 'com.elderly.controller.AdminUserController.importUsers', '/api/admin/user/import', '[]', '{\"code\":500,\"message\":\"导入中止，发现数据格式错误（未执行任何写入）：\\n第2行：老人/子女必须填写地址; \\n第3行：老人/子女必须填写地址; \\n第4行：老人/子女必须填写地址; \"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 21:24:04');
INSERT INTO `admin_log` VALUES (129, 2, 'admin', '用户管理', 1, '新增用户: 杨梓帆 (13829493229)', 'com.elderly.controller.UserController.addUser', '/api/user', '[{\"phone\":\"13829493229\",\"name\":\"杨梓帆\",\"gender\":1,\"idCard\":\"440513200402201456\",\"relatedIdCard\":\"\",\"userType\":3,\"workerType\":3,\"address\":\"\"}]', '{\"code\":200,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 21:40:49');
INSERT INTO `admin_log` VALUES (130, 2, 'admin', '用户管理', 3, '删除用户 ID: 138', 'com.elderly.controller.UserController.deleteUser', '/api/user/138', '[138]', '{\"code\":200,\"message\":\"success\"}', '0:0:0:0:0:0:0:1', 1, NULL, '2026-01-31 21:41:00');
INSERT INTO `admin_log` VALUES (131, 2, 'admin', '用户管理', 1, '新增用户: 杨梓帆 (13829493229)', 'com.elderly.controller.UserController.addUser', '/api/user', '[{\"phone\":\"13829493229\",\"name\":\"杨梓帆\",\"gender\":1,\"idCard\":\"440513200402201456\",\"relatedIdCard\":\"\",\"userType\":2,\"address\":\"1号楼 201\"}]', NULL, '0:0:0:0:0:0:0:1', 0, '该身份证号已注册', '2026-01-31 22:00:22');
INSERT INTO `admin_log` VALUES (132, 2, 'admin', '系统登录', 5, '超级管理员登录系统', NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-02-02 16:26:17');

-- ----------------------------
-- Table structure for building
-- ----------------------------
DROP TABLE IF EXISTS `building`;
CREATE TABLE `building`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '楼栋名称',
  `code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '楼栋编号',
  `floors` int NULL DEFAULT NULL COMMENT '楼层数',
  `units` int NULL DEFAULT NULL COMMENT '单元数',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-停用 1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '楼栋管理表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of building
-- ----------------------------
INSERT INTO `building` VALUES (16, '1号楼', '1号楼', NULL, NULL, '', 1, '2026-01-30 15:19:55', '2026-01-30 15:19:55', 0);
INSERT INTO `building` VALUES (17, '2号楼', '2号楼', NULL, NULL, '', 1, '2026-01-30 15:20:11', '2026-01-30 15:20:11', 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '楼栋配送员关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of building_worker
-- ----------------------------
INSERT INTO `building_worker` VALUES (16, '1号楼', 33, 1, 1, '2026-01-30 15:20:28', '2026-01-30 15:20:28');
INSERT INTO `building_worker` VALUES (17, '1号楼', 34, 0, 1, '2026-01-30 15:20:31', '2026-01-30 15:20:31');
INSERT INTO `building_worker` VALUES (18, '2号楼', 35, 1, 1, '2026-01-30 15:20:34', '2026-01-30 15:20:34');
INSERT INTO `building_worker` VALUES (19, '2号楼', 36, 0, 1, '2026-01-30 15:20:39', '2026-01-30 15:20:39');

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
  `is_late` tinyint NULL DEFAULT 0 COMMENT '是否迟到: 0-否 1-是',
  `late_minutes` int NULL DEFAULT 0 COMMENT '迟到分钟数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_elderly_id`(`elderly_id` ASC) USING BTREE,
  INDEX `idx_worker_id`(`worker_id` ASC) USING BTREE,
  INDEX `idx_service_date`(`service_date` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '保洁预约订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cleaning_order
-- ----------------------------

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
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '保洁服务项目表' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 47 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '保洁员排班表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cleaning_worker_schedule
-- ----------------------------
INSERT INTO `cleaning_worker_schedule` VALUES (42, 49, '2026-01-26', '08:00:00', '12:00:00', 1, '2026-01-25 20:35:13');
INSERT INTO `cleaning_worker_schedule` VALUES (43, 47, '2026-01-26', '14:00:00', '18:00:00', 1, '2026-01-25 20:35:31');
INSERT INTO `cleaning_worker_schedule` VALUES (44, 47, '2026-01-29', '08:00:00', '18:00:00', 1, '2026-01-28 20:26:27');
INSERT INTO `cleaning_worker_schedule` VALUES (45, 47, '2026-01-31', '08:00:00', '18:00:00', 1, '2026-01-30 19:44:14');
INSERT INTO `cleaning_worker_schedule` VALUES (46, 44, '2026-01-31', '08:00:00', '18:00:00', 1, '2026-01-30 23:00:02');

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '保洁员服务项目关联表' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应急呼叫表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of emergency_call
-- ----------------------------
INSERT INTO `emergency_call` VALUES (1, 1, '张大爷', '13800138001', '幸福社区1栋101室', 1, '2026-01-10 17:03:40', NULL, '2026-01-30 22:55:27', 0, 1, NULL, '2026-01-10 17:03:40');
INSERT INTO `emergency_call` VALUES (2, 1, '张大爷', '13800138001', '幸福社区1栋101室', 3, '2026-01-10 17:03:53', NULL, '2026-01-30 20:32:47', 0, 1, NULL, '2026-01-10 17:03:53');
INSERT INTO `emergency_call` VALUES (3, 1, '张大爷', '13800138001', '幸福社区1栋101室', 1, '2026-01-10 17:39:19', NULL, '2026-01-30 20:22:59', 0, 1, NULL, '2026-01-10 17:39:19');
INSERT INTO `emergency_call` VALUES (4, 1, '张大爷', '13800138001', '幸福社区1栋101室', 2, '2026-01-10 17:50:40', 17, '2026-01-16 10:26:38', 0, 1, NULL, '2026-01-10 17:50:40');
INSERT INTO `emergency_call` VALUES (5, 28, '李四', '13800001000', '1号楼 101', 2, '2026-01-30 23:06:46', NULL, NULL, 0, 0, NULL, '2026-01-30 23:06:46');

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
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '家庭表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of family
-- ----------------------------
INSERT INTO `family` VALUES (1, '北京市朝阳区幸福小区1号楼101', '幸福小区', '1号楼', '1单元', '101', '2026-01-14 22:31:02', 0);
INSERT INTO `family` VALUES (2, '北京市朝阳区幸福小区1号楼102', '幸福小区', '1号楼', '1单元', '102', '2026-01-14 22:31:02', 0);
INSERT INTO `family` VALUES (3, '北京市朝阳区幸福小区2号楼201', '幸福小区', '2号楼', '2单元', '201', '2026-01-14 22:31:02', 0);
INSERT INTO `family` VALUES (4, '2号楼 501', NULL, '2号楼', NULL, NULL, '2026-01-31 19:30:13', 0);
INSERT INTO `family` VALUES (5, '2号楼 501', NULL, '2号楼', NULL, NULL, '2026-01-31 19:30:13', 0);
INSERT INTO `family` VALUES (6, '2号楼2号楼502', NULL, '2号楼2号楼502', NULL, NULL, '2026-01-31 19:30:13', 0);
INSERT INTO `family` VALUES (7, '1号楼1号楼501', NULL, '1号楼1号楼501', NULL, NULL, '2026-01-31 19:45:29', 0);
INSERT INTO `family` VALUES (8, '1号楼 501', NULL, '1号楼', NULL, NULL, '2026-01-31 20:32:04', 0);
INSERT INTO `family` VALUES (9, '2号楼 501', NULL, '2号楼', NULL, NULL, '2026-01-31 20:32:04', 0);
INSERT INTO `family` VALUES (10, '2号楼 502', NULL, '2号楼', NULL, NULL, '2026-01-31 20:32:04', 0);
INSERT INTO `family` VALUES (11, '1号楼 501', NULL, '1号楼', NULL, NULL, '2026-01-31 20:51:56', 0);
INSERT INTO `family` VALUES (12, '2号楼 501', NULL, '2号楼', NULL, NULL, '2026-01-31 20:51:56', 0);
INSERT INTO `family` VALUES (13, '2号楼 502', NULL, '2号楼', NULL, NULL, '2026-01-31 20:51:56', 0);
INSERT INTO `family` VALUES (14, '1号楼 501', NULL, '1号楼', NULL, NULL, '2026-01-31 20:52:39', 0);
INSERT INTO `family` VALUES (15, '2号楼 501', NULL, '2号楼', NULL, NULL, '2026-01-31 20:52:39', 0);
INSERT INTO `family` VALUES (16, '2号楼 502', NULL, '2号楼', NULL, NULL, '2026-01-31 20:52:39', 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '健康档案表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of health_record
-- ----------------------------
INSERT INTO `health_record` VALUES (13, 12, NULL, '头晕、血压偏高', '青霉素过敏', '降压药（每日一次）、阿司匹林', '需定期监测血压', '2025-12-15 22:35:01');
INSERT INTO `health_record` VALUES (14, 12, NULL, '膝关节疼痛', '青霉素过敏', '降压药、止痛贴', '建议减少爬楼', '2025-12-30 22:35:01');
INSERT INTO `health_record` VALUES (15, 12, NULL, '感冒咳嗽', '青霉素过敏', '降压药、感冒灵', '多喝水多休息', '2026-01-09 22:35:01');
INSERT INTO `health_record` VALUES (16, 13, NULL, '失眠、心悸', '无', '安神补脑液', '建议睡前少喝茶', '2025-12-25 22:35:01');
INSERT INTO `health_record` VALUES (17, 13, NULL, '腰酸背痛', '无', '安神补脑液、膏药', '适当活动', '2026-01-07 22:35:01');
INSERT INTO `health_record` VALUES (18, 14, NULL, '糖尿病复查', '磺胺类药物', '二甲双胍、胰岛素', '控制饮食，定期测血糖', '2026-01-04 22:35:01');
INSERT INTO `health_record` VALUES (19, 28, 14, '柘城本', NULL, NULL, NULL, '2026-01-30 23:04:02');

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
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '每日菜品表' ROW_FORMAT = DYNAMIC;

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
INSERT INTO `meal_daily_dish` VALUES (22, 1, '2026-01-19', 2, '馒头', '松软可口的馒头', NULL, 3, '碳水化合物为主', 0, 1, '2026-01-22 18:31:58', 0);
INSERT INTO `meal_daily_dish` VALUES (23, 1, '2026-01-26', 1, '小米粥', '养胃小米粥', NULL, 5, '易消化，适合老年人', 0, 1, '2026-01-25 21:20:02', 0);
INSERT INTO `meal_daily_dish` VALUES (24, 1, '2026-01-26', 1, '馒头', '松软可口的馒头', NULL, 3, '碳水化合物为主', 0, 1, '2026-01-25 21:20:02', 0);
INSERT INTO `meal_daily_dish` VALUES (25, 1, '2026-01-26', 1, '蒸蛋糕', '松软蒸蛋糕', NULL, 6, '易消化', 0, 1, '2026-01-25 21:20:02', 0);
INSERT INTO `meal_daily_dish` VALUES (26, 1, '2026-01-26', 1, '豆沙包', '香甜豆沙包', NULL, 5, '适量糖分', 0, 1, '2026-01-25 21:20:02', 0);
INSERT INTO `meal_daily_dish` VALUES (27, 1, '2026-01-19', 1, '小米粥', '养胃小米粥', NULL, 5, '易消化，适合老年人', 0, 1, '2026-01-30 22:56:54', 0);
INSERT INTO `meal_daily_dish` VALUES (28, 1, '2026-01-19', 1, '馒头', '松软可口的馒头', NULL, 3, '碳水化合物为主', 0, 1, '2026-01-30 22:56:54', 0);
INSERT INTO `meal_daily_dish` VALUES (29, 1, '2026-01-19', 1, '蒸蛋糕', '松软蒸蛋糕', NULL, 6, '易消化', 0, 1, '2026-01-30 22:56:54', 0);
INSERT INTO `meal_daily_dish` VALUES (30, 1, '2026-01-19', 1, '豆沙包', '香甜豆沙包', NULL, 5, '适量糖分', 0, 1, '2026-01-30 22:56:54', 0);
INSERT INTO `meal_daily_dish` VALUES (31, 1, '2026-02-02', 1, '小米粥', '养胃小米粥', NULL, 5, '易消化，适合老年人', 0, 1, '2026-01-30 22:57:05', 0);
INSERT INTO `meal_daily_dish` VALUES (32, 1, '2026-02-02', 1, '馒头', '松软可口的馒头', NULL, 3, '碳水化合物为主', 0, 1, '2026-01-30 22:57:06', 0);
INSERT INTO `meal_daily_dish` VALUES (33, 1, '2026-02-02', 1, '蒸蛋糕', '松软蒸蛋糕', NULL, 6, '易消化', 0, 1, '2026-01-30 22:57:06', 0);
INSERT INTO `meal_daily_dish` VALUES (34, 1, '2026-02-02', 1, '豆沙包', '香甜豆沙包', NULL, 5, '适量糖分', 0, 1, '2026-01-30 22:57:06', 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '餐饮配送时间配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of meal_delivery_config
-- ----------------------------
INSERT INTO `meal_delivery_config` VALUES (1, 1, '07:00:00', '08:30:00', 60, 1, '2026-01-11 20:15:25');
INSERT INTO `meal_delivery_config` VALUES (2, 2, '11:00:00', '12:30:00', 60, 1, '2026-01-11 20:15:25');
INSERT INTO `meal_delivery_config` VALUES (3, 3, '17:00:00', '18:30:00', 60, 1, '2026-01-11 20:15:25');

-- ----------------------------
-- Table structure for meal_dish_template
-- ----------------------------
DROP TABLE IF EXISTS `meal_dish_template`;
CREATE TABLE `meal_dish_template`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dish_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜品名称',
  `category` tinyint NULL DEFAULT 1 COMMENT '菜品分类: 1-主食 2-荤菜 3-素菜 4-汤品 5-点心',
  `meal_types` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '1,2,3' COMMENT '适用餐次，多个用逗号分隔',
  `price` int NULL DEFAULT 10 COMMENT '默认价格(积分)',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜品图片',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `nutrition_info` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '营养信息',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜品模板/菜单库' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of meal_dish_template
-- ----------------------------
INSERT INTO `meal_dish_template` VALUES (1, '免费白米饭', 1, '2,3', 5, NULL, '软糯香甜的白米饭', '碳水化合物为主', 1, '2026-01-22 18:30:14', '2026-01-22 18:30:14', 0);
INSERT INTO `meal_dish_template` VALUES (2, '馒头', 1, '1,2,3', 3, NULL, '松软可口的馒头', '碳水化合物为主', 1, '2026-01-22 18:30:14', '2026-01-22 18:30:14', 0);
INSERT INTO `meal_dish_template` VALUES (3, '小米粥', 1, '1', 5, NULL, '养胃小米粥', '易消化，适合老年人', 1, '2026-01-22 18:30:14', '2026-01-22 18:30:14', 0);
INSERT INTO `meal_dish_template` VALUES (4, '红烧肉', 2, '2,3', 15, NULL, '肥而不腻的红烧肉', '蛋白质丰富', 1, '2026-01-22 18:30:14', '2026-01-22 18:30:14', 0);
INSERT INTO `meal_dish_template` VALUES (5, '清蒸鱼', 2, '2,3', 18, NULL, '新鲜清蒸鱼', '高蛋白低脂肪', 1, '2026-01-22 18:30:14', '2026-01-22 18:30:14', 0);
INSERT INTO `meal_dish_template` VALUES (6, '番茄炒蛋', 2, '2,3', 10, NULL, '家常番茄炒蛋', '维生素C丰富', 1, '2026-01-22 18:30:14', '2026-01-22 18:30:14', 0);
INSERT INTO `meal_dish_template` VALUES (7, '清炒时蔬', 3, '2,3', 8, NULL, '当季新鲜蔬菜', '膳食纤维丰富', 1, '2026-01-22 18:30:14', '2026-01-22 18:30:14', 0);
INSERT INTO `meal_dish_template` VALUES (8, '蒜蓉西兰花', 3, '2,3', 10, NULL, '营养丰富的西兰花', '维生素丰富', 1, '2026-01-22 18:30:14', '2026-01-22 18:30:14', 0);
INSERT INTO `meal_dish_template` VALUES (9, '紫菜蛋花汤', 4, '2,3', 5, NULL, '清淡可口的紫菜汤', '补碘', 1, '2026-01-22 18:30:14', '2026-01-22 18:30:14', 0);
INSERT INTO `meal_dish_template` VALUES (10, '排骨汤', 4, '2,3', 12, NULL, '营养滋补排骨汤', '补钙', 1, '2026-01-22 18:30:14', '2026-01-22 18:30:14', 0);
INSERT INTO `meal_dish_template` VALUES (11, '豆沙包', 5, '1', 5, NULL, '香甜豆沙包', '适量糖分', 1, '2026-01-22 18:30:14', '2026-01-22 18:30:14', 0);
INSERT INTO `meal_dish_template` VALUES (12, '蒸蛋糕', 5, '1', 6, NULL, '松软蒸蛋糕', '易消化', 1, '2026-01-22 18:30:14', '2026-01-22 18:30:14', 0);

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
  `is_late` tinyint NULL DEFAULT 0 COMMENT '是否迟到: 0-否 1-是',
  `late_minutes` int NULL DEFAULT 0 COMMENT '迟到分钟数',
  `actual_delivery_time` datetime NULL DEFAULT NULL COMMENT '实际送达时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_elderly_id`(`elderly_id` ASC) USING BTREE,
  INDEX `idx_dish_date`(`dish_date` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '餐饮预约订单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of meal_order
-- ----------------------------
INSERT INTO `meal_order` VALUES (1, 'M20260116144907001', 1, 1, 1, '2026-01-16', 1, 1, 8, '阳光小区1栋101室', 23, '621169', 0, NULL, 0, 0, NULL, 0, 0, NULL, '2026-01-16 14:49:07', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (2, 'M20260116144907002', 2, 2, 3, '2026-01-16', 2, 1, 15, '阳光小区2栋202室', 23, '496684', 0, NULL, 0, 0, NULL, 0, 0, NULL, '2026-01-16 14:49:07', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (3, 'M20260116144907003', 3, 3, 4, '2026-01-16', 2, 2, 36, '幸福花园3栋303室', 23, '619912', 1, NULL, 0, 0, NULL, 0, 0, NULL, '2026-01-16 14:49:07', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (4, 'M20260116144907004', 1, 1, 5, '2026-01-16', 3, 1, 10, '阳光小区1栋101室', 23, '609510', 0, NULL, 0, 0, NULL, 0, 0, NULL, '2026-01-16 14:49:07', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (5, 'M20260116144907005', 4, 4, 6, '2026-01-17', 1, 1, 5, '和谐家园4栋404室', 23, '187815', 0, NULL, 0, 0, NULL, 0, 0, NULL, '2026-01-16 14:49:07', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (6, 'M20260116144907006', 5, 5, 7, '2026-01-17', 2, 1, 16, '温馨苑5栋505室', 23, '110547', 0, NULL, 0, 0, NULL, 0, 0, NULL, '2026-01-16 14:49:07', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (7, 'M20260116144907007', 2, 2, 8, '2026-01-17', 2, 1, 14, '阳光小区2栋202室', 23, '989290', 1, NULL, 0, 0, NULL, 0, 0, NULL, '2026-01-16 14:49:07', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (8, 'M20260116144907008', 3, 3, 9, '2026-01-17', 3, 1, 8, '幸福花园3栋303室', 23, '614809', 2, NULL, 0, 0, NULL, 0, 0, NULL, '2026-01-16 14:49:07', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (9, 'M20260116144907009', 1, 1, 10, '2026-01-18', 2, 1, 13, '阳光小区1栋101室', 23, '106177', 0, NULL, 0, 0, NULL, 0, 0, NULL, '2026-01-16 14:49:07', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (10, 'M20260116144907010', 4, 4, 3, '2026-01-16', 2, 2, 30, '和谐家园4栋404室', 23, '686457', 0, NULL, 0, 0, NULL, 0, 0, NULL, '2026-01-16 14:49:07', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (11, 'M20260116145047001', 1, 1, 1, '2026-01-16', 1, 1, 8, '阳光小区1栋101室', 23, '113753', 0, NULL, 0, 0, NULL, 0, 0, NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (12, 'M20260116145047002', 2, 2, 3, '2026-01-16', 2, 1, 15, '阳光小区2栋202室', 23, '509397', 0, NULL, 0, 0, NULL, 0, 0, NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (13, 'M20260116145047003', 3, 3, 4, '2026-01-16', 2, 2, 36, '幸福花园3栋303室', 23, '205726', 1, NULL, 0, 0, NULL, 0, 0, NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (14, 'M20260116145047004', 1, 1, 5, '2026-01-16', 3, 1, 10, '阳光小区1栋101室', 23, '500438', 0, NULL, 0, 0, NULL, 0, 0, NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (15, 'M20260116145047005', 4, 4, 6, '2026-01-17', 1, 1, 5, '和谐家园4栋404室', 23, '885014', 0, NULL, 0, 0, NULL, 0, 0, NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (16, 'M20260116145047006', 5, 5, 7, '2026-01-17', 2, 1, 16, '温馨苑5栋505室', 23, '923754', 0, NULL, 0, 0, NULL, 0, 0, NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (17, 'M20260116145047007', 2, 2, 8, '2026-01-17', 2, 1, 14, '阳光小区2栋202室', 23, '963732', 1, NULL, 0, 0, NULL, 0, 0, NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (18, 'M20260116145047008', 3, 3, 9, '2026-01-17', 3, 1, 8, '幸福花园3栋303室', 23, '047396', 2, NULL, 0, 0, NULL, 0, 0, NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (19, 'M20260116145047009', 1, 1, 10, '2026-01-18', 2, 1, 13, '阳光小区1栋101室', 23, '345787', 0, NULL, 0, 0, NULL, 0, 0, NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (20, 'M20260116145047010', 4, 4, 3, '2026-01-16', 2, 2, 30, '和谐家园4栋404室', 23, '586747', 0, NULL, 0, 0, NULL, 0, 0, NULL, '2026-01-16 14:50:47', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (21, '2012057580626001920', 12, 12, 6, '2026-01-16', 3, 1, 10, NULL, 23, '896374', 3, '2026-01-16 15:32:41', 0, 10, NULL, 0, 0, NULL, '2026-01-16 15:01:24', '2026-01-16 15:16:05', 0);
INSERT INTO `meal_order` VALUES (22, '2012063056122171392', 12, 12, 16, '2026-01-16', 3, 1, 10, NULL, 23, NULL, 3, '2026-01-16 15:32:30', 0, 10, NULL, 0, 0, NULL, '2026-01-16 15:23:10', '2026-01-16 15:23:10', 0);
INSERT INTO `meal_order` VALUES (23, '2012065596347838464', 12, 12, 16, '2026-01-16', 3, 1, 10, NULL, 23, '266217', 2, NULL, 0, 0, NULL, 0, 0, NULL, '2026-01-16 15:33:15', '2026-01-16 15:37:23', 0);
INSERT INTO `meal_order` VALUES (24, '2015414516980383744', 28, 28, 23, '2026-01-26', 1, 4, 19, '1号楼 101', 35, '619071', 3, NULL, 0, 0, '菜品: 小米粥 x1, 蒸蛋糕 x1, 馒头 x1, 豆沙包 x1', 0, 0, NULL, '2026-01-25 21:20:40', '2026-01-25 21:26:39', 0);
INSERT INTO `meal_order` VALUES (25, '2015414670366081024', 30, 30, 23, '2026-01-26', 1, 4, 19, '2号楼 101', 42, '214734', 2, NULL, 0, 0, '菜品: 小米粥 x1, 蒸蛋糕 x1, 豆沙包 x1, 馒头 x1', 0, 0, NULL, '2026-01-25 21:21:17', '2026-01-25 21:30:19', 0);
INSERT INTO `meal_order` VALUES (26, '2015414837509095424', 63, 63, 23, '2026-01-26', 1, 3, 14, '1号楼 102', NULL, '233904', 0, NULL, 0, 0, '菜品: 小米粥 x1, 馒头 x1, 蒸蛋糕 x1', 0, 0, NULL, '2026-01-25 21:21:57', '2026-01-25 21:21:57', 0);
INSERT INTO `meal_order` VALUES (27, '2015414948385521664', 64, 64, 23, '2026-01-26', 1, 3, 14, '2号楼 102', NULL, '193525', 0, NULL, 0, 0, '菜品: 小米粥 x1, 馒头 x1, 蒸蛋糕 x1', 0, 0, NULL, '2026-01-25 21:22:23', '2026-01-25 21:22:23', 0);
INSERT INTO `meal_order` VALUES (28, '2015415092405338112', 65, 65, 23, '2026-01-26', 1, 4, 20, '1号楼 103', 33, '688747', 2, NULL, 0, 0, '菜品: 小米粥 x1, 馒头 x1, 蒸蛋糕 x1, 蒸蛋糕 x1', 0, 0, NULL, '2026-01-25 21:22:57', '2026-01-25 21:28:52', 0);
INSERT INTO `meal_order` VALUES (29, '2015415275130191872', 66, 66, 23, '2026-01-26', 1, 2, 8, '2号楼 103', NULL, '835844', 0, NULL, 0, 0, '菜品: 小米粥 x1, 馒头 x1', 0, 0, NULL, '2026-01-25 21:23:41', '2026-01-25 21:23:41', 0);
INSERT INTO `meal_order` VALUES (30, '2015417554147213312', 28, 29, 24, '2026-01-26', 1, 2, 9, '1号楼 101', 33, '519388', 3, NULL, 0, 0, '菜品: 馒头 x1, 蒸蛋糕 x1', 0, 0, NULL, '2026-01-25 21:32:44', '2026-01-25 21:34:30', 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '周食谱表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of meal_weekly_menu
-- ----------------------------
INSERT INTO `meal_weekly_menu` VALUES (1, '2026-01-19', '2026-01-25', 1, '2026-01-16 09:34:28', '2026-01-16 09:34:02', '2026-01-16 09:34:28', 0);
INSERT INTO `meal_weekly_menu` VALUES (2, '2026-03-02', '2026-02-08', 0, NULL, '2026-01-30 22:56:45', '2026-01-30 22:56:45', 0);

-- ----------------------------
-- Table structure for meal_worker_schedule
-- ----------------------------
DROP TABLE IF EXISTS `meal_worker_schedule`;
CREATE TABLE `meal_worker_schedule`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `worker_id` bigint NOT NULL COMMENT '配送员ID',
  `schedule_date` date NOT NULL COMMENT '值班日期',
  `meal_type` tinyint NULL DEFAULT NULL COMMENT '餐次: 1-早餐 2-午餐 3-晚餐, 为空表示全天',
  `buildings` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '负责楼栋(逗号分隔)，为空表示全部楼栋',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-停用 1-启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_schedule_date`(`schedule_date` ASC) USING BTREE,
  INDEX `idx_worker_id`(`worker_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '送餐配送员值班安排表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of meal_worker_schedule
-- ----------------------------
INSERT INTO `meal_worker_schedule` VALUES (1, 42, '2026-01-26', 1, '2号楼', 1, '', '2026-01-25 20:36:15', '2026-01-25 20:36:15', 0);
INSERT INTO `meal_worker_schedule` VALUES (2, 41, '2026-01-26', 1, '2号楼', 1, '', '2026-01-25 20:37:48', '2026-01-25 20:37:48', 0);
INSERT INTO `meal_worker_schedule` VALUES (3, 35, '2026-01-26', 1, '1号楼', 1, '', '2026-01-25 20:41:16', '2026-01-25 20:41:16', 0);
INSERT INTO `meal_worker_schedule` VALUES (4, 37, '2026-01-26', 1, '1号楼', 1, '', '2026-01-25 21:15:44', '2026-01-25 21:15:47', 1);
INSERT INTO `meal_worker_schedule` VALUES (5, 33, '2026-01-26', 1, '1号楼', 1, '', '2026-01-25 21:16:03', '2026-01-25 21:16:03', 0);
INSERT INTO `meal_worker_schedule` VALUES (6, 41, '2026-01-29', 1, '2号楼', 1, '', '2026-01-28 16:05:47', '2026-01-28 16:05:47', 0);

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
  `is_late` tinyint NULL DEFAULT 0 COMMENT '是否迟到: 0-否 1-是',
  `late_minutes` int NULL DEFAULT 0 COMMENT '迟到分钟数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_elderly_id`(`elderly_id` ASC) USING BTREE,
  INDEX `idx_doctor_id`(`doctor_id` ASC) USING BTREE,
  INDEX `idx_appointment_date`(`appointment_date` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医疗巡诊预约表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of medical_appointment
-- ----------------------------
INSERT INTO `medical_appointment` VALUES (14, '2017252470933790720', 28, 28, 14, '2026-01-30', 2, 1, '柘城本', '1号楼 101', 3, NULL, '2026-01-30 23:05:05', '', '', '895724', '无', 0, 0, '2026-01-30 23:04:02', '2026-01-30 23:05:29', 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生熔断状态表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of medical_circuit_breaker
-- ----------------------------
INSERT INTO `medical_circuit_breaker` VALUES (1, 14, 1, 0, '2026-01-30 23:04:02');

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
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of medical_doctor
-- ----------------------------
INSERT INTO `medical_doctor` VALUES (4, 23, '李医生', '主治医师', '全科', '从医20年，擅长老年常见病诊治', NULL, 20, 1, '2026-01-16 14:50:47', 1);
INSERT INTO `medical_doctor` VALUES (5, 61, '郑涛', '主任医师', '心血管内科疾病诊治', '从事心血管内科临床工作25年，擅长冠心病、高血压、心律失常等疾病的精准诊疗，曾主持多项省级科研项目，发表学术论文30余篇，临床经验丰富。', NULL, 10, 1, '2026-01-23 22:40:39', 0);
INSERT INTO `medical_doctor` VALUES (6, 60, '吴敏', '副主任医师', '儿科常见及疑难病症诊疗', '深耕儿科领域20年，对小儿呼吸、消化及神经系统疾病有深入研究，擅长儿童哮喘、腹泻、癫痫的规范化治疗，亲和力强，深受患儿及家长信赖。', NULL, 10, 1, '2026-01-23 22:42:08', 0);
INSERT INTO `medical_doctor` VALUES (7, 59, '周明', '主治医师', '骨科创伤修复与关节外科', '从事骨科临床工作15年，熟练掌握骨折复位固定、关节置换等手术技能，擅长四肢创伤、骨关节炎的诊治，注重术后康复指导，提高患者愈后质量。', NULL, 5, 1, '2026-01-23 22:42:36', 0);
INSERT INTO `medical_doctor` VALUES (8, 58, '孙丽', '主治医师', '妇产科生殖健康与孕期保健', '拥有13年妇产科临床经验，擅长孕前检查、孕期并发症防治、妇科炎症诊治，倡导自然分娩，为女性提供全周期生殖健康服务。', NULL, 10, 1, '2026-01-23 22:43:10', 0);
INSERT INTO `medical_doctor` VALUES (9, 57, '赵刚', '住院医师', '神经内科基础疾病诊治', '毕业于知名医学院校，从事神经内科工作8年，擅长头痛、头晕、脑梗死等基础疾病的诊断与治疗，积极参与科研项目，业务能力扎实。', NULL, 10, 1, '2026-01-23 22:43:49', 0);
INSERT INTO `medical_doctor` VALUES (10, 56, '刘芳', '住院医师', '消化内科疾病诊治', '临床工作7年，专注于胃炎、胃溃疡、肠炎等消化内科常见疾病的诊治，熟练掌握胃肠镜检查操作，对疑难病例有较强的分析能力。', NULL, 10, 1, '2026-01-23 22:44:16', 0);
INSERT INTO `medical_doctor` VALUES (11, 55, '王浩', '副主任医师', '眼科白内障与青光眼诊疗', '眼科临床工作16年，擅长白内障超声乳化联合人工晶体植入术、青光眼小梁切除术，对眼底疾病的早期筛查与干预有丰富经验。', NULL, 10, 1, '2026-01-23 22:44:44', 0);
INSERT INTO `medical_doctor` VALUES (12, 53, '陈明', '主治医师', '泌尿外科结石与前列腺疾病', '临床工作10年，擅长肾结石、输尿管结石的微创手术治疗，以及前列腺炎、前列腺增生的规范化诊疗，注重患者隐私保护与术后护理。', NULL, 10, 1, '2026-01-23 22:45:11', 0);
INSERT INTO `medical_doctor` VALUES (13, 32, '张宇辰', '住院医师', '急诊科急救与常见病处理', '从事急诊科工作6年，熟练掌握心肺复苏、创伤急救等技能，能快速处理发热、腹痛、外伤等常见急症，应变能力强，责任心强。', NULL, 10, 1, '2026-01-23 22:45:46', 0);
INSERT INTO `medical_doctor` VALUES (14, 70, '杨梓帆', '主治医师', '外科', '已给窝里giaogiao', NULL, 10, 1, '2026-01-30 23:01:50', 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生值班表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of medical_doctor_duty
-- ----------------------------
INSERT INTO `medical_doctor_duty` VALUES (6, 7, '2026-01-25', 2, '20:00:00', '08:00:00', 1, 1, '2026-01-25 20:31:39');
INSERT INTO `medical_doctor_duty` VALUES (7, 13, '2026-01-26', 1, '08:00:00', '20:00:00', 0, 1, '2026-01-25 20:32:14');
INSERT INTO `medical_doctor_duty` VALUES (8, 9, '2026-01-25', 2, '20:00:00', '08:00:00', 1, 1, '2026-01-25 20:32:31');
INSERT INTO `medical_doctor_duty` VALUES (9, 14, '2026-01-30', 2, '20:00:00', '08:00:00', 1, 1, '2026-01-30 23:02:10');

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
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '积分流水表' ROW_FORMAT = DYNAMIC;

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
INSERT INTO `points_transaction` VALUES (9, 12, 1, 100, 120, NULL, NULL, '管理员充值', '2026-01-22 20:24:44');
INSERT INTO `points_transaction` VALUES (10, 12, 2, 30, 90, 'cleaning', NULL, '保洁预约-卧室整理', '2026-01-22 20:24:49');
INSERT INTO `points_transaction` VALUES (11, 66, 1, 100, 100, NULL, NULL, '管理员充值', '2026-01-25 20:30:26');
INSERT INTO `points_transaction` VALUES (12, 65, 1, 100, 100, NULL, NULL, '管理员充值', '2026-01-25 20:30:29');
INSERT INTO `points_transaction` VALUES (13, 64, 1, 100, 100, NULL, NULL, '管理员充值', '2026-01-25 20:30:32');
INSERT INTO `points_transaction` VALUES (14, 63, 1, 100, 100, NULL, NULL, '管理员充值', '2026-01-25 20:30:35');
INSERT INTO `points_transaction` VALUES (15, 30, 1, 100, 100, NULL, NULL, '管理员充值', '2026-01-25 20:30:41');
INSERT INTO `points_transaction` VALUES (16, 28, 1, 100, 100, NULL, NULL, '管理员充值', '2026-01-25 20:30:44');
INSERT INTO `points_transaction` VALUES (17, 28, 2, 5, 95, 'meal', NULL, '餐饮预约-小米粥', '2026-01-25 21:20:40');
INSERT INTO `points_transaction` VALUES (18, 28, 2, 6, 89, 'meal', 24, '餐饮加点-蒸蛋糕', '2026-01-25 21:20:42');
INSERT INTO `points_transaction` VALUES (19, 28, 2, 3, 86, 'meal', 24, '餐饮加点-馒头', '2026-01-25 21:20:43');
INSERT INTO `points_transaction` VALUES (20, 28, 2, 5, 81, 'meal', 24, '餐饮加点-豆沙包', '2026-01-25 21:20:45');
INSERT INTO `points_transaction` VALUES (21, 30, 2, 5, 95, 'meal', NULL, '餐饮预约-小米粥', '2026-01-25 21:21:17');
INSERT INTO `points_transaction` VALUES (22, 30, 2, 6, 89, 'meal', 25, '餐饮加点-蒸蛋糕', '2026-01-25 21:21:18');
INSERT INTO `points_transaction` VALUES (23, 30, 2, 5, 84, 'meal', 25, '餐饮加点-豆沙包', '2026-01-25 21:21:20');
INSERT INTO `points_transaction` VALUES (24, 30, 2, 3, 81, 'meal', 25, '餐饮加点-馒头', '2026-01-25 21:21:21');
INSERT INTO `points_transaction` VALUES (25, 63, 2, 5, 95, 'meal', NULL, '餐饮预约-小米粥', '2026-01-25 21:21:57');
INSERT INTO `points_transaction` VALUES (26, 63, 2, 3, 92, 'meal', 26, '餐饮加点-馒头', '2026-01-25 21:21:58');
INSERT INTO `points_transaction` VALUES (27, 63, 2, 6, 86, 'meal', 26, '餐饮加点-蒸蛋糕', '2026-01-25 21:22:00');
INSERT INTO `points_transaction` VALUES (28, 64, 2, 5, 95, 'meal', NULL, '餐饮预约-小米粥', '2026-01-25 21:22:23');
INSERT INTO `points_transaction` VALUES (29, 64, 2, 3, 92, 'meal', 27, '餐饮加点-馒头', '2026-01-25 21:22:24');
INSERT INTO `points_transaction` VALUES (30, 64, 2, 6, 86, 'meal', 27, '餐饮加点-蒸蛋糕', '2026-01-25 21:22:25');
INSERT INTO `points_transaction` VALUES (31, 65, 2, 5, 95, 'meal', NULL, '餐饮预约-小米粥', '2026-01-25 21:22:57');
INSERT INTO `points_transaction` VALUES (32, 65, 2, 3, 92, 'meal', 28, '餐饮加点-馒头', '2026-01-25 21:22:58');
INSERT INTO `points_transaction` VALUES (33, 65, 2, 6, 86, 'meal', 28, '餐饮加点-蒸蛋糕', '2026-01-25 21:23:00');
INSERT INTO `points_transaction` VALUES (34, 65, 2, 6, 80, 'meal', 28, '餐饮加点-蒸蛋糕', '2026-01-25 21:23:01');
INSERT INTO `points_transaction` VALUES (35, 66, 2, 5, 95, 'meal', NULL, '餐饮预约-小米粥', '2026-01-25 21:23:41');
INSERT INTO `points_transaction` VALUES (36, 66, 2, 3, 92, 'meal', 29, '餐饮加点-馒头', '2026-01-25 21:23:42');
INSERT INTO `points_transaction` VALUES (37, 28, 2, 3, 78, 'meal', NULL, '餐饮预约-馒头', '2026-01-25 21:32:44');
INSERT INTO `points_transaction` VALUES (38, 28, 2, 6, 72, 'meal', 30, '餐饮加点-蒸蛋糕', '2026-01-25 21:32:46');
INSERT INTO `points_transaction` VALUES (39, 66, 1, 8, 100, NULL, NULL, '管理员充值', '2026-01-26 21:03:59');
INSERT INTO `points_transaction` VALUES (40, 66, 1, 50, 150, NULL, NULL, '管理员充值', '2026-01-30 20:33:13');

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
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务分类表' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务项目表' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 118 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '服务时间段配置表' ROW_FORMAT = DYNAMIC;

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
  `gender` int NULL DEFAULT NULL COMMENT '性别: 1-男 2-女',
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
  `related_id_card` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `admin_role` int NULL DEFAULT NULL COMMENT '管理员角色: 1-超级管理员 2-普通管理员 (仅userType=4时有效)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `openid`(`openid` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone_type`(`phone` ASC, `user_type` ASC) USING BTREE,
  INDEX `idx_openid`(`openid` ASC) USING BTREE,
  INDEX `idx_family_id`(`family_id` ASC) USING BTREE,
  INDEX `idx_user_id_card`(`id_card` ASC) USING BTREE,
  INDEX `idx_user_related_id_card`(`related_id_card` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 139 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (2, NULL, NULL, 'e10adc3949ba59abbe56e057f20f883e', 'admin', NULL, NULL, NULL, 4, NULL, NULL, NULL, 1, '2026-01-10 17:49:58', '2026-01-22 20:53:37', 0, NULL, 1);
INSERT INTO `sys_user` VALUES (28, NULL, '13800001000', '80afa0c0df103c81019c7b4a3dfca6c2', '李四', 1, '440203196507180069', NULL, 1, NULL, '1号楼 101', 4, 1, '2026-01-23 21:44:44', '2026-01-30 23:13:26', 0, '440203199908140123', NULL);
INSERT INTO `sys_user` VALUES (29, NULL, '13800002000', '8b97c05e0f151f88c651776d50a9f55a', '李泽轩', 1, '440203199908140123', NULL, 2, NULL, '1号楼 101', 4, 1, '2026-01-23 21:45:41', '2026-01-25 21:31:10', 0, '440203196507180069', NULL);
INSERT INTO `sys_user` VALUES (30, NULL, '13800003000', '872c153af8d90b51586fad1a523e8f48', '吴五', NULL, '440602196406240123', NULL, 1, NULL, '2号楼 101', 5, 1, '2026-01-23 21:46:48', '2026-01-23 21:46:48', 0, '440602200003120043', NULL);
INSERT INTO `sys_user` VALUES (31, NULL, '13800004000', 'f03380a767133ed7003fb172609e229d', '吴欣萌', NULL, '440602200003120043', NULL, 2, NULL, '2号楼 101', 5, 1, '2026-01-23 21:47:42', '2026-01-23 23:00:44', 0, '440602196406240123', NULL);
INSERT INTO `sys_user` VALUES (32, NULL, '13900001000', 'a95b9184e25423b0e1af44e6ff1a0f90', '张宇辰', NULL, '440104199508080035', NULL, 3, 3, NULL, NULL, 1, '2026-01-23 21:48:39', '2026-01-23 21:48:39', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (33, NULL, '13600000001', '863dd9b05b1b8bf20726dc15995aec28', '张伟', NULL, '110105198803124511', NULL, 3, 1, NULL, NULL, 1, '2026-01-23 22:06:35', '2026-01-23 22:06:35', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (34, NULL, '13600000002', '84d668b25205dd5ec5e34b2462d7bb24', '李娜', NULL, '120103199207256724', NULL, 3, 1, NULL, NULL, 1, '2026-01-23 22:07:19', '2026-01-23 22:07:19', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (35, NULL, '13600000003', '7b529a6d71a6171e14b77e143c9682fc', '王强', NULL, '130102198511083457', NULL, 3, 1, NULL, NULL, 1, '2026-01-23 22:07:48', '2026-01-23 22:07:48', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (36, NULL, '13600000004', '146667b73f4b0ed9460c1f399e404432', '刘敏', NULL, '140106199005168926', NULL, 3, 1, NULL, NULL, 1, '2026-01-23 22:08:14', '2026-01-23 22:08:14', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (37, NULL, '13600000005', '9533388fabab360acd375674012ef2b4', '陈涛', NULL, '150104198709235678', NULL, 3, 1, NULL, NULL, 1, '2026-01-23 22:08:51', '2026-01-23 22:08:51', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (38, NULL, '13600000006', '92cbf9ee8a14ec349afca1c35e6d8763', '赵静', NULL, '110108199302147829', NULL, 3, 1, NULL, NULL, 1, '2026-01-23 22:09:23', '2026-01-23 22:09:23', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (39, NULL, '13600000007', 'ae5ca8f0ca67f1fb56d68513b25d47cb', '孙浩', NULL, '120107198906302345', NULL, 3, 1, NULL, NULL, 1, '2026-01-23 22:09:47', '2026-01-23 22:09:47', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (40, NULL, '13600000008', '2c808f4f757814197d34ca7f99266b6b', '周丽', NULL, '130105199104086789', NULL, 3, 1, NULL, NULL, 1, '2026-01-23 22:10:19', '2026-01-23 22:10:19', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (41, NULL, '13600000009', '7782309ed5cdee11c5f749cdca97f9bf', '吴刚', NULL, '140102198612153456', NULL, 3, 1, NULL, NULL, 1, '2026-01-23 22:10:46', '2026-01-23 22:10:46', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (42, NULL, '13600000010', 'a1e2af7bdb4335bbbfca7633ea8d9e3d', '郑晓', NULL, '150103199408197892', NULL, 3, 1, NULL, NULL, 1, '2026-01-23 22:11:10', '2026-01-23 22:11:10', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (43, NULL, '13700000001', 'bb8f4bea79c64ca3e4e6a6708347ecfc', '马芳', NULL, '110102197805204567', NULL, 3, 2, NULL, NULL, 1, '2026-01-23 22:12:08', '2026-01-23 22:12:08', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (44, NULL, '13700000002', 'e10adc3949ba59abbe56e057f20f883e', '朱军', NULL, '120104197908123456', NULL, 3, 2, NULL, NULL, 1, '2026-01-23 22:12:31', '2026-01-23 22:12:31', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (45, NULL, '13700000003', 'a42abae7d7d744f30731187e1ad43cbb', '胡英', NULL, '130103198003256789', NULL, 3, 2, NULL, NULL, 1, '2026-01-23 22:12:57', '2026-01-23 22:12:57', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (46, NULL, '13700000004', '7a69f81b990d74c082a7c4ef8ffae3f3', '林波', NULL, '140105198107182345', NULL, 3, 2, NULL, NULL, 1, '2026-01-23 22:13:20', '2026-01-23 22:13:20', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (47, NULL, '13700000005', 'ff9a1d20421eb3bf01babd982ce2ce78', '郭梅', NULL, '150102198209305678', NULL, 3, 2, NULL, NULL, 1, '2026-01-23 22:14:19', '2026-01-23 22:14:19', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (48, NULL, '13700000006', '7782309ed5cdee11c5f749cdca97f9bf', '黄勇', NULL, '110106198311153456', NULL, 3, 2, NULL, NULL, 1, '2026-01-23 22:14:56', '2026-01-23 22:14:56', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (49, NULL, '13700000007', 'b7bd761b582cdddaf3aca6aed1643dc0', '董丽', NULL, '120108198404207890', NULL, 3, 2, NULL, NULL, 1, '2026-01-23 22:15:26', '2026-01-23 22:15:26', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (50, NULL, '13700000008', '968d532a87df26221d3b53f046b5f0d3', '梁刚', NULL, '130106198506082345', NULL, 3, 2, NULL, NULL, 1, '2026-01-23 22:15:50', '2026-01-23 22:15:50', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (51, NULL, '13700000009', '39e5cddf9d6fe5c39d348b5e2d45c07d', '谢静', NULL, '140103198602146789', NULL, 3, 2, NULL, NULL, 1, '2026-01-23 22:16:34', '2026-01-23 22:16:34', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (52, NULL, '13700000010', '51978ed02c82d655326afeb6ced426d4', '韩强', NULL, '150104198705233453', NULL, 3, 2, NULL, NULL, 1, '2026-01-23 22:16:59', '2026-01-23 22:16:59', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (53, NULL, '13900002000', '1e158a59629c8b5e4502ec6f65951ec6', '陈明', NULL, '310101197508101234', NULL, 3, 3, NULL, NULL, 1, '2026-01-23 22:18:23', '2026-01-23 22:18:23', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (54, NULL, '13900003000', '309b04b372507f3259411be6d88893fb', '李娟', NULL, '310102197804152345', NULL, 3, 3, NULL, NULL, 1, '2026-01-23 22:18:54', '2026-01-23 22:18:54', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (55, NULL, '13900004000', 'a5510d7d4fb3d917f01a797e5e6932d4', '王浩', NULL, '310103198011203456', NULL, 3, 3, NULL, NULL, 1, '2026-01-23 22:19:13', '2026-01-23 22:19:13', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (56, NULL, '13900005000', '4bc85970799658e5cd68932db2c95f29', '刘芳', NULL, '310104198207254567', NULL, 3, 3, NULL, NULL, 1, '2026-01-23 22:19:36', '2026-01-23 22:19:36', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (57, NULL, '13900006000', 'd6057c640358036b890ac394c46abb46', '赵刚', NULL, '310105198503125678', NULL, 3, 3, NULL, NULL, 1, '2026-01-23 22:20:11', '2026-01-23 22:20:11', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (58, NULL, '13900007000', 'ced546de6661cd1bdb01be20b9572106', '孙丽', NULL, '310106198709306789', NULL, 3, 3, NULL, NULL, 1, '2026-01-23 22:20:32', '2026-01-23 22:20:32', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (59, NULL, '13900008000', '726710ad6a006f72ba2d6ffe71298c94', '周明', NULL, '310107198902147894', NULL, 3, 3, NULL, NULL, 1, '2026-01-23 22:21:04', '2026-01-23 22:21:04', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (60, NULL, '13900009000', 'da7990a1a543fb6b70e2aaf70c8114d2', '吴敏', NULL, '310108199005181234', NULL, 3, 3, NULL, NULL, 1, '2026-01-23 22:21:26', '2026-01-23 22:21:26', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (61, NULL, '13900001001', '0110b96270248f746ecca06f1ce09746', '郑涛', NULL, '310109199206202345', NULL, 3, 3, NULL, NULL, 1, '2026-01-23 22:22:04', '2026-01-23 22:22:04', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (62, NULL, '13500001000', 'e10adc3949ba59abbe56e057f20f883e', 'admin1', NULL, NULL, NULL, 4, NULL, NULL, NULL, 1, '2026-01-23 22:48:17', '2026-01-23 22:48:17', 0, NULL, 2);
INSERT INTO `sys_user` VALUES (63, NULL, '13800005000', '7366fd1489f256e2dd0d42a8ffadb0e5', '刘六', NULL, '440582195508061234', NULL, 1, NULL, '1号楼 102', 6, 1, '2026-01-23 23:30:11', '2026-01-23 23:30:11', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (64, NULL, '13800006000', 'b1618cc1c62d6d7c03710c86c1928458', '王宇', NULL, '110101199506182317', NULL, 1, NULL, '2号楼 102', 7, 1, '2026-01-24 21:40:01', '2026-01-24 21:40:01', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (65, NULL, '13800007000', '534711d3b804d93a16ec590f3adbd86a', '陈曦', NULL, '310101198809221524', NULL, 1, NULL, '1号楼 103', 8, 1, '2026-01-24 21:41:43', '2026-01-24 21:41:43', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (66, NULL, '13800008000', 'a7314bd1eb618f8ea7c4bb5419fbff1d', '林晓峰', NULL, '440104199010053412', NULL, 1, NULL, '2号楼 103', 9, 1, '2026-01-24 21:43:38', '2026-01-24 21:43:38', 0, NULL, NULL);
INSERT INTO `sys_user` VALUES (67, NULL, '13500000002', 'e10adc3949ba59abbe56e057f20f883e', 'test', NULL, NULL, NULL, 4, NULL, NULL, NULL, 1, '2026-01-30 19:45:03', '2026-01-31 20:12:15', 0, NULL, 2);
INSERT INTO `sys_user` VALUES (69, NULL, '13622598933', 'ccc246e2e59d5e2d0cca344114f2065e', '杨梓帆', NULL, '440513200402201456', NULL, 2, NULL, NULL, NULL, 1, '2026-01-30 22:58:38', '2026-01-31 20:12:18', 0, NULL, NULL);

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
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户积分表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_points
-- ----------------------------
INSERT INTO `user_points` VALUES (1, 12, 90, 200, 130, '2026-01-13 21:52:37', '2026-01-13 21:52:37');
INSERT INTO `user_points` VALUES (2, 66, 150, 158, 8, '2026-01-25 20:30:25', '2026-01-25 20:30:25');
INSERT INTO `user_points` VALUES (3, 65, 80, 100, 20, '2026-01-25 20:30:28', '2026-01-25 20:30:28');
INSERT INTO `user_points` VALUES (4, 64, 86, 100, 14, '2026-01-25 20:30:31', '2026-01-25 20:30:31');
INSERT INTO `user_points` VALUES (5, 63, 86, 100, 14, '2026-01-25 20:30:34', '2026-01-25 20:30:34');
INSERT INTO `user_points` VALUES (6, 30, 81, 100, 19, '2026-01-25 20:30:40', '2026-01-25 20:30:40');
INSERT INTO `user_points` VALUES (7, 28, 72, 100, 28, '2026-01-25 20:30:43', '2026-01-25 20:30:43');

SET FOREIGN_KEY_CHECKS = 1;
