use QAssistant;

DROP TABLE IF EXISTS `minioFile`;
CREATE TABLE `minioFile`
(
    `id`           bigint(20)                                              NOT NULL COMMENT '主键',
    `uid`          varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT NULL COMMENT '上传用户uid(sys_user.uid,冗余)',
    `bucket`       varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci  NOT NULL COMMENT 'MinIO bucket',
    `object_name`  varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'MinIO objectName(对象key)',
    `minio_path`   varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'MinIO存储路径(通常为bucket/objectName)',
    `file_name`    varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '上传时文件名(原始文件名)',
    `file_ext`     varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci  NULL     DEFAULT NULL COMMENT '文件扩展名(不含点)',
    `content_type` tinyint(4)                                              NULL     DEFAULT NULL COMMENT '文件类型(MIME)',
    `file_size`    bigint(20)                                              NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
    `deleted`      tinyint(4)                                              NULL     DEFAULT 0 COMMENT '是否删除(0未删除；1已删除)',
    `delete_time`  datetime(0)                                             NULL     DEFAULT NULL COMMENT '删除时间',
    `create_time`  datetime(0)                                             NULL     DEFAULT NULL COMMENT '创建时间(上传时间)',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `uk_bucket_object` (`bucket`, `object_name`) USING BTREE COMMENT '同bucket下对象唯一',
    INDEX `idx_uid` (`uid`) USING BTREE,
    INDEX `idx_deleted` (`deleted`) USING BTREE,
    INDEX `idx_create_time` (`create_time`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8
  COLLATE = utf8_general_ci COMMENT = 'MinIO上传文件记录表'
  ROW_FORMAT = COMPACT;
