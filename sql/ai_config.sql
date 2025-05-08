-- ----------------------------
-- AI配置表结构
-- ----------------------------
CREATE TABLE `ai_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `authorization_key` varchar(255) NOT NULL COMMENT 'API认证密钥',
  `prompt_template` text NOT NULL COMMENT '提示词模板',
  `model` varchar(100) NOT NULL COMMENT 'AI模型名称',
  `create_by` varchar(64) NOT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='AI配置表'; 