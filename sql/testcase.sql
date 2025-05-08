-- ----------------------------
-- 1. 测试用例文件夹表结构
-- ----------------------------
CREATE TABLE `test_folder` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件夹ID',
  `name` varchar(50) NOT NULL COMMENT '文件夹名称',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父文件夹ID',
  `create_by` varchar(64) NOT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='测试用例文件夹';

-- ----------------------------
-- 2. 测试用例脑图表结构 (合并了原test_mindmap和test_mindmap_content表)
-- ----------------------------
CREATE TABLE `test_mindmap` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '脑图ID',
  `name` varchar(100) NOT NULL COMMENT '用例名称',
  `folder_id` bigint NOT NULL COMMENT '所属文件夹ID',
  `requirement` varchar(255) DEFAULT NULL COMMENT '关联需求链接',
  `owner` varchar(64) DEFAULT NULL COMMENT '负责人',
  `content` longtext NOT NULL COMMENT '脑图内容(JSON格式)',
  `create_by` varchar(64) NOT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`id`),
  KEY `idx_folder_id` (`folder_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='测试用例脑图';

-- ----------------------------
-- 添加脑图历史版本表
-- ----------------------------
CREATE TABLE `test_mindmap_history` (
  `version_id` bigint NOT NULL AUTO_INCREMENT COMMENT '版本ID',
  `mindmap_id` bigint NOT NULL COMMENT '脑图ID',
  `content` longtext NOT NULL COMMENT '历史脑图内容(JSON格式)',
  `description` varchar(255) DEFAULT NULL COMMENT '版本描述',
  `create_by` varchar(64) NOT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`version_id`),
  KEY `idx_mindmap_id` (`mindmap_id`),
  CONSTRAINT `fk_mindmap_history` FOREIGN KEY (`mindmap_id`) REFERENCES `test_mindmap` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='测试用例脑图历史版本';

-- ----------------------------
-- 初始数据
-- ----------------------------
INSERT INTO `test_folder` VALUES (1, '默认文件夹', 0, 1, 'admin', CURRENT_TIMESTAMP, NULL, NULL, '0');

-- 表关系说明:
-- 1. test_folder: 自引用关系，通过parent_id字段实现文件夹树形结构
-- 2. test_mindmap: 与test_folder是多对一关系，通过folder_id关联到文件夹
-- 3. test_mindmap_history: 与test_mindmap是多对一关系，通过mindmap_id关联脑图基本信息 