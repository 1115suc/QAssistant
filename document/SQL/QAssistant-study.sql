CREATE TABLE `study_session`
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`       VARCHAR(15) NOT NULL COMMENT '用户标识',
    `category`      VARCHAR(25) NOT NULL DEFAULT '' COMMENT '用户自定义标签 eg.语文/数学',
    `start_time`    DATETIME    NOT NULL COMMENT '开始时间',
    `end_time`      DATETIME    NULL DEFAULT NULL COMMENT '结束时间，未结束时为NULL',
    `focus_minutes` SMALLINT    NULL DEFAULT NULL COMMENT '专注时长(分钟)，结束后计算写入',
    `rest_minutes`  SMALLINT    NOT NULL DEFAULT 0 COMMENT '本次会话后休息时长(分钟)',
    `status`        TINYINT     NOT NULL DEFAULT 1 COMMENT '状态: 1=进行中 2=正常结束 3=系统关闭',
    `created_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX           `idx_user_start` (`user_id`, `start_time`),
    INDEX           `idx_user_category` (`user_id`, `category`),
    INDEX           `idx_user_status` (`user_id`, `status`)
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  COMMENT = '学习会话记录表';

CREATE TABLE `study_daily_summary`
(
    `id`                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`             VARCHAR(15) NOT NULL COMMENT '用户标识',
    `study_date`          DATE        NOT NULL COMMENT '统计日期',
    `total_study_minutes` SMALLINT    NOT NULL DEFAULT 0 COMMENT '当日总学习时长(分钟)',
    `total_rest_minutes`  SMALLINT    NOT NULL DEFAULT 0 COMMENT '当日总休息时长(分钟)',
    `session_count`       TINYINT     NOT NULL DEFAULT 0 COMMENT '当日学习次数',
    `category_minutes`    VARCHAR(255)        NOT NULL COMMENT '各标签时长 {"语文":90,"数学":60}',
    `updated_at`          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uniq_user_date` (`user_id`, `study_date`)
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci
  COMMENT = '每日学习汇总统计表';