-- 示例自有业务表。框架表（sys_*）的建表脚本只有 relation/auth-rbac/auth-rbac.sql 一份，
-- 由 sample/pom.xml 挂载到 classpath:sql/，本文件不重复定义。
-- 只放本示例独有的表；会话表由 spring-session 的 initialize-schema 创建。
-- 语法需同时被 MySQL 与 H2(MODE=MySQL) 接受，由 scripts/check-sql-compat.sh 实测校验。

DROP TABLE IF EXISTS `security_sample_business`;
CREATE TABLE `security_sample_business`
(
    `id`          bigint NOT NULL AUTO_INCREMENT,
    `name`        varchar(255) NULL DEFAULT NULL,
    `description` varchar(255) NULL DEFAULT NULL,
    `create_time` datetime NULL DEFAULT NULL,
    `update_time` datetime NULL DEFAULT NULL,
    `is_deleted`  tinyint(1) NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

-- 会议室：由 auth-rbac-boot-sample 的 meetingRoom / meetingRoomApprove 两组控制器移植而来，
-- MeetingRoomMpMapper 的 @AuthDataRange 与控制器上的 @AuthDataOperation 都作用在本表上。
DROP TABLE IF EXISTS `security_sample_meeting_room`;
CREATE TABLE `security_sample_meeting_room`
(
    `id`           bigint       NOT NULL AUTO_INCREMENT,
    `name`         varchar(255) NOT NULL COMMENT '会议室名字',
    `user_count`   int          DEFAULT NULL COMMENT ' 会议室容纳人数',
    `place`        varchar(255) DEFAULT NULL COMMENT '位置',
    `remark`       varchar(255) DEFAULT NULL COMMENT '备注',
    `is_video`     tinyint      DEFAULT NULL COMMENT '是否有电视屏幕',
    `is_projector` tinyint      DEFAULT NULL COMMENT '是否有投影仪',
    `is_phone`     tinyint      DEFAULT NULL COMMENT '是否有座机电话',
    `create_time`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sec_sample_meeting_room_name` (`name`)
) COMMENT='会议室';
