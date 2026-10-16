-- 示例自有表建表脚本：MySQL 与 H2(MODE=MySQL) 通用，不写 ENGINE / CHARACTER SET / COLLATE
-- 只放 auth-rbac-boot-sample 自己定义的表；框架表 sys_* 的 DDL 在 docs/sql/auth-rbac.sql，此处不重复
-- 会话存储所需的表由 spring-session 的 spring.session.jdbc.initialize-schema 创建，此处不建
-- 兼容性由 scripts/check-sql-compat.sh 用 H2 实测校验

-- ----------------------------
-- Table structure for testauth_annotation_business
-- ----------------------------
DROP TABLE IF EXISTS `testauth_annotation_business`;
CREATE TABLE `testauth_annotation_business`
(
    `id`          bigint NOT NULL AUTO_INCREMENT,
    `name`        varchar(255)  NULL DEFAULT NULL,
    `description` varchar(255)  NULL DEFAULT NULL,
    `create_time` datetime NULL DEFAULT NULL,
    `update_time` datetime NULL DEFAULT NULL,
    `is_deleted`  tinyint(1) NULL DEFAULT 0,
    PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `meeting_room`;
CREATE TABLE `meeting_room`
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
    UNIQUE KEY `uk_meeting_room_name` (`name`)
) COMMENT='会议室';
