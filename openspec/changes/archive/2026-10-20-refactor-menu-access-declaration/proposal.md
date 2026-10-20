# Proposal

## Why

`@AuthMenu` 同时承担了三件不相干的事：它是"这个端点受菜单管辖"的开关、是菜单树标签的文案来源（`name()`）、又靠 `sys_menu_api.path` 间接充当身份。`name()` 这个字段最坏的地方在于它看起来像权限标识——`RbacDeptController` 的 update 挂着"部门列表"就是这种误解留下的产物，而实际上鉴权与授权全部只认 path 对应的行，`name()` 从不出现在任何判定里（全仓无以 name 查询菜单的代码）。

同时，`updateMenu` 走 `updateById(req)` 且 `MenuApiSaveReq extends SysMenuApi`，请求方可以直接改写 `path`——也就是改写鉴权身份本身。身份由代码与建库脚本决定，却对请求开放写入，是唯一能让"代码侧注册表"与"库里的行"悄悄对不上的通道。

## What Changes

- **BREAKING** `@AuthMenu` 去掉 `name()`，变成无属性的管辖标记；只回答"管辖不管辖"。
- 新增 `@AuthMenuDoc`，专门承载显示文案，并继续充当菜单树标签的来源（类上给分组行命名，方法上给端点行命名）。标签缺失时回落到类简单名/方法名，与现状一致。
- **BREAKING** `updateMenu` 不再允许改写 `path`：更新语句用 SET 白名单，只写 `name`、`remark`、`auth_type`、`parent_id`。请求里带 `path` 不生效（不报错、不改写）。
- 类级 `@AuthMenu` 的既有语义写进规格：标注在类上 = 该 Controller 的**全部**端点受管辖并全部注册成菜单行；方法级标注是单个端点的声明；两者叠加；都没有 = 不受管辖。
- 全部标注点按新形态改写（36 处），含代码生成模板。
- 顺手改掉两处纯文案残留：`RbacDeptController` update 上的"部门列表"、`sample/auth-rbac-boot-sample/sql/seed-data.sql` 中 `/rbac/dept/update` 行的同名残留。

不在范围内（本轮明确不动）：鉴权身份从 `sys_menu_api.path` 换成端点身份、启动期校验与孤儿行检测、菜单同步的 `new Thread` 异步形态、生成器"只新增不覆写已有行"的自动创建行为、`@AuthDataOperation`、任何 DDL 变更。

## Capabilities

### New Capabilities

- `auth/menu-access`: 菜单鉴权的声明形态与管辖边界——管辖开关由谁给出、开关覆盖哪些端点、菜单标签从何而来、鉴权身份 `path` 的写入归属。

### Modified Capabilities

无。`auth/data-range` 的需求不变（本轮不碰数据范围，`@AuthDataOperation` 亦不动）。

## Impact

- `qy-auth/auth-core`：`AuthMenu`（去 `name()`）、新增 `AuthMenuDoc`、`MenuApiGeneratorProcessor`（`name()` 的两个消费点 `:132`/`:157` 改读 `@AuthMenuDoc`）、`MenuAccessInterceptor`（管辖判定不变，仅注释与规格对齐）。
- `qy-auth/auth-rbac`：`DefaultSysAuthMenuComposite.updateMenu` 改为 SET 白名单；`RbacDeptController` 等 5 个框架控制器的标注改写。
- `qy-auth/auth-rbac-spring-boot-starter` / `auth-core-spring-boot-starter`：无 bean 变更。
- `sample/auth-rbac-boot-sample`：3 个主控制器 + 4 个测试控制器标注改写；`sql/seed-data.sql` 两处菜单名。
- `other/code-generator/src/main/resources/templates/controller.java.vm:50`：生成模板拆成两个注解。
- 文档：`README.md`、`README_Auth.md`、`sample/auth-rbac-boot-sample/README.md`。`.qoder/repowiki` 为生成索引，重生成而非手改。
- 下游：`kewen-web-admin`（独立仓库）的 API 菜单表单里 `path` 输入框失去作用；本仓只改后端契约。
- 数据库：无 schema 变更，仅种子数据文案。
