-- 本示例种子数据：每行都用 WHERE NOT EXISTS 守卫，可被 spring.sql.init 反复执行
-- 不产生重复行；使用者手工增删的行按守卫条件自然收敛，不会被重复灌入
-- 语法需同时被 MySQL 与 H2(MODE=MySQL) 接受，由 scripts/check-sql-compat.sh 实测校验

-- ============================================================
-- 1. 示例用户与权限串（AuthCurrentUserContextHolder 的数据来源）
--    alice=工单管理员, bob=只读角色, dave=部门角色(双位置组合), erin=权限串含引号载荷
--    frank=只持有一条继承型串 DEPTINHERIT__100（该串的管辖用例见 /deptInherit/ledger）
--    carol 故意没有任何行，用来演"权限集合为空即拒绝"
-- ============================================================
INSERT INTO `demo_user_auth` (`username`, `auth_str`, `auth_desc`)
SELECT 'alice', 'ROLE__10', 'ROLE__工单管理员'
WHERE NOT EXISTS (SELECT 1 FROM `demo_user_auth` WHERE `username` = 'alice' AND `auth_str` = 'ROLE__10');
INSERT INTO `demo_user_auth` (`username`, `auth_str`, `auth_desc`)
SELECT 'alice', 'USER__1', 'USER__爱丽丝'
WHERE NOT EXISTS (SELECT 1 FROM `demo_user_auth` WHERE `username` = 'alice' AND `auth_str` = 'USER__1');
INSERT INTO `demo_user_auth` (`username`, `auth_str`, `auth_desc`)
SELECT 'bob', 'ROLE__20', 'ROLE__只读角色'
WHERE NOT EXISTS (SELECT 1 FROM `demo_user_auth` WHERE `username` = 'bob' AND `auth_str` = 'ROLE__20');
INSERT INTO `demo_user_auth` (`username`, `auth_str`, `auth_desc`)
SELECT 'dave', 'DEPT_ROLE__100_20', 'DEPT_ROLE__研发部_资产角色'
WHERE NOT EXISTS (SELECT 1 FROM `demo_user_auth` WHERE `username` = 'dave' AND `auth_str` = 'DEPT_ROLE__100_20');
-- frank 只持有继承型串，没有角色：用来证明"谁能进 /deptInherit/ledger"完全由这条串决定
INSERT INTO `demo_user_auth` (`username`, `auth_str`, `auth_desc`)
SELECT 'frank', 'DEPTINHERIT__100', 'DEPTINHERIT__研发部'
WHERE NOT EXISTS (SELECT 1 FROM `demo_user_auth` WHERE `username` = 'frank' AND `auth_str` = 'DEPTINHERIT__100');
-- erin 的权限串本身含单引号，用来验证权限串只作为数据参与比对、不会被解释成语句片段
INSERT INTO `demo_user_auth` (`username`, `auth_str`, `auth_desc`)
SELECT 'erin', 'ROLE__9'' OR ''1''=''1', 'ROLE__9'' OR ''1''=''1'
WHERE NOT EXISTS (SELECT 1 FROM `demo_user_auth` WHERE `username` = 'erin' AND `auth_str` = 'ROLE__9'' OR ''1''=''1');

-- ============================================================
-- 2. 业务数据：客户
-- ============================================================
INSERT INTO `demo_customer` (`id`, `name`)
SELECT 101, '归属客户A' WHERE NOT EXISTS (SELECT 1 FROM `demo_customer` WHERE `id` = 101);
INSERT INTO `demo_customer` (`id`, `name`)
SELECT 102, '归属客户B' WHERE NOT EXISTS (SELECT 1 FROM `demo_customer` WHERE `id` = 102);
INSERT INTO `demo_customer` (`id`, `name`)
SELECT 201, '下单客户A' WHERE NOT EXISTS (SELECT 1 FROM `demo_customer` WHERE `id` = 201);
INSERT INTO `demo_customer` (`id`, `name`)
SELECT 202, '下单客户B' WHERE NOT EXISTS (SELECT 1 FROM `demo_customer` WHERE `id` = 202);

-- ============================================================
-- 3. 业务数据：工单（两个客户外键，用来区分同一张表的两个别名实例）
-- ============================================================
INSERT INTO `demo_work_order` (`id`, `title`, `customer_id`, `booking_customer_id`, `amount`, `status`)
SELECT 1, '工单一', 101, 201, 100, 'open' WHERE NOT EXISTS (SELECT 1 FROM `demo_work_order` WHERE `id` = 1);
INSERT INTO `demo_work_order` (`id`, `title`, `customer_id`, `booking_customer_id`, `amount`, `status`)
SELECT 2, '工单二', 102, 201, 200, 'open' WHERE NOT EXISTS (SELECT 1 FROM `demo_work_order` WHERE `id` = 2);
INSERT INTO `demo_work_order` (`id`, `title`, `customer_id`, `booking_customer_id`, `amount`, `status`)
SELECT 3, '工单三', 101, 202, 300, 'open' WHERE NOT EXISTS (SELECT 1 FROM `demo_work_order` WHERE `id` = 3);
INSERT INTO `demo_work_order` (`id`, `title`, `customer_id`, `booking_customer_id`, `amount`, `status`)
SELECT 4, '工单四', 102, 202, 400, 'draft' WHERE NOT EXISTS (SELECT 1 FROM `demo_work_order` WHERE `id` = 4);
INSERT INTO `demo_work_order` (`id`, `title`, `customer_id`, `booking_customer_id`, `amount`, `status`)
SELECT 5, '工单五', 101, 201, 500, 'draft' WHERE NOT EXISTS (SELECT 1 FROM `demo_work_order` WHERE `id` = 5);
INSERT INTO `demo_work_order` (`id`, `title`, `customer_id`, `booking_customer_id`, `amount`, `status`)
SELECT 6, '工单六', 102, 202, 600, 'draft' WHERE NOT EXISTS (SELECT 1 FROM `demo_work_order` WHERE `id` = 6);

-- ============================================================
-- 4. 业务数据：资产（主键列名 asset_id，非 id）
-- ============================================================
INSERT INTO `demo_asset` (`asset_id`, `name`, `dept`)
SELECT 101, '投影仪', '研发部' WHERE NOT EXISTS (SELECT 1 FROM `demo_asset` WHERE `asset_id` = 101);
INSERT INTO `demo_asset` (`asset_id`, `name`, `dept`)
SELECT 102, '白板', '研发部' WHERE NOT EXISTS (SELECT 1 FROM `demo_asset` WHERE `asset_id` = 102);
INSERT INTO `demo_asset` (`asset_id`, `name`, `dept`)
SELECT 103, '服务器', '研发部' WHERE NOT EXISTS (SELECT 1 FROM `demo_asset` WHERE `asset_id` = 103);

-- ============================================================
-- 5. 权限槽位：工单
--    edit 给 alice 的 1/2/3；approve 给 alice 的 4/5 与 bob 的 1
--    unified（数据范围列表用）给 alice 的 1/2/4 与 bob 的 1
--    工单 2 故意没有 approve 行，供"授权后翻盘、清空后回归"用
-- ============================================================
INSERT INTO `demo_auth_data` (`biz_module`, `biz_data_id`, `action`, `auth_str`, `auth_desc`)
SELECT 'work_order', 1, 'edit', 'ROLE__10', 'ROLE__工单管理员'
WHERE NOT EXISTS (SELECT 1 FROM `demo_auth_data` WHERE `biz_module` = 'work_order' AND `biz_data_id` = 1 AND `action` = 'edit' AND `auth_str` = 'ROLE__10');
INSERT INTO `demo_auth_data` (`biz_module`, `biz_data_id`, `action`, `auth_str`, `auth_desc`)
SELECT 'work_order', 2, 'edit', 'ROLE__10', 'ROLE__工单管理员'
WHERE NOT EXISTS (SELECT 1 FROM `demo_auth_data` WHERE `biz_module` = 'work_order' AND `biz_data_id` = 2 AND `action` = 'edit' AND `auth_str` = 'ROLE__10');
INSERT INTO `demo_auth_data` (`biz_module`, `biz_data_id`, `action`, `auth_str`, `auth_desc`)
SELECT 'work_order', 3, 'edit', 'ROLE__10', 'ROLE__工单管理员'
WHERE NOT EXISTS (SELECT 1 FROM `demo_auth_data` WHERE `biz_module` = 'work_order' AND `biz_data_id` = 3 AND `action` = 'edit' AND `auth_str` = 'ROLE__10');
INSERT INTO `demo_auth_data` (`biz_module`, `biz_data_id`, `action`, `auth_str`, `auth_desc`)
SELECT 'work_order', 4, 'approve', 'ROLE__10', 'ROLE__工单管理员'
WHERE NOT EXISTS (SELECT 1 FROM `demo_auth_data` WHERE `biz_module` = 'work_order' AND `biz_data_id` = 4 AND `action` = 'approve' AND `auth_str` = 'ROLE__10');
INSERT INTO `demo_auth_data` (`biz_module`, `biz_data_id`, `action`, `auth_str`, `auth_desc`)
SELECT 'work_order', 5, 'approve', 'ROLE__10', 'ROLE__工单管理员'
WHERE NOT EXISTS (SELECT 1 FROM `demo_auth_data` WHERE `biz_module` = 'work_order' AND `biz_data_id` = 5 AND `action` = 'approve' AND `auth_str` = 'ROLE__10');
INSERT INTO `demo_auth_data` (`biz_module`, `biz_data_id`, `action`, `auth_str`, `auth_desc`)
SELECT 'work_order', 1, 'approve', 'ROLE__20', 'ROLE__只读角色'
WHERE NOT EXISTS (SELECT 1 FROM `demo_auth_data` WHERE `biz_module` = 'work_order' AND `biz_data_id` = 1 AND `action` = 'approve' AND `auth_str` = 'ROLE__20');
INSERT INTO `demo_auth_data` (`biz_module`, `biz_data_id`, `action`, `auth_str`, `auth_desc`)
SELECT 'work_order', 1, 'unified', 'ROLE__10', 'ROLE__工单管理员'
WHERE NOT EXISTS (SELECT 1 FROM `demo_auth_data` WHERE `biz_module` = 'work_order' AND `biz_data_id` = 1 AND `action` = 'unified' AND `auth_str` = 'ROLE__10');
INSERT INTO `demo_auth_data` (`biz_module`, `biz_data_id`, `action`, `auth_str`, `auth_desc`)
SELECT 'work_order', 2, 'unified', 'ROLE__10', 'ROLE__工单管理员'
WHERE NOT EXISTS (SELECT 1 FROM `demo_auth_data` WHERE `biz_module` = 'work_order' AND `biz_data_id` = 2 AND `action` = 'unified' AND `auth_str` = 'ROLE__10');
INSERT INTO `demo_auth_data` (`biz_module`, `biz_data_id`, `action`, `auth_str`, `auth_desc`)
SELECT 'work_order', 4, 'unified', 'ROLE__10', 'ROLE__工单管理员'
WHERE NOT EXISTS (SELECT 1 FROM `demo_auth_data` WHERE `biz_module` = 'work_order' AND `biz_data_id` = 4 AND `action` = 'unified' AND `auth_str` = 'ROLE__10');
INSERT INTO `demo_auth_data` (`biz_module`, `biz_data_id`, `action`, `auth_str`, `auth_desc`)
SELECT 'work_order', 1, 'unified', 'ROLE__20', 'ROLE__只读角色'
WHERE NOT EXISTS (SELECT 1 FROM `demo_auth_data` WHERE `biz_module` = 'work_order' AND `biz_data_id` = 1 AND `action` = 'unified' AND `auth_str` = 'ROLE__20');

-- ============================================================
-- 6. 权限槽位：资产（dave 的部门角色；编辑给 101/103，列表给 101/102，故意错开）
-- ============================================================
INSERT INTO `demo_auth_data` (`biz_module`, `biz_data_id`, `action`, `auth_str`, `auth_desc`)
SELECT 'asset', 101, 'edit', 'DEPT_ROLE__100_20', 'DEPT_ROLE__研发部_资产角色'
WHERE NOT EXISTS (SELECT 1 FROM `demo_auth_data` WHERE `biz_module` = 'asset' AND `biz_data_id` = 101 AND `action` = 'edit' AND `auth_str` = 'DEPT_ROLE__100_20');
INSERT INTO `demo_auth_data` (`biz_module`, `biz_data_id`, `action`, `auth_str`, `auth_desc`)
SELECT 'asset', 103, 'edit', 'DEPT_ROLE__100_20', 'DEPT_ROLE__研发部_资产角色'
WHERE NOT EXISTS (SELECT 1 FROM `demo_auth_data` WHERE `biz_module` = 'asset' AND `biz_data_id` = 103 AND `action` = 'edit' AND `auth_str` = 'DEPT_ROLE__100_20');
INSERT INTO `demo_auth_data` (`biz_module`, `biz_data_id`, `action`, `auth_str`, `auth_desc`)
SELECT 'asset', 101, 'unified', 'DEPT_ROLE__100_20', 'DEPT_ROLE__研发部_资产角色'
WHERE NOT EXISTS (SELECT 1 FROM `demo_auth_data` WHERE `biz_module` = 'asset' AND `biz_data_id` = 101 AND `action` = 'unified' AND `auth_str` = 'DEPT_ROLE__100_20');
INSERT INTO `demo_auth_data` (`biz_module`, `biz_data_id`, `action`, `auth_str`, `auth_desc`)
SELECT 'asset', 102, 'unified', 'DEPT_ROLE__100_20', 'DEPT_ROLE__研发部_资产角色'
WHERE NOT EXISTS (SELECT 1 FROM `demo_auth_data` WHERE `biz_module` = 'asset' AND `biz_data_id` = 102 AND `action` = 'unified' AND `auth_str` = 'DEPT_ROLE__100_20');

-- ============================================================
-- 7. 权限槽位：多表语句的关联表（同一个 demo_customer，两个别名各一个槽位）
--    挂 cu（归属客户）只能看到 1/3/5，挂 cb（下单客户）只能看到 3/4/6
-- ============================================================
INSERT INTO `demo_auth_data` (`biz_module`, `biz_data_id`, `action`, `auth_str`, `auth_desc`)
SELECT 'customer_link', 101, 'unified', 'ROLE__10', 'ROLE__工单管理员'
WHERE NOT EXISTS (SELECT 1 FROM `demo_auth_data` WHERE `biz_module` = 'customer_link' AND `biz_data_id` = 101 AND `action` = 'unified' AND `auth_str` = 'ROLE__10');
INSERT INTO `demo_auth_data` (`biz_module`, `biz_data_id`, `action`, `auth_str`, `auth_desc`)
SELECT 'customer_link', 202, 'unified', 'ROLE__10', 'ROLE__工单管理员'
WHERE NOT EXISTS (SELECT 1 FROM `demo_auth_data` WHERE `biz_module` = 'customer_link' AND `biz_data_id` = 202 AND `action` = 'unified' AND `auth_str` = 'ROLE__10');

-- ============================================================
-- 8. 权限槽位：批量上限（1..100 只授了 1，用来证明"判定确实发生"；101 条应直接失败且不打库）
-- ============================================================
INSERT INTO `demo_auth_data` (`biz_module`, `biz_data_id`, `action`, `auth_str`, `auth_desc`)
SELECT 'bulk_probe', 1, 'unified', 'ROLE__10', 'ROLE__工单管理员'
WHERE NOT EXISTS (SELECT 1 FROM `demo_auth_data` WHERE `biz_module` = 'bulk_probe' AND `biz_data_id` = 1 AND `action` = 'unified' AND `auth_str` = 'ROLE__10');

-- ============================================================
-- 9. 权限槽位：探针模块 probe（专供 AuthDataAdaptor 的授权/清空/读回，不碰任何真实业务数据）
--    801 已授予 alice 的 ROLE__10；802/803 故意没有行
-- ============================================================
INSERT INTO `demo_auth_data` (`biz_module`, `biz_data_id`, `action`, `auth_str`, `auth_desc`)
SELECT 'probe', 801, 'edit', 'ROLE__10', 'ROLE__工单管理员'
WHERE NOT EXISTS (SELECT 1 FROM `demo_auth_data` WHERE `biz_module` = 'probe' AND `biz_data_id` = 801 AND `action` = 'edit' AND `auth_str` = 'ROLE__10');

-- ============================================================
-- 10. 权限槽位：引号载荷（ Erin 的权限串含单引号；900 命中、901 不命中 ）
--     该模块没有任何数据范围声明引用它，因此不会走进按字面量拼接的 range 路径
-- ============================================================
INSERT INTO `demo_auth_data` (`biz_module`, `biz_data_id`, `action`, `auth_str`, `auth_desc`)
SELECT 'quote_probe', 900, 'unified', 'ROLE__9'' OR ''1''=''1', 'ROLE__9'' OR ''1''=''1'
WHERE NOT EXISTS (SELECT 1 FROM `demo_auth_data` WHERE `biz_module` = 'quote_probe' AND `biz_data_id` = 900 AND `action` = 'unified' AND `auth_str` = 'ROLE__9'' OR ''1''=''1');

-- ============================================================
-- 11. 菜单授权：路径是应用内路径（不含 /coredemo 前缀）
--     带 @AuthMenu 的行级判定探针端点，把两种相关角色都授上，
--     这样这些端点上观察到的任何拒绝都只可能来自行级判定，而不是菜单鉴权
--     /probe/* 与 /quoteProbe/* 不带 @AuthMenu，因此这里不登记
-- ============================================================
INSERT INTO `demo_menu_auth` (`path`, `auth_str`)
SELECT '/classLevel/all', 'ROLE__10' WHERE NOT EXISTS (SELECT 1 FROM `demo_menu_auth` WHERE `path` = '/classLevel/all' AND `auth_str` = 'ROLE__10');
INSERT INTO `demo_menu_auth` (`path`, `auth_str`)
SELECT '/methodLevel/guarded', 'ROLE__10' WHERE NOT EXISTS (SELECT 1 FROM `demo_menu_auth` WHERE `path` = '/methodLevel/guarded' AND `auth_str` = 'ROLE__10');
INSERT INTO `demo_menu_auth` (`path`, `auth_str`)
SELECT '/methodLevel/noDoc', 'ROLE__10' WHERE NOT EXISTS (SELECT 1 FROM `demo_menu_auth` WHERE `path` = '/methodLevel/noDoc' AND `auth_str` = 'ROLE__10');
INSERT INTO `demo_menu_auth` (`path`, `auth_str`)
SELECT '/classNoDoc/all', 'ROLE__10' WHERE NOT EXISTS (SELECT 1 FROM `demo_menu_auth` WHERE `path` = '/classNoDoc/all' AND `auth_str` = 'ROLE__10');
-- 同名文案的两个端点：只把 A 授给 ROLE__20，B 不授给任何人，用来证明两行相互独立
INSERT INTO `demo_menu_auth` (`path`, `auth_str`)
SELECT '/methodLevel/sameDocA', 'ROLE__20' WHERE NOT EXISTS (SELECT 1 FROM `demo_menu_auth` WHERE `path` = '/methodLevel/sameDocA' AND `auth_str` = 'ROLE__20');
-- 继承型标记只授予 frank：核心层按串的整值相等判定，"下级部门"这层语义由持有方怎么装配决定
INSERT INTO `demo_menu_auth` (`path`, `auth_str`)
SELECT '/deptInherit/ledger', 'DEPTINHERIT__100' WHERE NOT EXISTS (SELECT 1 FROM `demo_menu_auth` WHERE `path` = '/deptInherit/ledger' AND `auth_str` = 'DEPTINHERIT__100');
-- 工单写端点：alice 与 bob 都能进菜单，差异只体现在行级判定上
INSERT INTO `demo_menu_auth` (`path`, `auth_str`)
SELECT '/workOrder/update', 'ROLE__10' WHERE NOT EXISTS (SELECT 1 FROM `demo_menu_auth` WHERE `path` = '/workOrder/update' AND `auth_str` = 'ROLE__10');
INSERT INTO `demo_menu_auth` (`path`, `auth_str`)
SELECT '/workOrder/update', 'ROLE__20' WHERE NOT EXISTS (SELECT 1 FROM `demo_menu_auth` WHERE `path` = '/workOrder/update' AND `auth_str` = 'ROLE__20');
INSERT INTO `demo_menu_auth` (`path`, `auth_str`)
SELECT '/workOrder/approve', 'ROLE__10' WHERE NOT EXISTS (SELECT 1 FROM `demo_menu_auth` WHERE `path` = '/workOrder/approve' AND `auth_str` = 'ROLE__10');
INSERT INTO `demo_menu_auth` (`path`, `auth_str`)
SELECT '/workOrder/approve', 'ROLE__20' WHERE NOT EXISTS (SELECT 1 FROM `demo_menu_auth` WHERE `path` = '/workOrder/approve' AND `auth_str` = 'ROLE__20');
INSERT INTO `demo_menu_auth` (`path`, `auth_str`)
SELECT '/workOrder/batchApprove', 'ROLE__10' WHERE NOT EXISTS (SELECT 1 FROM `demo_menu_auth` WHERE `path` = '/workOrder/batchApprove' AND `auth_str` = 'ROLE__10');
INSERT INTO `demo_menu_auth` (`path`, `auth_str`)
SELECT '/workOrder/batchApprove', 'ROLE__20' WHERE NOT EXISTS (SELECT 1 FROM `demo_menu_auth` WHERE `path` = '/workOrder/batchApprove' AND `auth_str` = 'ROLE__20');
-- 资产写端点：dave 的部门角色，外加 alice 用于对照
INSERT INTO `demo_menu_auth` (`path`, `auth_str`)
SELECT '/asset/update', 'DEPT_ROLE__100_20' WHERE NOT EXISTS (SELECT 1 FROM `demo_menu_auth` WHERE `path` = '/asset/update' AND `auth_str` = 'DEPT_ROLE__100_20');
INSERT INTO `demo_menu_auth` (`path`, `auth_str`)
SELECT '/asset/update', 'ROLE__10' WHERE NOT EXISTS (SELECT 1 FROM `demo_menu_auth` WHERE `path` = '/asset/update' AND `auth_str` = 'ROLE__10');
INSERT INTO `demo_menu_auth` (`path`, `auth_str`)
SELECT '/asset/updateIntegerForm', 'DEPT_ROLE__100_20' WHERE NOT EXISTS (SELECT 1 FROM `demo_menu_auth` WHERE `path` = '/asset/updateIntegerForm' AND `auth_str` = 'DEPT_ROLE__100_20');
INSERT INTO `demo_menu_auth` (`path`, `auth_str`)
SELECT '/asset/batchUpdate', 'DEPT_ROLE__100_20' WHERE NOT EXISTS (SELECT 1 FROM `demo_menu_auth` WHERE `path` = '/asset/batchUpdate' AND `auth_str` = 'DEPT_ROLE__100_20');
INSERT INTO `demo_menu_auth` (`path`, `auth_str`)
SELECT '/asset/scattered', 'DEPT_ROLE__100_20' WHERE NOT EXISTS (SELECT 1 FROM `demo_menu_auth` WHERE `path` = '/asset/scattered' AND `auth_str` = 'DEPT_ROLE__100_20');
-- 权限管理端点：只授给持有 demo_admin 槽位的 alice
INSERT INTO `demo_menu_auth` (`path`, `auth_str`)
SELECT '/authData/grant', 'ROLE__10' WHERE NOT EXISTS (SELECT 1 FROM `demo_menu_auth` WHERE `path` = '/authData/grant' AND `auth_str` = 'ROLE__10');
INSERT INTO `demo_menu_auth` (`path`, `auth_str`)
SELECT '/authData/remove', 'ROLE__10' WHERE NOT EXISTS (SELECT 1 FROM `demo_menu_auth` WHERE `path` = '/authData/remove' AND `auth_str` = 'ROLE__10');
INSERT INTO `demo_menu_auth` (`path`, `auth_str`)
SELECT '/authData/fill', 'ROLE__10' WHERE NOT EXISTS (SELECT 1 FROM `demo_menu_auth` WHERE `path` = '/authData/fill' AND `auth_str` = 'ROLE__10');

-- ============================================================
-- 12. 管理槽位：/authData/* 自身的行级管辖（固定数据ID 900）
-- ============================================================
INSERT INTO `demo_auth_data` (`biz_module`, `biz_data_id`, `action`, `auth_str`, `auth_desc`)
SELECT 'demo_admin', 900, 'unified', 'ROLE__10', 'ROLE__工单管理员'
WHERE NOT EXISTS (SELECT 1 FROM `demo_auth_data` WHERE `biz_module` = 'demo_admin' AND `biz_data_id` = 900 AND `action` = 'unified' AND `auth_str` = 'ROLE__10');
