-- 本示例自有表建表脚本：MySQL 与 H2(MODE=MySQL) 双方都接受的语法
-- 1) 一律 CREATE TABLE IF NOT EXISTS，不写 DROP —— 本示例的数据文件是持久的，
--    删除语义会在每次重启时清掉使用者手工授予/回收的权限
-- 2) 不写 ENGINE / CHARACTER SET / COLLATE，交给各库自己的默认值
-- 3) 本示例不引用任何框架表（sys_*），relation/ 下的脚本也不进本模块 classpath
-- 4) 兼容性由 scripts/check-sql-compat.sh 用 H2 实测校验

-- ----------------------------
-- 权限槽位表：auth-core 的行级数据权限存储
-- 表名与全部列名都故意与框架默认约定不同，由 cu.auth.auth-data-table.* 七项属性映射
-- 业务数据ID 固定 BIGINT：本示例的业务主键一律整型
-- ----------------------------
CREATE TABLE IF NOT EXISTS `demo_auth_data`
(
    `pk`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `biz_module`  varchar(64)  NOT NULL COMMENT '业务功能',
    `biz_data_id` bigint       NOT NULL COMMENT '业务数据ID，取自业务主键',
    `action`      varchar(64)  NOT NULL DEFAULT 'unified' COMMENT '操作类型 unified edit approve 等',
    `auth_str`    varchar(100) NOT NULL COMMENT '权限字符串',
    `auth_desc`   varchar(200) NOT NULL COMMENT '权限描述',
    PRIMARY KEY (`pk`),
    KEY `idx_demo_auth_data_slot` (`biz_module`, `action`, `biz_data_id`, `auth_str`)
) COMMENT = '示例权限槽位表';

-- ----------------------------
-- 菜单授权表：某个应用内路径需要持有哪种权限串
-- 示例自定的规则，与框架的菜单权限表无关（路径存不进整型的数据ID列，所以必须单独一张表）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `demo_menu_auth`
(
    `pk`       bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `path`     varchar(256) NOT NULL COMMENT '应用内请求路径，不含 contextPath',
    `auth_str` varchar(100) NOT NULL COMMENT '被授予该路径的权限字符串',
    PRIMARY KEY (`pk`),
    UNIQUE KEY `uk_demo_menu_auth_path_auth` (`path`, `auth_str`)
) COMMENT = '示例菜单授权表';

-- ----------------------------
-- 菜单API表：启动时由 @AuthMenu/@AuthMenuDoc 扫描自动注册
-- id 为 BIGINT 但不用自增，由示例的 MenuApiService 用雪花算法生成
-- ----------------------------
CREATE TABLE IF NOT EXISTS `demo_menu_api`
(
    `id`        bigint       NOT NULL COMMENT '主键ID，雪花算法生成',
    `path`      varchar(256) NOT NULL COMMENT '请求路径，菜单行的唯一标识',
    `name`      varchar(255) NULL DEFAULT NULL COMMENT '菜单显示名称',
    `parent_id` bigint       NOT NULL DEFAULT 0 COMMENT '父节点ID，0 为根',
    `remark`    varchar(255) NULL DEFAULT NULL COMMENT '备注，供使用者手工改写以验证启动不覆写',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_demo_menu_api_path` (`path`)
) COMMENT = '示例菜单API表';

-- ----------------------------
-- 示例用户权限表：AuthCurrentUserContextHolder 的数据来源
-- 每个用户一行或多行权限串；没有行的用户即"权限集合为空"
-- ----------------------------
CREATE TABLE IF NOT EXISTS `demo_user_auth`
(
    `pk`        bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`  varchar(64)  NOT NULL COMMENT '用户名，取自 X-Demo-User 请求头',
    `auth_str`  varchar(100) NOT NULL COMMENT '权限字符串',
    `auth_desc` varchar(200) NOT NULL COMMENT '权限描述',
    PRIMARY KEY (`pk`),
    UNIQUE KEY `uk_demo_user_auth` (`username`, `auth_str`)
) COMMENT = '示例用户权限表';

-- ----------------------------
-- 业务表：工单。两个整型外键都指向 demo_customer，
-- 用于验证 tableAlias 能把权限条件精确挂到同表的某一个别名实例上
-- ----------------------------
CREATE TABLE IF NOT EXISTS `demo_work_order`
(
    `id`                   bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`                varchar(255) NOT NULL COMMENT '工单标题',
    `customer_id`          bigint       NOT NULL COMMENT '归属客户',
    `booking_customer_id`  bigint       NOT NULL COMMENT '下单客户',
    `amount`               int          NULL DEFAULT NULL COMMENT '金额',
    `status`               varchar(32)  NULL DEFAULT NULL COMMENT '状态',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_demo_work_order_title` (`title`)
) COMMENT = '示例业务表-工单';

-- ----------------------------
-- 业务表：资产。主键列故意不叫 id，用于验证 dataIdColumn 声明
-- ----------------------------
CREATE TABLE IF NOT EXISTS `demo_asset`
(
    `asset_id` bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID，列名非 id',
    `name`     varchar(255) NOT NULL COMMENT '资产名称',
    `dept`     varchar(255) NULL DEFAULT NULL COMMENT '所属部门',
    PRIMARY KEY (`asset_id`),
    UNIQUE KEY `uk_demo_asset_name` (`name`)
) COMMENT = '示例业务表-资产';

-- ----------------------------
-- 业务表：客户，作为多表语句中的关联表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `demo_customer`
(
    `id`   bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` varchar(255) NOT NULL COMMENT '客户名称',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_demo_customer_name` (`name`)
) COMMENT = '示例业务表-客户';

-- ----------------------------
-- 副作用证据表：受管辖写方法的第一行往这里插一条
-- 拒绝发生在方法体之前，所以"没有新增行"就是"方法体没执行"的直接证据
-- biz_data_id 用 varchar 只是日志记录，不参与任何权限比对
-- ----------------------------
CREATE TABLE IF NOT EXISTS `demo_execution_log`
(
    `pk`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `api_key`     varchar(128) NOT NULL COMMENT '被调用的接口标识',
    `biz_data_id` varchar(64)  NULL DEFAULT NULL COMMENT '本次涉及的业务数据ID，仅作记录',
    `memo`        varchar(255) NULL DEFAULT NULL COMMENT '备注',
    `create_time` datetime     NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    PRIMARY KEY (`pk`),
    KEY `idx_demo_execution_log_api` (`api_key`)
) COMMENT = '示例副作用证据表';
