# Proposal

## Why

数据范围权限（`@AuthDataRange`）现在的三个粒度是错配的：注解声明在方法上、生效上下文在线程上、SQL 注入落在语句上。结果是两种静默失效——一处注解会把权限条件注入该调用链上**每一条** SQL（未指定 `table` 时通配命中任意主表），而嵌套调用与线程池会让上下文被提前清空或长期残留，前者等于放开过滤，后者等于把上一个人的权限范围带给下一个请求。这两个方向的危害都是"越权/放开"而不是"少显示"，且现网不会报错。

## What Changes

- **BREAKING** `@AuthDataRange` 的声明位置从 Controller/Service 方法下沉到 MyBatis Mapper 接口方法；权限条件只注入该注解所属的那一条语句。
- **BREAKING** 删除线程上下文 `DataRangeContext` 与切面 `DataRangeAspect`（含 starter 中的 bean 定义），不再存在跨语句共享的数据权限上下文。
- 一个 Mapper 方法绑定一个权限槽位（`businessFunction` + `operate`），静态写在注解上；同一张表的多个 operate 通过多个 Mapper 方法表达。
- 新增启动期配置校验：注解所在方法解析不到映射语句、语句不是 SELECT、或语句挂了二级缓存时，应用启动失败。
- 新增与二级缓存的互斥约束：受数据范围管辖的语句不允许启用二级缓存。
- 仅支持原生 MyBatis。MyBatis-Plus 由 `BaseMapper` 继承而来的内建方法不提供挂载点，不做适配。
- 示例工程 4 个使用点改为手写 Mapper 方法（分页的两处各写列表语句与计数语句）。
- 补上 `AuthRbacConfig` 构造权限处理器时漏注入的 `jdbcAuthDataPersistent`，使示例 `/meetingRoom/page` 不再 500（仅补注入，不改该类中的 SQL 拼接）。

## Capabilities

### New Capabilities

- `auth/data-range`: 数据范围查询权限的声明单位与作用域边界、生效前提（opt-in、仅 SELECT）、启动期校验要求，以及与二级缓存的互斥关系。

### Modified Capabilities

无。`sample/demo-database-profiles` 等既有能力的 requirement 不变——示例改为手写 Mapper 方法属于实现调整，其对外可观察行为（按登录人权限过滤查询结果）与 profile/H2 约定均不受影响。

## Impact

**框架代码**

- `../../../../auth`：`AuthDataRange`（注解 `@Target` 语义与文档）、`data/range/` 下的 `MybatisDataRangeInterceptor`、`SqlAuthInject`、`AuthRange` 保持职责但改造入口；`DataRangeContext`、`DataRangeAspect` 删除。
- `../../../../auth`：`AuthCoreConfig` 中切面 bean 移除，新增启动期校验的挂载。
- `../../../../auth`：`AuthRbacConfig.annotationAuthHandler` 补一行 setter 调用。
- 依赖关系不变：`auth-core` 本就只依赖 `org.mybatis:mybatis` 与 `jsqlparser`，不引入 MyBatis-Plus。

**示例代码**

- `sample/auth-rbac-boot-sample`：`MeetingRoomController.page`、`MeetingRoomApproveController.pageapproves`、`TestAuthAnnotationController.testDataRange` / `pageDataRange` 四处，以及 `MeetingRoomMpMapper`、`TestauthAnnotationBusinessMpMapper` 需要新增手写语句；分页不再复用 `MybatisPageExecutor`（其入参绑定 MyBatis-Plus 的 `IService`/`Page`）。

**外部契约**

- 已有业务代码把 `@AuthDataRange` 标在 Controller/Service 上的用法会静默失效（语句上无注解即不过滤），启动期校验会把"注解标到解析不出 SELECT 语句的位置"变成显式失败。使用方式必须迁移。

**明确不在本 change 范围内**

- 一条 SQL 中多张表使用不同 operate 的场景（一个语句一个槽位的直接代价）。
- MyBatis-Plus 内建方法的权限挂载。
- `boot/tenant-spring-boot-starter` 中同型的 `TenantContext`（`InheritableThreadLocal` + set/clear）。
- `JdbcAuthDataPersistent` 的 SQL 拼接注入（单独暂缓，见既有记录）。
