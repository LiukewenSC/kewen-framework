-- 基础支撑初始化脚本：MySQL 与 H2(MODE=MySQL) 通用
-- 不写 ENGINE / CHARACTER SET / COLLATE / ROW_FORMAT，交给各库自己的默认值
-- 兼容性由 scripts/check-sql-compat.sh 用 H2 实测校验

DROP TABLE IF EXISTS `file_detail`;
CREATE TABLE `file_detail` (
    `id`                varchar(32)  NOT NULL COMMENT '文件id',
    `url`               varchar(512) NOT NULL COMMENT '文件访问地址',
    `size`              bigint       DEFAULT NULL COMMENT '文件大小，单位字节',
    `filename`          varchar(256) DEFAULT NULL COMMENT '文件名称',
    `original_filename` varchar(256) DEFAULT NULL COMMENT '原始文件名',
    `base_path`         varchar(256) DEFAULT NULL COMMENT '基础存储路径',
    `path`              varchar(256) DEFAULT NULL COMMENT '存储路径',
    `ext`               varchar(32)  DEFAULT NULL COMMENT '文件扩展名',
    `content_type`      varchar(128) DEFAULT NULL COMMENT 'MIME类型',
    `platform`          varchar(32)  DEFAULT NULL COMMENT '存储平台',
    `th_url`            varchar(512) DEFAULT NULL COMMENT '缩略图访问路径',
    `th_filename`       varchar(256) DEFAULT NULL COMMENT '缩略图名称',
    `th_size`           bigint       DEFAULT NULL COMMENT '缩略图大小，单位字节',
    `th_content_type`   varchar(128) DEFAULT NULL COMMENT '缩略图MIME类型',
    `object_id`         varchar(32)  DEFAULT NULL COMMENT '文件所属对象id',
    `object_type`       varchar(32)  DEFAULT NULL COMMENT '文件所属对象类型，例如用户头像，评价图片',
    `metadata`          text COMMENT '文件元数据',
    `user_metadata`     text COMMENT '文件用户元数据',
    `th_metadata`       text COMMENT '缩略图元数据',
    `th_user_metadata`  text COMMENT '缩略图用户元数据',
    `attr`              text COMMENT '附加属性',
    `file_acl`          varchar(32)  DEFAULT NULL COMMENT '文件ACL',
    `th_file_acl`       varchar(32)  DEFAULT NULL COMMENT '缩略图文件ACL',
    `hash_info`         text COMMENT '哈希信息',
    `upload_id`         varchar(128) DEFAULT NULL COMMENT '上传ID，仅在手动分片上传时使用',
    `upload_status`     int          DEFAULT NULL COMMENT '上传状态，仅在手动分片上传时使用，1：初始化完成，2：上传完成',
    `create_time`       datetime     DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`)
) COMMENT = '文件记录表';

DROP TABLE IF EXISTS `file_part_detail`;
CREATE TABLE `file_part_detail` (
    `id`          varchar(32) NOT NULL COMMENT '分片id',
    `platform`    varchar(32)  DEFAULT NULL COMMENT '存储平台',
    `upload_id`   varchar(128) DEFAULT NULL COMMENT '上传ID，仅在手动分片上传时使用',
    `e_tag`       varchar(255) DEFAULT NULL COMMENT '分片 ETag',
    `part_number` int          DEFAULT NULL COMMENT '分片号。每一个上传的分片都有一个分片号，一般情况下取值范围是1~10000',
    `part_size`   bigint       DEFAULT NULL COMMENT '文件大小，单位字节',
    `hash_info`   text         COMMENT '哈希信息',
    `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`)
) COMMENT = '文件分片信息表，仅在手动分片上传时使用';

DROP TABLE IF EXISTS `sys_request_log`;
CREATE TABLE `sys_request_log` (
    `id`          bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID，数据库自增',
    `user_id`     bigint NULL DEFAULT NULL COMMENT '用户id',
    `user_name`   varchar(64)  DEFAULT NULL COMMENT '用户姓名',
    `api_path`    varchar(255) DEFAULT NULL COMMENT 'API路径',
    `method`      varchar(32)  DEFAULT NULL COMMENT '请求方式',
    `millisecond` int NULL DEFAULT NULL COMMENT '请求持续时间',
    `ip_address`  varchar(64)  DEFAULT NULL COMMENT 'IP地址',
    `ip_comment`  varchar(64)  DEFAULT NULL COMMENT 'IP地址描述',
    `params`      varchar(512) DEFAULT NULL COMMENT '请求param参数',
    `body`        text COMMENT '请求body参数',
    `status`      int NULL DEFAULT NULL COMMENT '状态成功失败',
    `trace_id`    varchar(64)  DEFAULT NULL COMMENT '请求跟踪号',
    `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`)
) COMMENT = '请求日志记录器';
