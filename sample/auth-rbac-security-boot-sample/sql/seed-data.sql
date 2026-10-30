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

-- ============================================================
-- 5. 前端路由菜单（sys_menu_route）
--    本示例只有 /test 与 /deptInherit 两组接口，组织机构、会议管理等页面会打到不存在的接口，
--    所以这里只挂 kewen-web-admin 中不依赖后端数据的页面：首页、文档、组件示例下各页。
--    api_id 全部留空：DefaultSysAuthMenuComposite.getRouteAuthMenuTree 的可见性判据是
--    “api_id 为 null，或命中当前人的权限树”，留空即任何登录人都能看到。
--    ID 用 20xx 小区段，与 auth-rbac-boot-sample 的 2~19 固定主键不撞。
--    这里不能用雪花量级的大整数：/currentUser/routeMenus 把 id 作为 JSON number 下发，
--    前端 JSON.parse 后超过 2^53 的相邻 id 会塌成同一个 double，面包屑 lookupCurrentRoute
--    按 id 匹配就会永远命中第一条菜单。
--    meta 存 JSON 文本：列是 varchar（见 relation/auth-rbac/auth-rbac.sql 的说明），落库就是原文本身，
--    MySQL 与 H2 一致，实体侧 FastjsonTypeHandler 再反序列化成 Map。双引号不用反斜杠转义——
--    MySQL 和 H2 都把单引号串内的 " 当字面量，加了反斜杠反而会被 H2 原样存进去。
-- ============================================================
INSERT INTO `sys_menu_route` (`id`, `name`, `parent_id`, `path`, `redirect`, `component`, `meta`, `icon`, `hidden`, `api_id`, `type`)
SELECT 2001, '首页', 0, '/home', NULL, 'home', '{"affix": true, "externalLinkUrl": null, "isExternalLinks": false}', 'el-icon-s-home', 0, NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu_route` WHERE `path` = '/home');
INSERT INTO `sys_menu_route` (`id`, `name`, `parent_id`, `path`, `redirect`, `component`, `meta`, `icon`, `hidden`, `api_id`, `type`)
SELECT 2002, '文档', 0, '/docs', NULL, 'docs', NULL, 'el-icon-s-order', 0, NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu_route` WHERE `path` = '/docs');
INSERT INTO `sys_menu_route` (`id`, `name`, `parent_id`, `path`, `redirect`, `component`, `meta`, `icon`, `hidden`, `api_id`, `type`)
SELECT 2003, '组件例子', 0, '/demo', '/demo/filtering', 'layout/publics', NULL, 'el-icon-star-on', 0, NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu_route` WHERE `path` = '/demo');

INSERT INTO `sys_menu_route` (`id`, `name`, `parent_id`, `path`, `redirect`, `component`, `meta`, `icon`, `hidden`, `api_id`, `type`)
SELECT 2004, '筛选组件', p.`id`, '/demo/filtering', NULL, 'demo/filtering/index', NULL, 'el-icon-s-marketing', 0, NULL, 1
FROM `sys_menu_route` p
WHERE p.`path` = '/demo'
  AND NOT EXISTS (SELECT 1 FROM `sys_menu_route` c WHERE c.`path` = '/demo/filtering');
INSERT INTO `sys_menu_route` (`id`, `name`, `parent_id`, `path`, `redirect`, `component`, `meta`, `icon`, `hidden`, `api_id`, `type`)
SELECT 2005, '筛选组件详情', p.`id`, '/demo/filtering-details', NULL, 'demo/filtering/component/details', '{"hidden": true}', 'el-icon-s-marketing', 1, NULL, 1
FROM `sys_menu_route` p
WHERE p.`path` = '/demo'
  AND NOT EXISTS (SELECT 1 FROM `sys_menu_route` c WHERE c.`path` = '/demo/filtering-details');
INSERT INTO `sys_menu_route` (`id`, `name`, `parent_id`, `path`, `redirect`, `component`, `meta`, `icon`, `hidden`, `api_id`, `type`)
SELECT 2006, 'v-charts 图表', p.`id`, '/demo/v-charts', NULL, 'demo/vCharts/index', NULL, 'el-icon-data-analysis', 0, NULL, 1
FROM `sys_menu_route` p
WHERE p.`path` = '/demo'
  AND NOT EXISTS (SELECT 1 FROM `sys_menu_route` c WHERE c.`path` = '/demo/v-charts');
INSERT INTO `sys_menu_route` (`id`, `name`, `parent_id`, `path`, `redirect`, `component`, `meta`, `icon`, `hidden`, `api_id`, `type`)
SELECT 2007, '仿电视开关机', p.`id`, '/demo/tVSwitch', NULL, 'demo/tVSwitch/index', NULL, 'el-icon-s-platform', 0, NULL, 1
FROM `sys_menu_route` p
WHERE p.`path` = '/demo'
  AND NOT EXISTS (SELECT 1 FROM `sys_menu_route` c WHERE c.`path` = '/demo/tVSwitch');
INSERT INTO `sys_menu_route` (`id`, `name`, `parent_id`, `path`, `redirect`, `component`, `meta`, `icon`, `hidden`, `api_id`, `type`)
SELECT 2008, '富文本编辑器', p.`id`, '/demo/mavonEditor', NULL, 'demo/mavonEditor/index', NULL, 'el-icon-edit-outline', 0, NULL, 1
FROM `sys_menu_route` p
WHERE p.`path` = '/demo'
  AND NOT EXISTS (SELECT 1 FROM `sys_menu_route` c WHERE c.`path` = '/demo/mavonEditor');
INSERT INTO `sys_menu_route` (`id`, `name`, `parent_id`, `path`, `redirect`, `component`, `meta`, `icon`, `hidden`, `api_id`, `type`)
SELECT 2009, '论坛评价', p.`id`, '/demo/comment', NULL, 'demo/comment/index', NULL, 'el-icon-chat-line-square', 0, NULL, 1
FROM `sys_menu_route` p
WHERE p.`path` = '/demo'
  AND NOT EXISTS (SELECT 1 FROM `sys_menu_route` c WHERE c.`path` = '/demo/comment');
INSERT INTO `sys_menu_route` (`id`, `name`, `parent_id`, `path`, `redirect`, `component`, `meta`, `icon`, `hidden`, `api_id`, `type`)
SELECT 2010, 'Tree 树形控件', p.`id`, '/demo/treeControl', NULL, 'demo/treeControl/index', NULL, 'el-icon-finished', 0, NULL, 1
FROM `sys_menu_route` p
WHERE p.`path` = '/demo'
  AND NOT EXISTS (SELECT 1 FROM `sys_menu_route` c WHERE c.`path` = '/demo/treeControl');
INSERT INTO `sys_menu_route` (`id`, `name`, `parent_id`, `path`, `redirect`, `component`, `meta`, `icon`, `hidden`, `api_id`, `type`)
SELECT 2011, '大数据可视化', p.`id`, 'externalLinkUrl', NULL, 'demo/bigData', '{"isExternalLinks": true, "externalLinkUrl": "/big-data", "externalLinkType": "open"}', 'el-icon-s-data', 0, NULL, 1
FROM `sys_menu_route` p
WHERE p.`path` = '/demo'
  AND NOT EXISTS (SELECT 1 FROM `sys_menu_route` c WHERE c.`path` = '/demo/bigData');

-- 个人中心：隐藏路由，只在菜单数据里出现一次用于验证“hidden 不生成菜单但可直接访问”
INSERT INTO `sys_menu_route` (`id`, `name`, `parent_id`, `path`, `redirect`, `component`, `meta`, `icon`, `hidden`, `api_id`, `type`)
SELECT 2012, '个人中心', 0, '/profile', NULL, 'profile', '{"hidden": true}', 'el-icon-user', 1, NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu_route` WHERE `path` = '/profile');

-- ============================================================
-- 6. 组织机构接口的菜单行与授权（/rbac/*）
--    前端组织管理页调 /rbac/{dept,role,user} 的 list|page|add|update|delete。这些路径由 auth-rbac
--    模块的控制器以 @AuthMenu 声明受管辖，缺菜单行或授权行时 MenuAccessInterceptor 一律回
--    “没有API菜单/...访问权限”的 403。父行 auth_type=1 绑 ROLE__1（admin 实测持有该权限），
--    子行 auth_type=2 继承父行，与本文件 /test 那组同构。
--    ID 沿用 auth-rbac-boot-sample 的固定值，便于两个示例的种子在同一个库里共存。
-- ============================================================
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1818496367898750979, '/rbac/dept', '部门管理', 0, 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu_api` WHERE `path` = '/rbac/dept');
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1818496367898750980, '/rbac/dept/add', '部门管理>添加部门', p.`id`, 2 FROM `sys_menu_api` p
WHERE p.`path` = '/rbac/dept' AND NOT EXISTS (SELECT 1 FROM `sys_menu_api` c WHERE c.`path` = '/rbac/dept/add');
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1818496367898750981, '/rbac/dept/delete', '部门管理>删除部门', p.`id`, 2 FROM `sys_menu_api` p
WHERE p.`path` = '/rbac/dept' AND NOT EXISTS (SELECT 1 FROM `sys_menu_api` c WHERE c.`path` = '/rbac/dept/delete');
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1818496367898750982, '/rbac/dept/list', '部门管理>部门列表', p.`id`, 2 FROM `sys_menu_api` p
WHERE p.`path` = '/rbac/dept' AND NOT EXISTS (SELECT 1 FROM `sys_menu_api` c WHERE c.`path` = '/rbac/dept/list');
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1818496367898750983, '/rbac/dept/page', '部门管理>部门分页', p.`id`, 2 FROM `sys_menu_api` p
WHERE p.`path` = '/rbac/dept' AND NOT EXISTS (SELECT 1 FROM `sys_menu_api` c WHERE c.`path` = '/rbac/dept/page');
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1818496367898750984, '/rbac/dept/update', '部门管理>修改部门', p.`id`, 2 FROM `sys_menu_api` p
WHERE p.`path` = '/rbac/dept' AND NOT EXISTS (SELECT 1 FROM `sys_menu_api` c WHERE c.`path` = '/rbac/dept/update');

INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1818496367898750985, '/rbac/role', '角色管理', 0, 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu_api` WHERE `path` = '/rbac/role');
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1818496367898750986, '/rbac/role/add', '角色管理>添加角色', p.`id`, 2 FROM `sys_menu_api` p
WHERE p.`path` = '/rbac/role' AND NOT EXISTS (SELECT 1 FROM `sys_menu_api` c WHERE c.`path` = '/rbac/role/add');
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1818496367898750987, '/rbac/role/delete', '角色管理>删除角色', p.`id`, 2 FROM `sys_menu_api` p
WHERE p.`path` = '/rbac/role' AND NOT EXISTS (SELECT 1 FROM `sys_menu_api` c WHERE c.`path` = '/rbac/role/delete');
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1818496367898750988, '/rbac/role/list', '角色管理>角色列表', p.`id`, 2 FROM `sys_menu_api` p
WHERE p.`path` = '/rbac/role' AND NOT EXISTS (SELECT 1 FROM `sys_menu_api` c WHERE c.`path` = '/rbac/role/list');
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1818496367898750989, '/rbac/role/page', '角色管理>角色分页', p.`id`, 2 FROM `sys_menu_api` p
WHERE p.`path` = '/rbac/role' AND NOT EXISTS (SELECT 1 FROM `sys_menu_api` c WHERE c.`path` = '/rbac/role/page');
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1818496367898750990, '/rbac/role/update', '角色管理>修改角色', p.`id`, 2 FROM `sys_menu_api` p
WHERE p.`path` = '/rbac/role' AND NOT EXISTS (SELECT 1 FROM `sys_menu_api` c WHERE c.`path` = '/rbac/role/update');

INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1818496367898750991, '/rbac/user', '用户相关', 0, 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu_api` WHERE `path` = '/rbac/user');
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1818496367898750992, '/rbac/user/add', '用户相关>新增', p.`id`, 2 FROM `sys_menu_api` p
WHERE p.`path` = '/rbac/user' AND NOT EXISTS (SELECT 1 FROM `sys_menu_api` c WHERE c.`path` = '/rbac/user/add');
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1818496367898750993, '/rbac/user/delete', '用户相关>删除用户', p.`id`, 2 FROM `sys_menu_api` p
WHERE p.`path` = '/rbac/user' AND NOT EXISTS (SELECT 1 FROM `sys_menu_api` c WHERE c.`path` = '/rbac/user/delete');
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1818496367898750994, '/rbac/user/list', '用户相关>用户列表', p.`id`, 2 FROM `sys_menu_api` p
WHERE p.`path` = '/rbac/user' AND NOT EXISTS (SELECT 1 FROM `sys_menu_api` c WHERE c.`path` = '/rbac/user/list');
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1818496367898750995, '/rbac/user/page', '用户相关>分页', p.`id`, 2 FROM `sys_menu_api` p
WHERE p.`path` = '/rbac/user' AND NOT EXISTS (SELECT 1 FROM `sys_menu_api` c WHERE c.`path` = '/rbac/user/page');
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1818496367898750996, '/rbac/user/update', '用户相关>更新', p.`id`, 2 FROM `sys_menu_api` p
WHERE p.`path` = '/rbac/user' AND NOT EXISTS (SELECT 1 FROM `sys_menu_api` c WHERE c.`path` = '/rbac/user/update');

INSERT INTO `sys_auth_menu` (`api_id`, `authority`, `description`)
SELECT m.`id`, 'ROLE__1', 'ROLE__超级管理员' FROM `sys_menu_api` m
WHERE m.`path` = '/rbac/dept'
  AND NOT EXISTS (SELECT 1 FROM `sys_auth_menu` a WHERE a.`api_id` = m.`id` AND a.`authority` = 'ROLE__1');
INSERT INTO `sys_auth_menu` (`api_id`, `authority`, `description`)
SELECT m.`id`, 'ROLE__1', 'ROLE__超级管理员' FROM `sys_menu_api` m
WHERE m.`path` = '/rbac/role'
  AND NOT EXISTS (SELECT 1 FROM `sys_auth_menu` a WHERE a.`api_id` = m.`id` AND a.`authority` = 'ROLE__1');
INSERT INTO `sys_auth_menu` (`api_id`, `authority`, `description`)
SELECT m.`id`, 'ROLE__1', 'ROLE__超级管理员' FROM `sys_menu_api` m
WHERE m.`path` = '/rbac/user'
  AND NOT EXISTS (SELECT 1 FROM `sys_auth_menu` a WHERE a.`api_id` = m.`id` AND a.`authority` = 'ROLE__1');

-- ============================================================
-- 7. 会议管理与授权（meetingRoom / meetingRoomApprove）
--    两组控制器从 auth-rbac-boot-sample 移植而来，业务功能与操作名取自 MeetingRoomConstant：
--    business_function=meeting_room，operate 分别为 edit_info（改会议室）与 approve（预约会议室）。
--    data_id 按 name 回查会议室表的真实主键，不假定自增落在 1。表名带 security_sample_ 前缀，
--    与 auth-rbac-boot-sample 的 meeting_room 区分开，两套种子才能共库（check-sql-compat.sh 按
--    “一条 CREATE TABLE 对应一张独有表”计数，重名会被判成没建出来）。
--    menu_api 的父行在本示例挂根（parent_id=0），旧示例那条“会议室管理”根节点属
--    meetingRoomPrimary 那一族，已随该端点一并废弃。
-- ============================================================
INSERT INTO `security_sample_meeting_room` (`id`, `name`, `user_count`, `place`, `remark`, `is_video`, `is_projector`, `is_phone`)
SELECT 1, '1103会议室', 32, '11楼', '大会议室，开会使用', 1, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `security_sample_meeting_room` WHERE `name` = '1103会议室');

INSERT INTO `sys_auth_data` (`business_function`, `data_id`, `operate`, `authority`, `description`)
SELECT 'meeting_room', r.`id`, 'edit_info', 'ROLE__1', 'ROLE__超级管理员' FROM `security_sample_meeting_room` r
WHERE r.`name` = '1103会议室'
  AND NOT EXISTS (SELECT 1 FROM `sys_auth_data` a WHERE a.`business_function` = 'meeting_room'
                    AND a.`data_id` = r.`id` AND a.`operate` = 'edit_info' AND a.`authority` = 'ROLE__1');
INSERT INTO `sys_auth_data` (`business_function`, `data_id`, `operate`, `authority`, `description`)
SELECT 'meeting_room', r.`id`, 'approve', 'ROLE__1', 'ROLE__超级管理员' FROM `security_sample_meeting_room` r
WHERE r.`name` = '1103会议室'
  AND NOT EXISTS (SELECT 1 FROM `sys_auth_data` a WHERE a.`business_function` = 'meeting_room'
                    AND a.`data_id` = r.`id` AND a.`operate` = 'approve' AND a.`authority` = 'ROLE__1');

INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1825734851692703744, '/meetingRoom', '会议室信息管理', 0, 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu_api` WHERE `path` = '/meetingRoom');
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1825734851692703745, '/meetingRoom/page', '会议室信息管理>分页查询可编辑会议室', p.`id`, 2
FROM `sys_menu_api` p
WHERE p.`path` = '/meetingRoom'
  AND NOT EXISTS (SELECT 1 FROM `sys_menu_api` c WHERE c.`path` = '/meetingRoom/page');
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1825734851692703746, '/meetingRoom/updateById', '会议室信息管理>修改会议室', p.`id`, 2
FROM `sys_menu_api` p
WHERE p.`path` = '/meetingRoom'
  AND NOT EXISTS (SELECT 1 FROM `sys_menu_api` c WHERE c.`path` = '/meetingRoom/updateById');

INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1825734851692703749, '/meetingRoomApprove', '会议室预约', 0, 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu_api` WHERE `path` = '/meetingRoomApprove');
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1825734851692703750, '/meetingRoomApprove/page', '会议室预约>分页查询可预约列表', p.`id`, 2
FROM `sys_menu_api` p
WHERE p.`path` = '/meetingRoomApprove'
  AND NOT EXISTS (SELECT 1 FROM `sys_menu_api` c WHERE c.`path` = '/meetingRoomApprove/page');
INSERT INTO `sys_menu_api` (`id`, `path`, `name`, `parent_id`, `auth_type`)
SELECT 1825734851692703751, '/meetingRoomApprove/approve', '会议室预约>预约会议室', p.`id`, 2
FROM `sys_menu_api` p
WHERE p.`path` = '/meetingRoomApprove'
  AND NOT EXISTS (SELECT 1 FROM `sys_menu_api` c WHERE c.`path` = '/meetingRoomApprove/approve');

INSERT INTO `sys_auth_menu` (`api_id`, `authority`, `description`)
SELECT m.`id`, 'ROLE__1', 'ROLE__超级管理员' FROM `sys_menu_api` m
WHERE m.`path` = '/meetingRoom'
  AND NOT EXISTS (SELECT 1 FROM `sys_auth_menu` a WHERE a.`api_id` = m.`id` AND a.`authority` = 'ROLE__1');
INSERT INTO `sys_auth_menu` (`api_id`, `authority`, `description`)
SELECT m.`id`, 'ROLE__1', 'ROLE__超级管理员' FROM `sys_menu_api` m
WHERE m.`path` = '/meetingRoomApprove'
  AND NOT EXISTS (SELECT 1 FROM `sys_auth_menu` a WHERE a.`api_id` = m.`id` AND a.`authority` = 'ROLE__1');

-- 对应的侧边路由菜单：旧示例那组里的“会议室管理”指向已废弃的 meetingRoomPrimary，这里不搬；
-- 父行 redirect 因此改指会议室基本信息管理页。ID 接第 5 段的 92 区段往下排。
INSERT INTO `sys_menu_route` (`id`, `name`, `parent_id`, `path`, `redirect`, `component`, `meta`, `icon`, `hidden`, `api_id`, `type`)
SELECT 2013, '会议管理', 0, '/meeting', '/meeting/room-info', 'layout/publics', NULL, 'el-icon-tickets', 0, NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu_route` WHERE `path` = '/meeting');
INSERT INTO `sys_menu_route` (`id`, `name`, `parent_id`, `path`, `redirect`, `component`, `meta`, `icon`, `hidden`, `api_id`, `type`)
SELECT 2014, '会议室基本信息管理', p.`id`, '/meeting/room-info', NULL, 'meeting/MeetingRoomInfo', NULL, 'el-icon-tickets', 0, NULL, 1
FROM `sys_menu_route` p
WHERE p.`path` = '/meeting'
  AND NOT EXISTS (SELECT 1 FROM `sys_menu_route` c WHERE c.`path` = '/meeting/room-info');
INSERT INTO `sys_menu_route` (`id`, `name`, `parent_id`, `path`, `redirect`, `component`, `meta`, `icon`, `hidden`, `api_id`, `type`)
SELECT 2015, '会议室预约', p.`id`, '/meeting/room-approve', NULL, 'meeting/MeetingRoomApprove', NULL, 'el-icon-tickets', 0, NULL, 1
FROM `sys_menu_route` p
WHERE p.`path` = '/meeting'
  AND NOT EXISTS (SELECT 1 FROM `sys_menu_route` c WHERE c.`path` = '/meeting/room-approve');
