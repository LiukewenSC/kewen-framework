# Tasks

## 1. 声明解析与配置缓存（auth-core）

- [x] 1.1 在 `data/range` 包内新增声明缓存组件：遍历 MyBatis `Configuration.getMapperRegistry().getMappers()`，读取 Mapper 接口方法上的 `@AuthDataRange`，以 `接口全限定名.方法名` 为键缓存静态声明模板（业务功能、操作类型、目标表、别名、主键列、匹配方式），缓存中不含任何用户相关数据；补 JUnit4 测试（参照 `MybatisAuthSqlInjectTest` 的写法，用 `Configuration` + `addMapper` 注册一个带 `@Select` 的测试接口），断言命中与不命中两种解析结果
- [x] 1.2 把缓存中的声明模板定义为执行期不可变：注入前复制成每次新建的 `AuthRange` 再填入当前权限集合；测试断言同一语句连续两次以不同权限集合执行时，第二次不受第一次影响（防止把共享缓存对象当作用域变量）
- [x] 1.3 更新 `AuthDataRange` 的 javadoc，说明声明位置是 MyBatis Mapper 接口方法、一条语句绑定一个权限槽位、MyBatis-Plus 内建方法不提供挂载点；验证方式为 `mvn -pl qy-auth/auth-core javadoc` 或编译无告警且文档描述与 1.1 的实现一致

## 2. 启动期校验

- [x] 2.1 新增校验器并以 `SmartInitializingSingleton.afterSingletonsInstantiated()` 挂载（不挂在 `SqlSessionFactory` bean 上，也不放到 `ApplicationRunner`），复用 1.1 的遍历结果执行三项硬校验：解析不到映射语句、语句不是 SELECT、语句 `getCache()` 非空（二级缓存与范围过滤互斥）；分别构造这三类错误声明，验证应用启动失败且异常信息指名具体语句
- [x] 2.2 增加目标表静态校验：仅对 `RawSqlSource`（实际在 `org.apache.ibatis.scripting.defaults` 包）的语句取 `ms.getBoundSql(null)` 的 SQL 文本、复用 `SqlAuthInject` 现有表匹配逻辑（新增只读的 `validateTargetTable`）判定目标表是否存在于该语句，不匹配则启动失败；`DynamicSqlSource` 与 `ProviderSqlSource` 必须跳过。验证方式：静态语句写错表名→启动失败；同一语句改成 `<script>` 包裹的动态形式→启动不报错
- [x] 2.3 增加迁移守卫：遍历容器内 bean 的原始类（用 `ClassUtils.getUserClass` 避开 CGLIB 代理），发现 `@AuthDataRange` 出现在不属于 MyBatis mapper 接口的方法上时启动失败并提示迁移到 mapper 方法；验证方式：把一个注解放回任一业务 bean 并注册为 BeanDefinition，校验器抛异常
- [x] 2.4 同步 `README_Auth.md`（`@AuthDataRange` 用法与"在 Controller 方法上加注解"的说明）和 `README.md` 中的注解示例，改为 Mapper 方法声明并列出 2.1/2.2 的启动失败条件

## 3. 执行路径改为按语句身份决策

- [x] 3.1 `MybatisDataRangeInterceptor.intercept` 改为按语句 id 查 1.1 的缓存；未命中、或 `SqlCommandType != SELECT` 时原样 `proceed`；命中时用 1.2 的副本机制构造 `AuthRange` 后调用现有 `SqlAuthInject`。为可测性允许直接注入 `SqlAuthInject`，保留现有懒加载分支。取 MappedStatement 的实际做法：MyBatis 3.5.16 的 `Plugin` 只有 `wrap` 没有 unwrap，且 JDK 17 禁止反射读 `java.lang.reflect.Proxy.h`（实测 InaccessibleObjectException），因此用公开的 `Proxy.isProxyClass` + `Proxy.getInvocationHandler` 拿到 `Plugin` 处理器，再用 MetaObject 读它的 `target` 字段逐层下钻，最后读 `delegate.mappedStatement`；解不出语句身份时抛异常而不是放行
- [x] 3.2 为 3.1 补测试：用 `Configuration` 注册两个 mapper 方法（一个声明、一个未声明），断言只有声明过的那条被改写、且同一请求内先执行声明查询再执行未声明查询时后者不被注入（旧机制 A1 的回归点）；并补一个经过两层 `Plugin.wrap` 的 `RoutingStatementHandler`，断言仍能取到 MappedStatement
- [x] 3.3 删除 `DataRangeContext`、`DataRangeAspect` 以及 `auth-core-spring-boot-starter` 中 `AuthCoreConfig` 里的切面 bean 定义与相关 javadoc 引用；验证方式为全仓 `@AuthDataRange`/`DataRangeContext`/`DataRangeAspect` 的 Java 引用搜索只剩 mapper 位置，且 `mvn -pl qy-auth/auth-core,qy-auth/auth-core-spring-boot-starter -am test` 通过

## 4. 示例工程迁移到 mapper 方法

- [x] 4.1 在 `MeetingRoomMpMapper` 新增四个手写语句：可编辑列表、可编辑计数、可审批列表、可审批计数（列表带 `limit`/`offset`，计数返回 `long`），列表语句各自声明对应的 `operate`（`edit_info` / `approve`）；计数语句同样声明，保证 total 与行集口径一致
- [x] 4.2 改 `MeetingRoomController.page` 与 `MeetingRoomApproveController.pageapproves`：移除方法上的 `@AuthDataRange`，改调 4.1 的 mapper 方法（经 Service 暴露）并用 `PageResult.of` 手工组装（`MybatisPageExecutor.pageAndConvert` 的入参绑定 MyBatis-Plus `IService`/`Page`，此路径不再适用）；验证方式为以 `h2` 起 sample 打接口，四个 `operate` 槽位各自生成自己的过滤条件，行集与 total 同口径
- [x] 4.3 在 `TestauthAnnotationBusinessMpMapper` 新增声明了 `testrange` 槽位的列表语句，改 `TestAuthAnnotationController.testDataRange` / `pageDataRange` 调用它（分页那处同样手工组装）；验证方式为原有 `Assert.isTrue(list.size()==1, ...)` 断言在 `h2` profile 下仍然成立

## 5. 装配补齐（auth-core-spring-boot-starter）

- [x] 5.1 实施时定位修正：NPE 的根因不在 `AuthRbacConfig.annotationAuthHandler`（`RbacAnnotationAuthHandler` 只管菜单、没有 `jdbcAuthDataPersistent` 字段），而在 `AuthCoreConfig.jdbcAuthDataHandler()` 建 `JdbcAuthDataHandler` 时没 set 持久化组件。改为把 `JdbcAuthDataPersistent` 作为该 bean 方法参数注入并调用 setter，不做字段级 `@Autowired`；验证方式为 `/meetingRoom/page` 不再返回 `this.jdbcAuthDataPersistent is null` 的 500。明确不修改 `JdbcAuthDataPersistent` 内的 SQL 拼接方式

## 6. 集成验证

- [x] 6.1 以 `h2` profile 启动 `auth-rbac-boot-sample`，依次打 `/test/dataRange`、`/test/pageDataRange`、`/meetingRoom/page`、`/meetingRoomApprove/page`、`/test/checkMenu`，确认启动期校验无报错、四个受控接口返回按权限过滤的数据、`/meetingRoom/page` 返回 200 且权限数据被填充
- [x] 6.2 验证跨语句隔离：在 debug 日志下确认一次 `/meetingRoom/page` 请求里除声明过的两条语句外，其它 SQL（含 `fillDataAuths` 走的权限表查询）未被追加范围条件；同时确认 `/test/checkMenuNone` 仍按设计返回 500 权限拒绝而不是被本次改造影响。实测补充：`fillDataAuths` 走 `JdbcTemplate` 而非映射语句，本就不在拦截器路径内；被隔离的是 MyBatis-Plus 的 `SysUserMpMapper.selectList`、`SysMenuApiMpMapper.selectList`、`sys_auth_menu`、`getAuthInfo` 等一批同请求内的语句，日志显示它们全部按"未声明→不增强"原样执行
