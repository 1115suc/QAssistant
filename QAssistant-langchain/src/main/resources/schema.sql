-- 用户自定义 AI 模型表
CREATE TABLE `user_ai_model` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `uid` varchar(64) NOT NULL COMMENT '用户UID',
  `model_name` varchar(64) NOT NULL COMMENT '模型名称',
  `base_url` varchar(255) DEFAULT NULL COMMENT '模型调用URL',
  `api_key` varchar(255) DEFAULT NULL COMMENT 'API Key',
  `provider` varchar(64) DEFAULT NULL COMMENT '模型提供商 (OpenAI, DeepSeek等)',
  `description` varchar(255) DEFAULT NULL COMMENT '模型描述',
  `is_public` tinyint(1) DEFAULT 0 COMMENT '是否公开',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户自定义AI模型表';

-- 用户 AI 个性化配置表
CREATE TABLE `user_ai_preference` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `uid` varchar(64) NOT NULL COMMENT '用户UID',
  `ai_model_id` bigint(20) NOT NULL COMMENT '关联的AI模型ID',
  `temperature` decimal(3,2) DEFAULT 0.70 COMMENT '温度参数',
  `top_p` decimal(3,2) DEFAULT 1.00 COMMENT '核采样参数',
  `max_tokens` int(11) DEFAULT 2048 COMMENT '最大生成Token数',
  `system_prompt` text COMMENT '系统提示词',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_ai` (`uid`, `ai_model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户AI个性化配置表';
