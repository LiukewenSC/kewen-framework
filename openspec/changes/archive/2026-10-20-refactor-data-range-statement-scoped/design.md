# Design

## Context

`qy-auth/auth-core` 里的数据权限由三部分组成：注解 `@AuthDataRange`（方法级声明）、`DataRangeAspect` + `DataRangeContext`（线程级上下文）、`MybatisDataRangeInterceptor` + `SqlAuthInject`（语句级注入）。动机与危害见 proposal.md - Why；行为契约见 specs/auth/data-range/spec.md。

设计与实现要遵守的现状约束：

- `auth-core` 的 pom 只依赖 `org.mybatis:mybatis` 与 `jsqlparser`，**不依赖 MyBatis-Plus**。本设计不改变这一依赖关系。
- 映射语句 id 的构造规则是 `接口全限定名.方法名`。MyBatis 的 `MapperMethod.SqlCommand` 先按具体 mapper 接口名解析；同名重载在注册阶段就会因语句 id 重复而失败。因此 **statement id 天然是"一条语句"的唯一键**，可以直接当缓存键，不需要额外的作用域机制。
- `CachingExecutor.query` 判断是否走二级缓存的唯一条件是 `ms.getCache() != null`，而 `ms.getCache()` 只在映射声明了命名空间缓存且全局 `cacheEnabled` 为真时非空。仓库当前对 `cacheEnabled` / `@CacheNamespace` / `<cache` 零匹配。
- `MybatisPageExecutor` 的方法签名绑定 MyBatis-Plus 的 `IService` 与 `Page`；仓库没有 PageHelper，也没有任何 `RowBounds` 用法。原生 MyBatis 侧不存在分页设施。
- 现网验证手段只有"本机以 `h2` profile 起 sample 工程打接口"（见 [[verification-limits]]），所以 sample 必须始终保留至少一条能跑通的受控查询路径。

## Goals / Non-Goals

**Goals:**

- 让"权限条件注到哪条 SQL"这个决定完全由声明位置决定，不依赖任何跨语句可变状态。
- 把声明写错（位置错、目标不是查询、与二级缓存冲突）从运行期行为变成启动期失败。
- 迁移后 sample 仍能端到端证明过滤生效。

**Non-Goals:**

- 不给 MyBatis-Plus 内建方法（`BaseMapper.selectList` 一类）提供挂载点。
- 不改 `SqlAuthInject` 的 SQL 改写算法本身（IN/EXISTS 构造、表匹配递归逻辑保持原样）。
- 不引入分页设施，不引入缓存，不引入表清单/白名单式的全局管辖。
- 不动 `boot/tenant-spring-boot-starter` 里同型的 `TenantContext`。
- 不改 `JdbcAuthDataPersistent` 中的 SQL 拼接方式（单独暂缓）。

## Decisions

### D1 声明位置下沉到 Mapper 方法，而不是保留切面只换上下文键

备选：保留 `@AuthDataRange` 在 Service/Controller 上，把 `DataRangeContext` 的键从线程换成 MappedStatement id。

否决理由：那需要切面在目标方法执行前"预知"它会产生哪些 statement id，而产生点（Service）与消费点（语句）粒度不同，重入与线程池两个问题只是换了键、没有消失——上下文仍然要在进出时 set/clear，嵌套仍然要栈。声明直接落在方法上之后，上下文的必要性本身消失了：**作用域边界由 MyBatis 的语句身份提供，不需要再造一个**。

代价：现有把注解标在 Controller/Service 上的用法会失效，见 D9 的迁移守卫。

### D2 拦截点留在 `StatementHandler.prepare`

备选：改拦 `Executor.query`，`MappedStatement` 是方法入参，取语句身份不用反射。

选 `prepare` 的理由是粒度对齐：`prepare` 是"每一条真正执行的物理语句"过一次，被声明的嵌套查询与 `<collection>` 子查询各自带着自己的 statement id 到来，天然被覆盖。走 `Executor.query` 则要同时拦 4 参数与 6 参数两个重载（内层调用走 6 参数版本），漏一个就是静默不过滤，且改写依赖传入的 `BoundSql` 实例一路传到下游不被重建。

配套的两处健壮性修补（现状代码都缺）：

- 取 `MappedStatement` 的方式（实施时更正）：MyBatis 3.5.16 的 `Plugin` **只有 `wrap`、没有 `unwrap`**；社区常用的 `SystemMetaObject` 读 `h`/`target` 解包写法在 JDK 17 上实测抛
  `InaccessibleObjectException: module java.base does not "opens java.lang.reflect"`——`java.lang.reflect.Proxy.h` 是不可反射访问的。
  改用公开入口 `Proxy.isProxyClass` + `Proxy.getInvocationHandler(target)` 拿到 `Plugin` 处理器，再用 MetaObject 读它的 `target` 字段（MyBatis 自己的类，非受限模块）逐层下钻，最后读 `delegate.mappedStatement`。
  现状代码是裸强转 `(StatementHandler) invocation.getTarget()`，只要我们的插件被排在别的插件之后，target 就是代理、直接 ClassCastException。
- 用 `ms.getSqlCommandType() != SELECT` 作为放行条件。`prepare` 同样覆盖 INSERT/UPDATE/DELETE，写语句被改写会产出非法 SQL，方向是故障而不是越权，但必须挡在门口（spec 已把它定为启动期失败，这里是运行期的第二道）。
- 语句身份解析不出来时抛异常而不是放行：识别不出语句就无法判断它是否需要过滤，静默放开正是本次要消除的那类失效。

### D3 启动期一次性建缓存，执行期只补权限集合

建 `statementId -> 声明模板` 的映射，模板只含注解上的静态信息（业务功能、操作类型、目标表、别名、主键列、匹配方式）。执行期命中后把当前登录人的权限集合合进去构造 `AuthRange`。

不把权限集合缓存进模板：那会使缓存按用户分键，而权限字符串本身是敏感数据，一旦缓存生命周期长于请求就是跨用户泄漏——正是要消除的那类问题。

不采用"首次执行时懒解析"：懒解析等于没有启动校验，而启动校验是本次的主要交付之一。

### D4 校验挂载点用 `SmartInitializingSingleton.afterSingletonsInstantiated()`

备选：挂在 `SqlSessionFactory` bean 创建后；挂在 `ApplicationRunner`。

否决前者是因为 mapper 接口不只在 `Configuration` 构建期注册——`MapperFactoryBean.checkDaoConfig()` 会在各 mapper bean 创建时补注册，那个时点可能晚于 `SqlSessionFactory`，扫描会漏，漏掉的正是"解析不到语句"这一类要抓的错误。后者太晚，一个配置错误的声明要等整个上下文启动完才报错。`afterSingletonsInstantiated` 在所有单例实例化之后、任何业务调用之前，且能安全拿到 `SqlSessionFactory`。

数据来源：`Configuration.getMapperRegistry().getMappers()` → 每个接口 `getMethods()` → 找 `@AuthDataRange`；语句身份按 `接口全限定名.方法名` 拼，再回查 `Configuration`。

### D5 静态 SQL 判定用正例枚举，不用否定式

启动期的目标表校验只对 `sqlSource instanceof RawSqlSource` 的语句执行，取 `ms.getBoundSql(null)` 得到的文本跑现有的表匹配逻辑。

为什么传 `null` 是安全的：静态来源只登记 `ParameterMapping`，不对 `#{}` 求值。

为什么不能写成"非 `DynamicSqlSource` 就试"：`DynamicSqlSource` 用 `null` 参数渲染会真的去跑 OGNL 判 `<if test="x != null">`，得出一条与实际执行不同的假 SQL，据此判定表名会产生随机误报；`ProviderSqlSource` 则会调用 provider 方法。这两类连同其它来源统一延后到执行期，由现有的"匹配不到目标表即抛 `JSQLParserException`"兜住。

### D6 一条语句一个权限槽位，operate 静态写在注解上

备选：把 operate 做成 mapper 方法的入参，注解里用占位符引用（例：`operate = "#[authOperate]"`）。

选静态声明的理由：`businessFunction` + `operate` 是权限模型的维度，写在代码里可以直接 grep 到"这个槽位在哪些查询上生效"，这也是审计这张表权限覆盖情况时唯一能依赖的东西。参数化会把这层可检索性换成运行期解析，并让启动校验对这类语句失效。代价是同一张表 N 个 operate 需要 N 个方法——sample 里 `meeting_room` 已有 `edit_info` / `approve` 两个槽位，正好是这个形状，不需要为演示额外造例子。

### D7 sample 分页用手写两条语句，不给框架加分页设施

分页的受控查询需要一条列表语句（带 `limit`/`offset`）加一条计数语句（`select count(*)`），两个方法各自声明。

否决"引入 PageHelper"：为一个示例接口新增全仓库依赖不成比例，且分页策略是独立于本次作用域问题的决定。否决"sample 保留 MyBatis-Plus 分页、只改不分页的接口"：那样三个最像真实业务的分页接口会失去过滤而**不报任何错**，本次改造就没有可跑的证明——这是本设计不接受的，因为验证手段只有起 sample 打接口。

### D8 二级缓存用互斥校验处理，不去改缓存键

三个备选：改写后强制 `flushCache`；把权限范围并入缓存键；禁止共存。

前两个都不可行或不值得：缓存键在 `CachingExecutor` 进入语句改写**之前**就已算出（键内容来自 `ms.getId()` + 改写前 SQL + 参数值），要改得侵入 MyBatis 执行器内部；把权限范围并进键会让受控语句的命中率归零，却仍然保留二级缓存的全部一致性复杂度。因此选择"受数据范围管辖的语句不得启用二级缓存"，用启动失败把这个约束钉死，代价是零运行期复杂度。当前仓库没有任何缓存声明，所以这条校验不会挡住现有代码，它挡的是将来。

### D9 迁移守卫：非 mapper 位置上的残留声明也要启动失败

单纯把切面删掉之后，旧位置（Controller/Service 方法）上的 `@AuthDataRange` 不会被 D4 的 mapper 扫描看到，会变成**静默不过滤**——这恰好是本次要消灭的那类失效。

因此启动校验除遍历 mapper 外，再遍历容器内 bean 的类（取原始类，避开 CGLIB 代理），若发现 `@AuthDataRange` 出现在不属于 MyBatis mapper 接口的方法上，则启动失败并提示迁移到 mapper 方法。

代价是启动时多一次 bean 类型遍历；收益是破坏性变更不会以"看起来正常但权限放开"的形态落地。这条是设计层面的自我加戏，不在 proposal 的 What Changes 里，若要砍掉只影响这一项校验。

### D10 `AuthRbacConfig` 补注入的方式

在 `annotationAuthHandler(...)` 的 bean 方法上把 `JdbcAuthDataPersistent` 作为参数注入并调用 handler 的 setter（该类已有 Lombok `@Setter`），不做字段级 `@Autowired`。只补这一处装配，明确不顺手改 `JdbcAuthDataPersistent` 里的 SQL 拼接。

## Risks / Trade-offs

- [漏声明 = 不过滤，且没有机制能发现] → 无技术缓解，这是 opt-in 的固有代价，spec 里已把它写成显式契约而不是缺陷。可用的辅助只有 `prepare` 期已有的 debug 日志（原始 SQL / 改写后 SQL），排查时按语句 grep。
- [权限条件一律 AND 进 WHERE，会让 LEFT JOIN 退化成内连接（无权限的行整行消失，而不是从表列为空）] → 本 change 不改写规则，行为与现状一致；`SqlAuthInject` 支持在子查询里定位目标表，业务可以把"必须保留左表全部行"的语句改写成子查询形式绕开。真要支持外连接语义需要单独的改写设计。
- [机制改造与切面删除必须同一次落地，否则存在"切面已删、语句级未接"的窗口=全量不过滤] → 见 Migration Plan 的顺序约束：不允许拆成两次可发布的提交。
- [D9 的迁移守卫依赖遍历容器 bean，可能因个别 bean 类型解析不出（懒加载占位、工厂方法返回类型宽）而漏报] → 漏报的只是这条辅助校验，主路径的失败方向仍然是"mapper 上无声明即不过滤"，不会因为这条校验而误伤；不做成必过项。
- [一个 operate 一个方法会让受控表的方法数随槽位数线性增长] → 接受。同表 10 个 operate 需要 10 个方法，这是 D6 换来的可审计性；SQL 可用映射文件的片段复用，重复的是声明而不是语句体。
- [`RawSqlSource` 判定使启动期的表名校验对动态 SQL 失效] → 延后到执行期抛异常，方向是报错而不是放开，可接受。

## Migration Plan

落地顺序（同一次提交内完成，不存在中间可用状态）：

1. 建缓存与启动校验（不改动执行路径）→ 此时旧机制仍在，可先让校验暴露既有声明位置问题。
2. `prepare` 改为按 statement id 决策；删除 `DataRangeContext`、`DataRangeAspect` 及 starter 中的 bean。
3. 迁移 sample 四处声明到 mapper 方法，并为分页接口补列表与计数语句。
4. 补 `AuthRbacConfig` 的注入，使 `/meetingRoom/page` 恢复可断言。

回滚：整段 revert。框架版本是同一 `0.0.1-SNAPSHOT` 线，无外部消费方需要兼容期，因此不提供"新旧并存"的过渡开关——并存意味着两条注入路径都可能生效，属于本次要消除的不确定性。

## Open Questions

- 当前登录人权限集合为空时，构造出的条件形如 `authority in ()`，多数数据库会直接语法错误，而不是返回空结果集。这是既有行为、spec 未约束、也不影响本次的作用域改造。要不要在本 change 里把它定义成"空权限集合 = 查不到任何行"，需要单独拍一次；默认不动。
