--  ------------------------------------ 权限体系相关 -----------------------------------
drop table if exists sys_user;
create table sys_user
(
    id          bigint auto_increment primary key comment '用户id',
    name        varchar(100) comment '用户姓名',
    create_time datetime default now() comment '创建时间',
    update_time datetime default now() on update now() comment '修改时间'
) comment '用户表';
drop table if exists sys_user_info;
create table sys_user_info
(
    id          bigint auto_increment primary key comment '主键id',
    user_id     bigint not null comment '用户id',
    nick_name   varchar(100) comment '用户昵称',
    username    varchar(100) comment '用户名',
    password    varchar(100) comment '密码',
    phone       varchar(20) comment '手机号',
    create_time datetime default now() comment '创建时间',
    update_time datetime default now() on update now() comment '修改时间',
    unique (username),
    unique (phone)
) comment '用户信息表';
drop table if exists sys_dept;
create table sys_dept
(
    id          bigint auto_increment primary key comment '部门id',
    name        varchar(100) not null comment '部门名',
    parent_id   bigint       not null comment '部门，如果部门为0 则代表根部门',
    create_time datetime default now() comment '创建时间',
    update_time datetime default now() on update now() comment '修改时间'
) comment '部门表';

drop table if exists sys_position;
create table sys_position
(
    id          bigint auto_increment primary key comment '岗位id',
    name        varchar(100) not null comment '岗位名',
    create_time datetime default now() comment '创建时间',
    update_time datetime default now() on update now() comment '修改时间'
) comment '岗位表';
drop table if exists sys_user_dept;
create table sys_user_dept
(
    id          bigint auto_increment primary key comment '主键',
    user_id     bigint             not null comment '用户id',
    dept_id     bigint             not null comment '部门id',
    is_primary  tinyint  default 0 not null comment '是否主要归属部门 0-非主要部门 1-主要部门',
    create_time datetime default now() comment '创建时间',
    update_time datetime default now() on update now() comment '修改时间',
    index (user_id, dept_id)
) comment '用户部门关联表';
drop table if exists sys_user_position;
create table sys_user_position
(
    id          bigint auto_increment primary key comment '主键',
    user_id     bigint not null comment '用户id',
    position_id bigint not null comment '岗位id',
    create_time datetime default now() comment '创建时间',
    update_time datetime default now() on update now() comment '修改时间',
    index (user_id, position_id)
) comment '用户岗位关联表';

drop table if exists sys_group;
create table sys_group
(
    id          bigint auto_increment primary key comment '用户组id',
    name        varchar(100) not null comment '用户组名',
    create_time datetime default now() comment '创建时间',
    update_time datetime default now() on update now() comment '修改时间'
) comment '角色组表';

drop table if exists sys_role;
create table sys_role
(
    id          bigint auto_increment primary key comment '角色id',
    name        varchar(100) not null comment '角色名',
    create_time datetime default now() comment '创建时间',
    update_time datetime default now() on update now() comment '修改时间'
) comment '角色表';
drop table if exists sys_permission;
create table sys_permission
(
    id          bigint auto_increment primary key comment '权限id',
    name        varchar(100) not null comment '权限名',
    create_time datetime default now() comment '创建时间',
    update_time datetime default now() on update now() comment '修改时间'
) comment '权限表';

drop table if exists sys_user_group;
create table sys_user_group
(
    id          bigint auto_increment primary key comment '主键id',
    user_id     bigint not null comment '用户id',
    group_id    bigint not null comment '角色组id',
    create_time datetime default now() comment '创建时间',
    update_time datetime default now() on update now() comment '修改时间',
    index (user_id, group_id)
) comment '用户角色组关联表';
drop table if exists sys_user_role;
create table sys_user_role
(
    id          bigint auto_increment primary key comment '主键id',
    user_id     bigint not null comment '用户id',
    role_id     bigint not null comment '角色id',
    create_time datetime default now() comment '创建时间',
    update_time datetime default now() on update now() comment '修改时间',
    index (user_id, role_id)
);
drop table if exists sys_group_role;
create table sys_group_role
(
    id          bigint auto_increment primary key comment '主键id',
    group_id    bigint not null comment '角色组id',
    role_id     bigint not null comment '角色id',
    create_time datetime default now() comment '创建时间',
    update_time datetime default now() on update now() comment '修改时间',
    index (role_id, group_id)
) comment '角色组角色关联表';
drop table if exists sys_role_permission;
create table sys_role_permission
(
    id            bigint auto_increment primary key comment '主键id',
    role_id       bigint not null comment '角色id',
    permission_id bigint not null comment '权限id',
    create_time   datetime default now() comment '创建时间',
    update_time   datetime default now() on update now() comment '修改时间',
    index (role_id, permission_id)
) comment '角色权限关联表';



--  ------------------------------------ 应用相关 -----------------------------------

-- 应用权限表
drop table if exists sys_application_auth;
create table sys_application_auth
(
    id          bigint auto_increment primary key comment '主键ID',
    module      varchar(64)  not null comment '模块',
    business_id bigint       not null comment '业务ID 应用中业务的主键ID',
    operate     varchar(64)  not null default 'unified' comment '操作类型 unified modify delete 等,应用可以自定义操作名称',
    authority   varchar(100) not null comment '权限字符串',
    description varchar(200) not null comment '权限描述',
    create_time datetime              default now() comment '创建时间',
    update_time datetime              default now() on update now() comment '修改时间'
) comment '应用权限表';
-- 校验业务权限时用到此索引
alter table sys_application_auth
    add index (business_id, module, operate, authority);


-- 菜单表
drop table if exists sys_menu;
create table sys_menu
(
    id          bigint auto_increment primary key comment '主键id',
    name        varchar(200)       not null comment '菜单名',
    parent_id   bigint   default 0 not null comment '父id',
    url         varchar(512) comment '链接或路径',
    image       varchar(512) comment '图片地址',
    type        tinyint  default 1 not null comment '类型： 1-菜单 2-按钮 3-外部链接',
    auth_type   tinyint  default 1 not null comment '权限类型 1-基于父菜单权限 2-基于本身权限',
    description text comment '描述',
    create_time datetime default now() comment '创建时间',
    update_time datetime default now() on update now() comment '修改时间'
) comment '菜单表';


-- 菜单权限表
drop table if exists sys_menu_auth;
create table sys_menu_auth
(
    id          bigint auto_increment primary key comment '主键id',
    menu_id     bigint       not null comment '菜单id',
    authority   varchar(100) not null comment '权限字符串',
    description varchar(200) not null comment '权限描述',
    create_time datetime default now() comment '创建时间',
    update_time datetime default now() on update now() comment '修改时间'
) comment '菜单权限表';



-- 测试sql
insert into sys_user(name) values ('system');
insert into sys_user_info(user_id, nick_name, username, password, phone) values (1,'后台管理用户','admin','admin123456','18508202016');
insert into sys_dept(name,parent_id) values ('根机构',0);
insert into sys_user_dept(user_id, dept_id) values (1,1);
insert into sys_role(name) values ('超级管理员');
insert into sys_user_role(user_id, role_id) VALUES (1,1);


insert into sys_application_auth(module, business_id, authority, description) VALUES ('test', '1', 'R_1', 'R_超级管理员');
insert into sys_application_auth(module, business_id, authority, description) VALUES ('test', '1', 'D_1', 'D_总部');
insert into sys_menu(id, name, parent_id, url, image, description) VALUES (1, '测试菜单1', 0, '/test/testMenuAuth', null, null);
insert into sys_menu(id, name, parent_id, url, image, description) VALUES (2, '测试菜单2', 1, '/test/1', null, null);
insert into sys_menu(id, name, parent_id, url, image, description) VALUES (3, '测试菜单3', 2, '/test/2', null, null);
insert into sys_menu(id, name, parent_id,auth_type, url, image, description) VALUES (4, '测试菜单4', 3,2, '/test/3', null, null);
insert into sys_menu(id, name, parent_id, url, image, description) VALUES (5, '测试菜单5', 3, '/test/4', null, null);
insert into sys_menu(id, name, parent_id, url, image, description) VALUES (6, '测试菜单6', 2, '/test/5', null, null);
insert into sys_menu(id, name, parent_id, url, image, description) VALUES (7, '测试菜单7', 6, '/test/6', null, null);
insert into sys_menu(id, name, parent_id, url, image, description) VALUES (8, '测试菜单8', 6, '/test/7', null, null);
insert into sys_menu(id, name, parent_id, url, image, description) VALUES (9, '测试菜单9', 0, '/test/8', null, null);
insert into sys_menu(id, name, parent_id, url, image, description) VALUES (10, '测试菜单10', 9, '/test/9', null, null);
insert into sys_menu_auth(menu_id, authority, description) VALUES (4, 'R_1', 'R_超级管理员');
insert into sys_menu_auth(menu_id, authority, description) VALUES (4, 'D_1', 'D_根机构');


insert into sys_menu(id, name, parent_id,auth_type, url, image, description) VALUES (11, '日志功能', 0,2, '/sysLog', null, null);
insert into sys_menu(id, name, parent_id, url, image, description) VALUES (12, '分页列表', 11, '/sysLog/pageQuery', null, null);
insert into sys_menu(id, name, parent_id, url, image, description) VALUES (13, '日志列表', 11, '/sysLog/findList', null, null);
insert into sys_menu(id, name, parent_id, url, image, description) VALUES (14, '日志详情', 11, '/sysLog/findById', null, null);
insert into sys_menu_auth(menu_id, authority, description) VALUES (11,'R_1','R_超级管理员');
insert into sys_menu_auth(menu_id, authority, description) VALUES (11,'D_1','D_根机构');