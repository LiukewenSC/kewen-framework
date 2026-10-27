# Design

## Context

`auth-core` 交出的是一组接缝：两个强制扩展点（当前用户上下文、菜单权限处理）、一个可选扩展点（菜单持久化）、两个注解声明（`@AuthMenu`/`@AuthMenuDoc`、`@AuthDataOperation`、`@AuthDataRange`）、一份可重命名的权限存储映射（`cu.auth.auth-data-table.*`）。目前这些接缝只有内存桩单测，没有端到端载体。

约束来自用户 2026-10-20 的裁决，本设计全部遵守：

- 示例不引用任何 `sys_*` 表、不加载 `docs/sql/` 下的框架脚本；需要的表全部自设计。
- 业务主键与权限存储的数据ID一律整型，不为其他主键形态加表或加配置档。
- 框架侧只动四条：注册拒绝响应通道、加用法错误出口、权限集合为空时加判断、range 侧字面量拼接只留 TODO。
- H2 相关声明只写在示例的 `h2` profile 段内（既有硬约束）。
- 数据落在示例自己的 `data/` 目录，重启后可继续使用。

规格要求见 `specs/` 下五份增量，本文不再复述。

## Goals / Non-Goals

**Goals:**

- 每个规格场景都能落到"一个声明位置 + 一条触发命令 + 一个观察点"，且观察点必须是外部可见的（响应、日志、表内容、启动异常），不能靠读代码结论。
- 示例自己实现的三个扩展点必须简单到几十行，但要把 core 的判定路径全部走一遍——证明 core 与具体权限模型无关。
- 框架侧四条改动的影响面被明确记录，特别是既有 rbac 示例的响应契约变化。

**Non-Goals:**

- 不做权限模型层该做的事：角色/部门/用户树、菜单层级继承、菜单管理端点、缓存。
- 不做登录：示例不引入 `login-security-spring-boot-starter`、不用 spring-session、不写模板页。
- 不做多配置档矩阵：只有一个 `h2`（文件库）profile 为默认，`mysql` profile 仅连接不建表。
- 不改 `SqlAuthInject` 的字面量拼接（留 TODO），不改 `docs/sql/`，不改 `auth-rbac*`。

## Decisions

### D1 数据载体：H2 文件库 + 幂等脚本，`sql.init.mode` 保持 `always`

`jdbc:h2:file:./data/auth_core_sample;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE`，连接参数与 rbac 示例的 `h2` 段一致，只把 `mem:` 换成 `file:`。所有 DDL 写 `CREATE TABLE IF NOT EXISTS`（不写 `DROP`），种子写 `INSERT ... SELECT ... WHERE NOT EXISTS`，于是 `always` 可以永远开着：首次启动自动建表灌数据，之后重启既不清空使用者改过的授权，也不重复插入种子行。重置 = 删除 `data/` 下的数据库文件，README 写明这一条。

相对路径意味着必须从模块目录启动——这与现有各示例用 `./logs` 的约定一致，README 里同处说明。

*备选*：H2 内存库（每次重启丢手工授权，与"以后也好用"直接冲突）；`mode=never` + 首次手工初始化（增加一道人工步骤，用户明确偏好低摩擦）；SQLite（仓库现在零 SQLite 依赖，而所有脚本是 MySQL/H2-MySQL 方言，`AUTO_INCREMENT`、`ON UPDATE CURRENT_TIMESTAMP` 要整篇重写）。

### D2 权限存储：示例自有表 + 七个属性全量映射

`demo_auth_data(pk BIGINT 自增, biz_module, biz_data_id BIGINT, action, auth_str, auth_desc)`，`cu.auth.auth-data-table` 把表名与六个列名全部覆盖成与默认约定不同的值。这样既满足"不用框架表"，又让"框架不绑死命名"这条能力被真正演到——只要有任何一处代码硬编码了列名，示例立刻失败。

数据范围注入会生成 `demo_work_order.id IN (SELECT biz_data_id FROM demo_auth_data WHERE ...)`，业务主键与权限数据ID同为整型，不存在跨类型比较。

*备选*：照抄框架列名（能力没演到）；再开一张 varchar 数据ID的表演字符串主键（用户否决）。

### D3 身份注入：`X-Demo-User` 请求头 + 一个拦截器写 ThreadLocal

示例注册一个 MVC 拦截器，`preHandle` 读 `X-Demo-User` 放进 ThreadLocal，`afterCompletion` 清理；`AuthCurrentUserContextHolder` 的实现从 ThreadLocal 取用户名，**每次调用都通过 MyBatis 查 `demo_user_auth` 装载权限串**，不做请求内缓存。

不缓存是有意为之：装载权限那条查询本身就是一条"与调用链上其他语句同批执行、但未被声明数据范围"的语句，正好充当 `auth/data-range` 里"同一调用链上的其他语句不受影响"的观察对象；同时它天然演示了"权限集合为空即拒绝"（不带请求头时）。

*备选*：spring-session + 表单登录（把 core 示例做成 rbac 示例的翻版，还要拖进登录层）；Java 里硬编码用户名→权限（少一张表，但"数据必须经过 MyBatis"这条要求就没被满足了）。

### D4 三个扩展点的自有实现

```
AuthCurrentUserContextHolder  -> DemoAuthContextHolder
      getUser()  <- ThreadLocal 里的用户名
      getAuths() <- demo_user_auth(username) 逐行转 BaseAuth

AuthMenuHandler               -> DemoMenuAuthHandler
      hasMenuAccessAuth(auths, path)
        <- demo_menu_auth(path, auth_str)：该 path 有登记行时，
           要求 auths 与之有交集；该 path 未登记时视为"没有授予任何人"

MenuApiService<Long>          -> DemoMenuApiStore
      list/saveBatch/getRootParentId/generateId
        <- demo_menu_api(id BIGINT, path UNIQUE, name, parent_id, remark)
           ID 用 hutool 雪花（auth-core 已带 hutool-all）
```

`demo_menu_auth` 之所以存在：数据ID列定死整型之后，请求路径存不进 `demo_auth_data`。这一条在 README 里点明，并说明它与权限模型层的菜单权限表无关——它只是示例自己选的一种"path→需要什么权限"的记法。

`MenuAccessInterceptor` 与 `AuthExceptionHandler` 需要容器里有 `AuthMenuHandler`；`AuthCoreConfig` 不登记它（那是权限模型层的事），所以示例必须自己提供，否则启动失败——这既是约束也是 A2 那条场景的载体。

### D5 原生 MyBatis，不用 MyBatis-Plus

`mybatis-spring-boot-starter:2.3.1`（已实测可从项目配置的阿里云镜像取得）+ XML 映射文件。`MybatisDataRangeInterceptor` 是 `Interceptor` bean，Boot 的 MyBatis 自动配置会把容器中的 `Interceptor` 收进插件链，无需手工注册——这一步列为实现时必须实测确认的第一件事。

选 XML 而不是注解 SQL，是因为有两类场景只能在 XML 上做：`<if>` 动态语句（演"无法静态取得 SQL 的语句把表校验延后到执行期"），以及 `<cache>` 开关（演"语句启用二级缓存则启动失败"）。

*备选*：MyBatis-Plus（本机已有、rbac 示例在用），但它把内建方法、分页插件、逻辑删除一并拖进来，而这些不在 `@AuthDataRange` 的管辖范围内，只会让"core 只管原生语句"这条边界更难说清。

### D6 副作用证据表把"方法体有没有执行"变成可查询事实

`demo_execution_log(id, api_key, biz_data_id, memo, created_at)`。所有被 `@AuthDataOperation` 管辖的写方法，第一行插入一条自己的记录。拒绝发生在方法体之前，因此"该 `api_key` 的日志条数没有增加"就是"方法体没执行"的直接证据；批量部分命中的整体拒绝同理（一条都不该出现）。

这让 C 组里"方法体不因已命中的 ID 而执行""超出上限时不发起任何权限查询"这类场景有了不依赖肉眼的判据。后者额外用 SQL 日志辅助：不开 `logging.level.org.springframework.jdbc.core=DEBUG` 时看不到任何权限查询语句。

### D7 拒绝通道：条件注册 + 两个编码

`AuthCoreConfig` 增加 `@Bean @ConditionalOnWebApplication @ConditionalOnMissingBean(AuthExceptionHandler.class)`；`AuthExceptionHandler` 再加一个 `@ExceptionHandler(AuthServiceException.class)`，输出 `code=400`，`AuthException` 保持 `code=403`。用 `@ConditionalOnMissingBean` 是为了满足"使用方自带出口时框架出口让位"，而不是再造一个竞争出口。

两个编码走 `Result` 的 body 字段、HTTP 状态仍是 200——这与该 advice 现有的写法以及 `basic` 层的处理惯例保持一致，不引入第二套错误形态。

### D8 空权限集合：注入恒假条件

在 `SqlAuthInject.convert2NewSql` 里，取到目标语句后先判 `authRange.getAuthorities()` 为空：为空时给该语句挂一个恒真语法、恒假语义的条件（`1 = 0`），`IN` 与 `EXISTS` 两条分支同一处理，不再构造权限子查询。这不是兜底：它与 `hasDataOperateAuths` 的"权限集合为空即拒绝"是同一条判定规则的两种表现——写路径拒绝执行，读路径不给任何行。

### D9 启动期失败的场景放 `src/test`，其余全部走 HTTP

D 组"声明错误在启动阶段暴露"的四类，加上"声明不在映射接口上"的旧位置守卫，无法在活着的应用上演示。做法：`src/test` 下为每种坏配置准备一组独立的 mapper 接口与上下文，断言启动抛出的异常信息指到对应声明位置。这类断言只在示例里做"是否失败 + 是否指到位置"，不重复 `auth-core` 已有的单测。

### D10 文档与脚本同源

`README.md` 的场景矩阵是唯一的用例清单来源，每行固定五列：编号、声明位置、触发命令、观察点、期望。`verify.sh` 按同一批编号逐条执行并打印通过/失败，编号一致使两边不会漂移。矩阵需要两次启动才能验的场景（重启保数据、已有行不被覆写）在文档里单独成段，脚本用 `--stage=restart` 参数触发，不参与默认的一次性跑。

## Risks / Trade-offs

- [`sql.init.mode=always` 与幂等脚本的语法假设未实测] → 实现第一步就用 `scripts/check-sql-compat.sh` 跑新增的两个脚本；若 H2(MySQL 模式) 不接受无 `FROM` 的 `SELECT ... WHERE NOT EXISTS`，改用 `FROM DUAL` 再验；两边都通不过则退回"首启建表、种子只在表空时写入"的一条 SQL 判式，并在 README 记录实际形态。
- [Boot 的 MyBatis 自动配置是否真的收下 `Interceptor` bean 未在本仓库验证过] → 实现第一步做最小启动验证：日志里确认改写后的语句出现；不成立则在示例配置里显式 `configuration.addInterceptor`，并在文档注明。
- [`mybatis-spring-boot-starter:2.3.1` 与 `pom.xml` 里管理的 `mybatis:3.5.16` 版本叠加] → 依赖以父 pom 的 `dependencyManagement` 为准；启动后确认 `MyBatis` 与 `mybatis-spring` 都落在预期版本，冲突则在本模块 `dependencyManagement` 钉住。
- [注册 `AuthExceptionHandler` 改变 rbac 示例的拒绝响应体（generic code → `code=403`）] → 该变化已在 proposal 的影响面里列明；配套前端需确认按 code 判分支的地方。回滚只需撤这一个 `@Bean`。
- [恒假注入改变了"无权限用户查询受管辖列表"的对外结果] → 从（高置信推断的）数据库语法错误变成返回零行；有依赖报错来识别未登录的用法会受影响。范围仅限被声明的语句，未声明语句零变化。
- [菜单注册在启动后的独立线程里跑，验证脚本可能与它抢跑] → `verify.sh` 在断言 B 组前等待"添加的接口有"这条日志或做有限次重试；README 说明该线程与主启动的先后关系。
- [H2 文件库同一时刻只允许一个进程打开] → README 明确"不要在 IDE 与命令行同时启动同一个示例"，并给出用 H2 控制台读该文件的正确方式（应用停着读，或用控制台连接时的独占说明）。
- [本模块 `spring.sql.init` 引用的脚本在 `classpath:sql/` 下，而父 pom 会把 `docs/sql` 也挂到同一位置] → 本模块 pom 自己声明 `<resources>`，只挂本模块 `sql/` 一项，使框架脚本根本不进 classpath；这也让"示例不引用框架表"这一条在构建产物层面可核对。

## Migration Plan

无数据迁移，无破坏性变更需要脚本配合。

1. 框架侧四条改动合入（可单独回滚，逐条独立）。
2. 示例模块与其脚本、文档、验证命令作为整体新增（回滚 = 删除该模块 + 撤销 `sample/pom.xml` 里的 `<module>` 一行）。
3. `.gitignore` 增加示例 `data/` 的忽略项。
4. 既有 rbac 示例：不改其代码；只有第 1 步的通道注册影响它的拒绝响应体。

## Open Questions

- `code=400`/`403` 是继续用 `HttpStatus` 的字面值，还是收进 `basic` 的错误码常量表——不影响规格与任务拆分，实现时按现有 `AuthExceptionHandler` 风格定即可。
- `verify.sh` 是否需要把每条用例的原始响应留档到文件便于复查（脚本可读性与产物目录的取舍），实现时按脚本长度决定。
