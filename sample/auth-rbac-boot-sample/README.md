# auth-rbac-boot-sample

## 演示什么

默认 RBAC 实现的完整示例：用户-部门-角色-菜单权限、数据范围、行级数据操作权限与部门继承，都用会议室管理这个案例串起来。默认 H2 内存库，端口 8081。

本示例**没有接入 Spring Security**：身份由请求头 `X-Demo-User` 声明，不带时回落到 `admin`。权限仍然按 `sys_user` / `sys_role` / `sys_dept` 实时装载，因此换身份就能看到不同的判权结果。

## 零配置启动

```bash
cd sample/auth-rbac-boot-sample
mvn -q -DskipTests package
java -jar target/auth-rbac-boot-sample-0.0.1-exec.jar
```

> 请用打包后的 exec jar 启动。当前工程用 `mvn spring-boot:run` 时，权限实体的类路径扫描可能不完整，表现为 `权限标记错误: ROLE`；`java -jar` 是验证过的方式。

默认 `h2` profile，启动时自动执行 `relation/auth-rbac/auth-rbac.sql` 与本模块的 `sql/schema.sql`、`sql/seed-data.sql`。H2 控制台：<http://localhost:8081/h2-console>，JDBC URL 用 `application.yml` 里的 `jdbc:h2:mem:auth_sample;...`。

接自己的 MySQL 时用 `--spring.profiles.active=mysql`，先手工按顺序执行：

1. `relation/auth-rbac/auth-rbac.sql`
2. 本模块 `sql/schema.sql`
3. 本模块 `sql/seed-data.sql`

MySQL 会话表执行 [relation/login-security/spring-session-mysql.sql](../../relation/login-security/spring-session-mysql.sql)，并把 `spring.session.jdbc.initialize-schema` 设为 `never`；H2 用 `embedded` 自动建表。

脚本执行完后，用 `mysql` profile 启动同一个 exec jar；数据源用命令行覆盖：

```bash
java -jar target/auth-rbac-boot-sample-0.0.1-exec.jar \
  --spring.profiles.active=mysql \
  --spring.datasource.url='jdbc:mysql://localhost:3306/your_database?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai' \
  --spring.datasource.username=your_username \
  --spring.datasource.password=your_password
```

MySQL 侧已验证到“建库 → 四个脚本 → 启动 → `/meetingRoom/page` 与 `/deptInherit/rooms` 返回符合预期”；示例不负责创建数据库。

## 身份怎么来

```bash
curl -H "X-Demo-User: admin" http://localhost:8081/meetingRoom/page
```

| 身份 | 所属部门 | 角色 | 用途 |
| --- | --- | --- | --- |
| `admin`（缺省） | 根部门(1) | 超级管理员(`ROLE__1`) | 拥有种子里的会议室编辑/预约权限 |
| `admin2` | 总部(2) | 无 | 部门继承的“本部门不放行”对照 |
| `subordinate` | 研发部(3，总部子部门) | 无 | 部门继承的“子部门放行”对照 |
| `outsider` | 外部合作部(4) | 无 | 与总部无层级关系的对照 |

请求头只存在于示例；框架本身不读任何身份头。

## 数据与种子

| 脚本 | 内容 |
| --- | --- |
| [relation/auth-rbac/auth-rbac.sql](../../relation/auth-rbac/auth-rbac.sql) | 权限框架 10 张表 + `view_menu_api_auth` 视图 |
| [relation/basic-support/basic-support.sql](../../relation/basic-support/basic-support.sql) | 文件存储与请求日志表，本示例可选 |
| `sql/schema.sql` | 示例业务表 `meeting_room`、`testauth_annotation_business` |
| `sql/seed-data.sql` | 上述两表与框架 10 张表的种子数据 |

种子数据包含部门树 `1 根部门 -> 2 总部 -> 3 研发部`、独立根 `4 外部合作部`，4 个角色，5 个用户，以及 1 间会议室（id=1）的编辑/预约权限。

## 使用案例

### 可编辑会议室列表

`GET /meetingRoom/page`，类上 `@AuthMenu` 控制接口访问，方法内的查询由 `MeetingRoomMpMapper.selectEditAblePage` 上的 `@AuthDataRange` 过滤 `edit_info` 槽位。

```bash
curl -H "X-Demo-User: admin" "http://localhost:8081/meetingRoom/page?page=1&size=10"
```

返回的每条会议室会通过 `AuthDataAdaptor.fillDataAuths` 回填 `appointment` 槽位的权限对象。

### 编辑会议室

`POST /meetingRoom/updateById`：

```java
@PostMapping("/updateById")
@Transactional(rollbackFor = Exception.class)
@AuthDataOperation(businessFunction = BUSINESS_FUNCTION, operate = OPERATE_EDIT_INFO)
public Result updateByIdMethod(@RequestBody MeetingRoomUpdateReq entity) {
    meetingRoomService.updateById(entity);
    authDataAdaptor.editDataAuths(BUSINESS_FUNCTION, entity.getId(),
            OPERATE_APPROVE, entity.getAuthObject());
    return Result.success(entity);
}
```

```bash
curl -X POST http://localhost:8081/meetingRoom/updateById \
  -H "Content-Type: application/json" -H "X-Demo-User: admin" \
  -d '{"id":1,"name":"1103会议室","authObject":{"roles":[{"id":4,"name":"会议室管理员"}]}}'
```

- `@AuthDataOperation(edit_info)` 先校验当前人能否编辑这条数据。
- 业务更新后，`AuthDataAdaptor.editDataAuths` 把请求里的 `authObject` 写到同一间会议室的 `appointment` 槽位。
- 把预约权限改成只有角色 4 后，`admin` 再调用预约接口就会失去权限；这正是“编辑权限”和“预约权限”分开的例子。

### 可预约列表与预约

`GET /meetingRoomApprove/page` 走 `selectApproveAblePage` 的 `appointment` 数据范围；`POST /meetingRoomApprove/approve` 走 `@AuthDataOperation(appointment)`。

```bash
curl -H "X-Demo-User: admin" "http://localhost:8081/meetingRoomApprove/page?page=1&size=10"

curl -X POST http://localhost:8081/meetingRoomApprove/approve \
  -H "Content-Type: application/json" -H "X-Demo-User: admin" \
  -d '{"id":1,"time":"2024-11-10"}'
```

没有预约权限时返回 HTTP 403 + `code=403` + `errorType=AUTHORIZATION`；请求体不实现 `DataIdCarrier` 或 ID 为空时返回 HTTP 400 + `code=400` + `errorType=USAGE`。

### 部门继承权限

`GET /deptInherit/rooms`，类上 `@AuthMenu` 批量管辖；菜单上只挂了一条 `RELATIONDEPT__2`（总部）。

```bash
curl -H "X-Demo-User: subordinate" http://localhost:8081/deptInherit/rooms   # 200
curl -H "X-Demo-User: admin2"      http://localhost:8081/deptInherit/rooms   # 403
curl -H "X-Demo-User: outsider"    http://localhost:8081/deptInherit/rooms   # 403
```

| 身份 | 与总部(2)的关系 | 期望 |
| --- | --- | --- |
| `subordinate` | 子部门(3) | 200 放行 |
| `admin2` | 本人就在总部 | 403 拒绝 |
| `outsider` | 无层级关系 | 403 拒绝 |
| 不带请求头 | 根部门(1) | 403 拒绝 |

`RELATIONDEPT__2` 只覆盖总部的严格子孙部门，不含总部自己。要放行 `admin2`，需要另外配 `DEPT__2`；自动化用例 `DeptInheritanceSampleTest#grantingTheDeptsOwnFlagAdmitsItsOwnMember` 在运行时补上这条并验证。

### 调试端点

| 请求 | 用途 |
| --- | --- |
| `GET /test/dataRange` | 单表数据范围查询 |
| `GET /test/pageDataRange` | 分页数据范围（列表与计数各一条语句） |
| `POST /test/dataEdit` | 单条 `@AuthDataOperation` |
| `POST /test/dataEditBatch` | 批量 `@AuthDataOperation`，全部命中才放行 |
| `GET /test/checkMenu` / `checkMenuNone` | 菜单鉴权正反例 |

## 菜单注册与授权

启动时 `MenuApiGeneratorProcessor` 把带 `@AuthMenu` 的端点写进 `sys_menu_api`，只补缺失行，不覆写已有行。类上有 `@RequestMapping` 时生成虚拟父菜单，方法行通过 `parent_id` 挂在下面。

授权在 `sys_auth_menu` 中配置，`api_id` 指向 `sys_menu_api.id`。示例种子已经给出固定 ID 与授权；自己新增接口时，按路径找到行再插入授权：

```sql
INSERT INTO sys_auth_menu (api_id, authority, description)
SELECT id, 'ROLE__1', 'ROLE__超级管理员'
FROM sys_menu_api
WHERE path = '/your/api/path';
```

菜单行由路径定位，更新菜单不能改路径；带路径变量的端点会被恒拒绝。完整规则见 [auth/docs/annotations.md](../../auth/docs/annotations.md)。

## 权限粒度

- 数据创建、删除由菜单权限控制；当前示例没有创建/删除端点。
- 数据编辑由 `@AuthDataOperation(edit_info)` 控制。
- 数据使用（预约）由 `@AuthDataOperation(appointment)` 控制。
- 主权限管理应单独用菜单权限控制，避免把“主权限”交给普通数据操作。

## 不覆盖什么

- 不演示登录 / SSO；身份是请求头，登录示例见 [login-boot-sample](../login-boot-sample/README.md)。
- 不演示创建/删除会议室；示例只保留编辑、预约与数据范围。
- 不覆盖 MySQL 上的实际执行；脚本用 H2 实测，MySQL 需自行跑一遍。
- 不覆盖前端页面；示例只提供 JSON 接口。

## 相关文档

- [auth 总览](../../auth/README.md)
- [权限注解与权限编辑](../../auth/docs/annotations.md)
- [RBAC 模型与扩展](../../auth/docs/rbac-and-extension.md)
- [auth-rbac-spring-boot-starter](../../auth/auth-rbac-spring-boot-starter/README.md)
- [auth-core 验证示例](../auth-core-boot-sample/README.md)
- [项目根 README](../../README.md)
