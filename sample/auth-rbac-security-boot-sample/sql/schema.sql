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
