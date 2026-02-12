-- ----------------------------
-- Records of sys_auth_data
-- ----------------------------
INSERT INTO sys_auth_data
VALUES (1, 'testrange', 1, 'unified', 'ROLE__1', 'ROLE__超级管理员', '2024-08-01 00:00:00', NULL);
INSERT INTO sys_auth_data
VALUES (2, 'testrange', 2, 'unified', 'ROLE__3', 'ROLE__普通角色', '2024-08-01 00:00:00', NULL);
INSERT INTO sys_auth_data
VALUES (3, 'testedit', 1, 'unified', 'ROLE__1', 'ROLE__超级管理员', '2024-08-01 00:00:00', NULL);
INSERT INTO sys_auth_data
VALUES (4, 'testauthedit', 1, 'unified', 'ROLE__1', 'ROLE__超级管理员', '2024-08-01 00:00:00', NULL);
INSERT INTO sys_auth_data
VALUES (5, 'testauthedit', 1, 'unified', 'USER_1', 'USER_系统用户', '2024-08-01 00:00:00', NULL);


-- ----------------------------
-- Records of sys_auth_menu
-- ----------------------------
INSERT INTO `sys_auth_menu`
VALUES (1, 1825734851692703744, 'ROLE__1', 'ROLE__超级管理员', '2024-08-01 00:00:00', NULL);
INSERT INTO `sys_auth_menu`
VALUES (2, 1823284778685722624, 'ROLE__1', 'ROLE__超级管理员', '2024-08-01 00:00:00', NULL);
INSERT INTO `sys_auth_menu`
VALUES (3, 1818496367898750876, 'ROLE__1', 'ROLE__超级管理员', '2024-08-01 00:00:00', NULL);
INSERT INTO `sys_auth_menu`
VALUES (4, 1818496367898750879, 'ROLE__1', 'ROLE__超级管理员', '2024-08-01 00:00:00', NULL);
INSERT INTO `sys_auth_menu`
VALUES (5, 1818496367898750979, 'ROLE__1', 'ROLE__超级管理员', '2024-08-01 00:00:00', NULL);
INSERT INTO `sys_auth_menu`
VALUES (6, 1818496367898750985, 'ROLE__1', 'ROLE__超级管理员', '2024-08-01 00:00:00', NULL);
INSERT INTO `sys_auth_menu`
VALUES (7, 1818496367898750991, 'ROLE__1', 'ROLE__超级管理员', '2024-08-01 00:00:00', NULL);
INSERT INTO `sys_auth_menu`
VALUES (8, 1825772501489168384, 'ROLE__1', 'ROLE__超级管理员', '2024-08-01 00:00:00', NULL);
INSERT INTO `sys_auth_menu`
VALUES (9, 1825734851692703749, 'ROLE__1', 'ROLE__超级管理员', '2024-08-01 00:00:00', NULL);


-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept`
VALUES (1, '根部门', 0, '2024-08-01 00:00:00', NULL);
INSERT INTO `sys_dept`
VALUES (2, '总部', 0, '2024-08-01 00:00:00', NULL);

-- ----------------------------
-- Records of sys_menu_api
-- ----------------------------
INSERT INTO `sys_menu_api`
VALUES (1818496367898750876, '/menu/api', '菜单路径相关接口', 0, NULL, 1, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1818496367898750877, '/menu/api/tree', '菜单路径相关接口>API菜单树列表', 1818496367898750876, NULL, 2,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1818496367898750878, '/menu/api/update', '菜单路径相关接口>修改API菜单', 1818496367898750876, NULL, 2,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1818496367898750879, '/menu/route', '菜单路由相关接口', 0, NULL, 1, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1818496367898750880, '/menu/route/tree', '菜单路由相关接口>路由菜单树列表', 1818496367898750879, NULL, 2,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1818496367898750979, '/rabc/dept', '部门管理', 0, NULL, 1, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1818496367898750980, '/rabc/dept/add', '部门管理>添加部门', 1818496367898750979, NULL, 2, '2024-08-01 00:00:00',
        NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1818496367898750981, '/rabc/dept/delete', '部门管理>删除部门', 1818496367898750979, NULL, 2,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1818496367898750982, '/rabc/dept/list', '部门管理>部门列表', 1818496367898750979, NULL, 2,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1818496367898750983, '/rabc/dept/page', '部门管理>部门分页', 1818496367898750979, NULL, 2,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1818496367898750984, '/rabc/dept/update', '部门管理>部门列表', 1818496367898750979, NULL, 2,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1818496367898750985, '/rabc/role', '角色管理', 0, NULL, 1, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1818496367898750986, '/rabc/role/add', '角色管理>添加角色', 1818496367898750985, NULL, 2, '2024-08-01 00:00:00',
        NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1818496367898750987, '/rabc/role/delete', '角色管理>删除角色', 1818496367898750985, NULL, 2,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1818496367898750988, '/rabc/role/list', '角色管理>角色列表', 1818496367898750985, NULL, 2,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1818496367898750989, '/rabc/role/page', '角色管理>角色分页', 1818496367898750985, NULL, 2,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1818496367898750990, '/rabc/role/update', '角色管理>修改角色', 1818496367898750985, NULL, 2,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1818496367898750991, '/rabc/user', '用户相关', 0, NULL, 1, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1818496367898750992, '/rabc/user/add', '用户相关>新增', 1818496367898750991, NULL, 2, '2024-08-01 00:00:00',
        NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1818496367898750993, '/rabc/user/delete', '用户相关>d删除用户', 1818496367898750991, NULL, 2,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1818496367898750994, '/rabc/user/list', '用户相关>用户列表', 1818496367898750991, NULL, 2,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1818496367898750995, '/rabc/user/page', '用户相关>分页', 1818496367898750991, NULL, 2, '2024-08-01 00:00:00',
        NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1818496367898750996, '/rabc/user/update', '用户相关>更新', 1818496367898750991, NULL, 2, '2024-08-01 00:00:00',
        NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1823284778685722623, '/meeting', '会议管理', 0, NULL, 1, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1823284778685722624, '/meetingRoomPrimary', '会议室管理', 1823284778685722623, NULL, 1, '2024-08-01 00:00:00',
        NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1823284778685722625, '/meetingRoomPrimary/add', '会议室管理>创建会议室', 1823284778685722624, NULL, 2,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1823284778685722626, '/meetingRoomPrimary/add2', '会议室管理>创建会议室', 1823284778685722624, NULL, 2,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1823284778685722627, '/meetingRoomPrimary/deleteById', '会议室管理>删除会议室', 1823284778685722624, NULL, 2,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1823284778685722628, '/meetingRoomPrimary/update', '会议室管理>编辑会议室主权限', 1823284778685722624, NULL, 2,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1823284778685722629, '/meetingRoomPrimary/pageRoom', '会议室管理>会议室主权限分类', 1823284778685722624, NULL,
        2, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1825734851692703744, '/meetingRoom', '会议室信息管理', 1823284778685722623, NULL, 1, '2024-08-01 00:00:00',
        NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1825734851692703745, '/meetingRoom/page', '会议室信息管理>分页查询可编辑会议室', 1825734851692703744, NULL, 2,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1825734851692703746, '/meetingRoom/updateById', '会议室信息管理>修改会议室', 1825734851692703744, NULL, 2,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1825734851692703747, '/meetingRoom/updateById2', '会议室信息管理>修改会议室2', 1825734851692703744, NULL, 2,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1825734851692703749, '/meetingRoomApprove', '会议室预约', 1823284778685722623, NULL, 1, '2024-08-01 00:00:00',
        NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1825734851692703750, '/meetingRoomApprove/page', '会议室预约>分页查询可预约列表', 1825734851692703749, NULL, 2,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1825734851692703751, '/meetingRoomApprove/approve', '会议室预约>预约会议室', 1825734851692703749, NULL, 2,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1825772501489168384, '/test', '测试', 0, NULL, 1, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1825772501489168385, '/test/checkMenu', '测试>测试菜单控制', 1825772501489168384, NULL, 2,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_api`
VALUES (1825772501489168386, '/test/checkMenuNone', '测试>测试菜单控制无权限', 1825772501489168384, NULL, 1,
        '2024-08-01 00:00:00', NULL, 0);

-- ----------------------------
-- Records of sys_menu_route
-- ----------------------------
INSERT INTO `sys_menu_route`
VALUES (2, '首页', 0, '/home', NULL, 'home', '{\"affix\": true, \"externalLinkUrl\": null, \"isExternalLinks\": false}',
        'el-icon-s-home', 0, NULL, 1, NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (3, '文档', 0, '/docs', NULL, 'docs', NULL, 'el-icon-s-order', 0, NULL, 1, NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (4, '组织机构管理', 0, '/organization', '/organization/user', 'layout/publics', NULL, 'el-icon-s-help', 0, NULL,
        1, NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (5, '部门管理', 4, '/organization/dept', NULL, 'organization/Dept', NULL, 'el-icon-s-platform', 0,
        1818496367898750979, 1, NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (6, '用户管理', 4, '/organization/user', NULL, 'organization/User', NULL, 'el-icon-s-platform', 0,
        1818496367898750991, 1, NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (7, '角色管理', 4, '/organization/role', NULL, 'organization/Role', NULL, 'el-icon-s-custom', 0,
        1818496367898750985, 1, NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (8, '个人中心', 0, '/profile', NULL, 'profile', '{\"hidden\": true}', 'el-icon-star-on', 1, NULL, 1, NULL,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (9, '菜单路由', 0, '/menu-route', '/route', 'layout/publics', NULL, 'el-icon-s-data', 0, NULL, 1, NULL,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (10, '路由菜单管理', 9, '/route', NULL, 'menu/RouteMenu', NULL, 'el-icon-s-operation', 0, 1818496367898750879, 1,
        NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (11, 'API菜单管理', 9, '/api', NULL, 'menu/ApiMenu', NULL, 'el-icon-s-operation', 0, 1818496367898750876, 1,
        NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (12, '权限框架测试', 0, '/test', '/test/datarange', 'layout/publics', NULL, 'el-icon-tickets', 0,
        1825772501489168384, 1, NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (13, '@DataRange权限范围', 12, '/test/datarange', NULL, 'test/DataRange', NULL, 'el-icon-tickets', 0, NULL, 1,
        NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (14, '@AuthMenu权限列表', 12, '/test/authmenu', NULL, 'test/AuthMenu', NULL, 'el-icon-tickets', 0, NULL, 1, NULL,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (15, '@AuthCheckDataOperation数据编辑', 12, '/test/authdataoperation', NULL, 'test/AuthCheckDataOperation', NULL,
        'el-icon-tickets', 0, NULL, 1, NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (16, '会议管理', 0, '/meeting', '/meeting/room', 'layout/publics', NULL, 'el-icon-tickets', 0, NULL, 1, NULL,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (17, '会议室管理', 16, '/meeting/room', '', 'meeting/MeetingRoomPrimary', NULL, 'el-icon-tickets', 0,
        1823284778685722624, 1, NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (18, '会议室基本信息管理', 16, '/meeting/room-info', '', 'meeting/MeetingRoomInfo', NULL, 'el-icon-tickets', 0,
        1825734851692703744, 1, NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (19, '会议室预约', 16, '/meeting/room-approve', '', 'meeting/MeetingRoomApprove', NULL, 'el-icon-tickets', 0,
        1825734851692703750, 1, NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (101, '组件模板', 0, '/example', NULL, 'example', NULL, 'el-icon-s-platform', 0, NULL, 1, NULL,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (117, '组件例子', 101, '/demo', '/demo/filtering', 'layout/publics', NULL, 'el-icon-star-on', 0, NULL, 1, NULL,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (118, '筛选组件', 117, '/demo/filtering', NULL, 'demo/filtering/index', NULL, 'el-icon-s-marketing', 0, NULL, 1,
        NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (119, '筛选组件详情', 117, '/demo/filtering-details', NULL, 'demo/filtering/component/details',
        '{\"hidden\": true}', 'el-icon-s-marketing', 0, NULL, 1, NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (120, 'v-charts 图表', 117, '/demo/v-charts', NULL, 'demo/vCharts/index', NULL, 'el-icon-data-analysis', 0, NULL,
        1, NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (121, '大数据可视化', 117, 'externalLinkUrl', NULL, 'demo/bigData',
        '{\"externalLinkUrl\": \"/big-data\", \"isExternalLinks\": true}', 'el-icon-s-data', 0, NULL, 1, NULL,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (122, '仿电视开关机', 117, '/demo/tVSwitch', NULL, 'demo/tVSwitch/index', NULL, 'el-icon-s-platform', 0, NULL, 1,
        NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (123, '富文本编辑器', 117, '/demo/mavonEditor', NULL, 'demo/mavonEditor/index', NULL, 'el-icon-edit-outline', 0,
        NULL, 1, NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (124, '瀑布屏', 117, '/demo/waterfall', NULL, 'demo/waterfall/index', NULL, 'el-icon-reading', 0, NULL, 1, NULL,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (125, '论坛评价', 117, '/demo/comment', NULL, 'demo/comment/index', NULL, 'el-icon-chat-line-square', 0, NULL, 1,
        NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (126, 'Tree 树形控件', 117, '/demo/treeControl', NULL, 'demo/treeControl/index', NULL, 'el-icon-finished', 0,
        NULL, 1, NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (127, '天地图', 117, '/demo/tianditu', NULL, 'demo/tianditu/index', NULL, 'el-icon-location-information', 0,
        NULL, 1, NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (128, '表格', 117, '/demo/table', NULL, 'demo/table/index', NULL, 'el-icon-tickets', 0, NULL, 1, NULL,
        '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (129, '表单页', 101, '/forms', '/forms/basic-form', 'layout/publics', NULL, 'el-icon-notebook-2', 0, NULL, 1,
        NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (130, '基础表单', 129, '/forms/basic-form', NULL, 'forms/basicForm/index', NULL, 'el-icon-cloudy', 0, NULL, 1,
        NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (131, '分步表单', 129, '/forms/step-form', NULL, 'forms/stepForm/index', NULL, 'el-icon-partly-cloudy', 0, NULL,
        1, NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (132, '高级表单', 129, '/forms/advanced-form', NULL, 'forms/advancedForm/index', NULL,
        'el-icon-cloudy-and-sunny', 0, NULL, 1, NULL, '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (133, '工具类集合', 0, '/tools', NULL, 'tools', NULL, 'el-icon-setting', 0, NULL, 1, NULL, '2024-08-01 00:00:00',
        NULL, 0);
INSERT INTO `sys_menu_route`
VALUES (134, '外链', 101, 'externalLinkUrl', NULL, NULL,
        '{\"externalLinkUrl\": \"https://www.baidu.com\", \"isExternalLinks\": true, \"externalLinkType\": \"open\"}',
        'el-icon-link', 0, NULL, 1, NULL, '2024-08-01 00:00:00', NULL, 0);

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role`
VALUES (1, '超级管理员', '2024-08-01 00:00:00', NULL);
INSERT INTO `sys_role`
VALUES (2, '管理员', '2024-08-01 00:00:00', NULL);
INSERT INTO `sys_role`
VALUES (3, '普通角色', '2024-08-01 00:00:00', NULL);
INSERT INTO `sys_role`
VALUES (4, '会议室管理员', '2024-08-01 00:00:00', NULL);

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user`
VALUES (1, '系统用户', '系统用户', 'admin', '12110', '593655063@qq.com', NULL, 1, '2024-08-01 00:00:00', NULL);
INSERT INTO `sys_user`
VALUES (2, '系统用户2', '系统用户2', 'admin2', NULL, NULL, NULL, 2, '2024-08-01 00:00:00', NULL);
INSERT INTO `sys_user`
VALUES (7, '测试MenuAuth', '测试MenuAuth', '测试MenuAuth', '6019521325', 'test@example.us', NULL, 2,
        '2024-08-01 00:00:00', NULL);

-- ----------------------------
-- Records of sys_user_credential
-- ----------------------------
INSERT INTO `sys_user_credential`
VALUES (1, 1, '{bcrypt}$2a$10$uCJpehGeWfvENTq.6LVrquPrGhZ9/QJXHmXmkNlu9cvnevFra1YdW', '123456', NULL, NULL, 1,
        '2024-08-01 00:00:00', NULL);

-- ----------------------------
-- Records of sys_user_dept
-- ----------------------------
INSERT INTO `sys_user_dept`
VALUES (1, 1, 1, 1, '2024-08-01 00:00:00', NULL);

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role`
VALUES (1, 1, 1, '2024-08-01 00:00:00', NULL);

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
    PRIMARY KEY (`id`) USING BTREE
)  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of testauth_annotation_business
-- ----------------------------
INSERT INTO `testauth_annotation_business`
VALUES (1, '可通过超级管理员1', '超级管理员有权限', '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `testauth_annotation_business`
VALUES (2, '可通过超级管理员2', '超级管理员有权限', '2024-08-01 00:00:00', NULL, 0);
INSERT INTO `testauth_annotation_business`
VALUES (3, '不可通过超级管理员', '超级管理员没有权限，也就是说没有对应权限的不展示到页面', '2024-08-01 00:00:00', NULL,
        0);



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
    UNIQUE KEY `name` (`name`) USING BTREE
) COMMENT='会议室';

-- 插入一条示例数据，超级管理员可管理、可修改、可预约的会议室
INSERT INTO `meeting_room`
VALUES (1, '1103会议室', 32, '11楼', '大会议室，开会使用', 1, 1, 1, '2024-08-01 00:00:00', NULL);
INSERT INTO `sys_auth_data`
VALUES (7, 'meeting_room', 1, 'edit_info', 'ROLE__1', 'ROLE__超级管理员', '2024-08-01 00:00:00', NULL);
INSERT INTO `sys_auth_data`
VALUES (8, 'meeting_room', 1, 'approve', 'ROLE__1', 'ROLE__超级管理员', '2024-08-01 00:00:00', NULL);


-- 引入SpringSessionJdbc
CREATE TABLE SPRING_SESSION
(
    PRIMARY_ID            CHAR(36) NOT NULL,
    SESSION_ID            CHAR(36) NOT NULL,
    CREATION_TIME         BIGINT   NOT NULL,
    LAST_ACCESS_TIME      BIGINT   NOT NULL,
    MAX_INACTIVE_INTERVAL INT      NOT NULL,
    EXPIRY_TIME           BIGINT   NOT NULL,
    PRINCIPAL_NAME        VARCHAR(100),
    CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
) ENGINE=INNODB ROW_FORMAT=DYNAMIC;
CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);
CREATE TABLE SPRING_SESSION_ATTRIBUTES
(
    SESSION_PRIMARY_ID CHAR(36)     NOT NULL,
    ATTRIBUTE_NAME     VARCHAR(200) NOT NULL,
    ATTRIBUTE_BYTES    BLOB         NOT NULL,
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION (PRIMARY_ID) ON DELETE CASCADE
) ENGINE=INNODB ROW_FORMAT=DYNAMIC;