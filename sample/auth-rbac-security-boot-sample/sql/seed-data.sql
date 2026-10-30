-- 示例种子数据。每行都用 WHERE NOT EXISTS 守卫，可被 spring.sql.init 与 SQL 兼容检查反复执行，
-- 不产生重复行；与 auth-rbac-boot-sample 的种子在同一个库里顺序执行时也不会撞主键/唯一键。
-- 语法需同时被 MySQL 与 H2(MODE=MySQL) 接受，由 scripts/check-sql-compat.sh 实测校验。

-- ============================================================
-- 1. 角色与部门
--    部门树：1 根部门 -> 2 总部 -> 3 研发部；4 外部合作部是另一棵根。
--    部门继承演示靠这条层级：subordinate 在 3（总部的子部门），admin2 在 2（总部自己），
--    outsider 在 4（与总部无层级关系）。
-- ============================================================
INSERT INTO `sys_role` (`id`, `name`)
SELECT 1, '超级管理员'
WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 1);

INSERT INTO `sys_dept` (`id`, `name`, `parent_id`)
SELECT 1, '根部门', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dept` WHERE `id` = 1);
INSERT INTO `sys_dept` (`id`, `name`, `parent_id`)
SELECT 2, '总部', 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_dept` WHERE `id` = 2);
INSERT INTO `sys_dept` (`id`, `name`, `parent_id`)
SELECT 3, '研发部', 2
WHERE NOT EXISTS (SELECT 1 FROM `sys_dept` WHERE `id` = 3);
INSERT INTO `sys_dept` (`id`, `name`, `parent_id`)
SELECT 4, '外部合作部', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dept` WHERE `id` = 4);

-- ============================================================
-- 2. 演示账号
--    admin=超级管理员(角色 1)，admin2 无角色且同时挂总部与外部合作部，
--    subordinate 无角色，outsider 无角色。四个账号密码统一 123456（bcrypt）。
--    用户主键由自增分配，关联行统一按 username 取 id，避免与其它示例的固定主键冲突。
-- ============================================================
INSERT INTO `sys_user` (`name`, `nick_name`, `username`, `gender`)
SELECT '系统用户', '系统用户', 'admin', 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_user` WHERE `username` = 'admin');
INSERT INTO `sys_user` (`name`, `nick_name`, `username`, `gender`)
SELECT '系统用户2', '系统用户2', 'admin2', 2
WHERE NOT EXISTS (SELECT 1 FROM `sys_user` WHERE `username` = 'admin2');
INSERT INTO `sys_user` (`name`, `nick_name`, `username`, `gender`)
SELECT '下级部门用户', '下级部门用户', 'subordinate', 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_user` WHERE `username` = 'subordinate');
INSERT INTO `sys_user` (`name`, `nick_name`, `username`, `gender`)
SELECT '外部协作用户', '外部协作用户', 'outsider', 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_user` WHERE `username` = 'outsider');

INSERT INTO `sys_user_credential` (`user_id`, `password`, `enabled`)
SELECT u.`id`, '{bcrypt}$2a$10$uCJpehGeWfvENTq.6LVrquPrGhZ9/QJXHmXmkNlu9cvnevFra1YdW', 1
FROM `sys_user` u
WHERE u.`username` = 'admin'
  AND NOT EXISTS (SELECT 1 FROM `sys_user_credential` c WHERE c.`user_id` = u.`id`);
INSERT INTO `sys_user_credential` (`user_id`, `password`, `enabled`)
SELECT u.`id`, '{bcrypt}$2a$10$uCJpehGeWfvENTq.6LVrquPrGhZ9/QJXHmXmkNlu9cvnevFra1YdW', 1
FROM `sys_user` u
WHERE u.`username` = 'admin2'
  AND NOT EXISTS (SELECT 1 FROM `sys_user_credential` c WHERE c.`user_id` = u.`id`);
INSERT INTO `sys_user_credential` (`user_id`, `password`, `enabled`)
SELECT u.`id`, '{bcrypt}$2a$10$uCJpehGeWfvENTq.6LVrquPrGhZ9/QJXHmXmkNlu9cvnevFra1YdW', 1
FROM `sys_user` u
WHERE u.`username` = 'subordinate'
  AND NOT EXISTS (SELECT 1 FROM `sys_user_credential` c WHERE c.`user_id` = u.`id`);
INSERT INTO `sys_user_credential` (`user_id`, `password`, `enabled`)
SELECT u.`id`, '{bcrypt}$2a$10$uCJpehGeWfvENTq.6LVrquPrGhZ9/QJXHmXmkNlu9cvnevFra1YdW', 1
FROM `sys_user` u
WHERE u.`username` = 'outsider'
  AND NOT EXISTS (SELECT 1 FROM `sys_user_credential` c WHERE c.`user_id` = u.`id`);

INSERT INTO `sys_user_dept` (`user_id`, `dept_id`, `is_primary`)
SELECT u.`id`, 1, 1 FROM `sys_user` u
WHERE u.`username` = 'admin'
  AND NOT EXISTS (SELECT 1 FROM `sys_user_dept` d WHERE d.`user_id` = u.`id` AND d.`dept_id` = 1);
INSERT INTO `sys_user_dept` (`user_id`, `dept_id`, `is_primary`)
SELECT u.`id`, 2, 1 FROM `sys_user` u
WHERE u.`username` = 'admin2'
  AND NOT EXISTS (SELECT 1 FROM `sys_user_dept` d WHERE d.`user_id` = u.`id` AND d.`dept_id` = 2);
INSERT INTO `sys_user_dept` (`user_id`, `dept_id`, `is_primary`)
SELECT u.`id`, 4, 0 FROM `sys_user` u
WHERE u.`username` = 'admin2'
  AND NOT EXISTS (SELECT 1 FROM `sys_user_dept` d WHERE d.`user_id` = u.`id` AND d.`dept_id` = 4);
INSERT INTO `sys_user_dept` (`user_id`, `dept_id`, `is_primary`)
SELECT u.`id`, 3, 1 FROM `sys_user` u
WHERE u.`username` = 'subordinate'
  AND NOT EXISTS (SELECT 1 FROM `sys_user_dept` d WHERE d.`user_id` = u.`id` AND d.`dept_id` = 3);
INSERT INTO `sys_user_dept` (`user_id`, `dept_id`, `is_primary`)
SELECT u.`id`, 4, 1 FROM `sys_user` u
WHERE u.`username` = 'outsider'
  AND NOT EXISTS (SELECT 1 FROM `sys_user_dept` d WHERE d.`user_id` = u.`id` AND d.`dept_id` = 4);

INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.`id`, 1 FROM `sys_user` u
WHERE u.`username` = 'admin'
  AND NOT EXISTS (SELECT 1 FROM `sys_user_role` r WHERE r.`user_id` = u.`id` AND r.`role_id` = 1);

-- ============================================================
-- 3. 业务数据与权限槽位
--    testrange：admin 可见 id=1 这一行；testedit：admin 可操作 id=1。
--    另外两行业务数据故意不授权，用来让“允许但 0 行”与“只有部分行”可区分。
-- ============================================================
INSERT INTO `security_sample_business` (`id`, `name`, `description`, `create_time`, `is_deleted`)
SELECT 1, '可通过超级管理员1', '超级管理员有权限', '2024-08-01 00:00:00', 0
WHERE NOT EXISTS (SELECT 1 FROM `security_sample_business` WHERE `id` = 1);
INSERT INTO `security_sample_business` (`id`, `name`, `description`, `create_time`, `is_deleted`)
SELECT 2, '可通过超级管理员2', '超级管理员有权限', '2024-08-01 00:00:00', 0
WHERE NOT EXISTS (SELECT 1 FROM `security_sample_business` WHERE `id` = 2);
INSERT INTO `security_sample_business` (`id`, `name`, `description`, `create_time`, `is_deleted`)
SELECT 3, '不可通过超级管理员', '超级管理员没有权限', '2024-08-01 00:00:00', 0
WHERE NOT EXISTS (SELECT 1 FROM `security_sample_business` WHERE `id` = 3);

INSERT INTO `sys_auth_data` (`business_function`, `data_id`, `operate`, `authority`, `description`)
SELECT 'testrange', 1, 'unified', 'ROLE__1', 'ROLE__超级管理员'
WHERE NOT EXISTS (SELECT 1 FROM `sys_auth_data`
                  WHERE `business_function` = 'testrange' AND `data_id` = 1
                    AND `operate` = 'unified' AND `authority` = 'ROLE__1');
INSERT INTO `sys_auth_data` (`business_function`, `data_id`, `operate`, `authority`, `description`)
SELECT 'testedit', 1, 'unified', 'ROLE__1', 'ROLE__超级管理员'
WHERE NOT EXISTS (SELECT 1 FROM `sys_auth_data`
                  WHERE `business_function` = 'testedit' AND `data_id` = 1
                    AND `operate` = 'unified' AND `authority` = 'ROLE__1');

-- ============================================================
-- 4. 菜单行与授权
--    /test/checkMenu 用 auth_type=2，继承父菜单 /test 的 ROLE__1；
--    /test/checkMenuNone 用 auth_type=1 且不授权，作为菜单拒绝的反例；
--    /deptInherit/rooms 必须用 auth_type=1 并直接挂 RELATIONDEPT__2，
--    否则会被菜单注册器按“非根行”改成继承父菜单，部门继承演示就失效了。
--    ID 沿用 auth-rbac-boot-sample 的固定值，便于两个示例的种子在同一个库里共存。
-- ============================================================
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1825772501489168384, '/test', '测试', 0, 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu_api` WHERE `path` = '/test');
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1825772501489168385, '/test/checkMenu', '测试>测试菜单控制', p.`id`, 2
FROM `sys_menu_api` p
WHERE p.`path` = '/test'
  AND NOT EXISTS (SELECT 1 FROM `sys_menu_api` c WHERE c.`path` = '/test/checkMenu');
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1825772501489168386, '/test/checkMenuNone', '测试>测试菜单控制无权限', p.`id`, 1
FROM `sys_menu_api` p
WHERE p.`path` = '/test'
  AND NOT EXISTS (SELECT 1 FROM `sys_menu_api` c WHERE c.`path` = '/test/checkMenuNone');

INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 9100000000000000001, '/deptInherit', '部门继承演示', 0, 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu_api` WHERE `path` = '/deptInherit');
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 9100000000000000002, '/deptInherit/rooms', '部门继承演示>总部下级可见', p.`id`, 1
FROM `sys_menu_api` p
WHERE p.`path` = '/deptInherit'
  AND NOT EXISTS (SELECT 1 FROM `sys_menu_api` c WHERE c.`path` = '/deptInherit/rooms');

INSERT INTO `sys_auth_menu` (`api_id`, `authority`, `description`)
SELECT m.`id`, 'ROLE__1', 'ROLE__超级管理员'
FROM `sys_menu_api` m
WHERE m.`path` = '/test'
  AND NOT EXISTS (SELECT 1 FROM `sys_auth_menu` a WHERE a.`api_id` = m.`id` AND a.`authority` = 'ROLE__1');
INSERT INTO `sys_auth_menu` (`api_id`, `authority`, `description`)
SELECT m.`id`, 'RELATIONDEPT__2', 'RELATIONDEPT__总部'
FROM `sys_menu_api` m
WHERE m.`path` = '/deptInherit/rooms'
  AND NOT EXISTS (SELECT 1 FROM `sys_auth_menu` a
                  WHERE a.`api_id` = m.`id` AND a.`authority` = 'RELATIONDEPT__2');
