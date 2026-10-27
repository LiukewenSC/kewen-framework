# Proposal: build-auth-core-boot-sample-demo

## Why

`auth-core` 是权限框架的接缝层，但它目前没有任何独立可运行的验证载体：`sample/auth-core-boot-sample` 只有一个 IDE 生成的 `Main.java`，框架能力全部要靠 `auth-rbac-boot-sample` 间接验证。这带来两个后果——core 与 rbac 是否真的解耦无人证明，以及 openspec 里 `auth/data-operation`、`auth/data-range`、`auth/menu-access` 三份规格合计 19 条 requirement、三十多个 scenario，没有任何一条在端到端层面跑通过（现有的全是内存桩单测）。

搭这个 demo 的同时暴露出 4 个 starter 装配与健壮性问题，其中两个直接让规格里的场景在今天无法成立：`AuthExceptionHandler` 从未被注册（`spring.factories` 只登记了 `AuthCoreConfig` 与 `AuthWebConfig`），纯 core 接入时权限拒绝对外是 HTTP 500 而不是规格要求的统一拒绝语义；`AuthServiceException`（用法错误）没有任何专属出口，与权限拒绝同形，直接违背 `auth/data-operation` 的"权限拒绝与配置错误区分表达"。

## What Changes

**框架侧（`../../../../auth`，4 条，全部经用户 2026-10-20 逐条裁决）**

- `auth-core-spring-boot-starter`：把 `AuthExceptionHandler` 注册成自动装配的 bean，使 `AuthException` 对外稳定输出 `code=403` 的统一拒绝结果。
- `auth-core-spring-boot-starter`：`AuthExceptionHandler` 增加 `AuthServiceException` 出口，输出 `code=400`，消息只带业务功能/操作类型/上限数，不含框架类名与接口名。
- `auth-core`：`SqlAuthInject` 在权限集合为空时注入恒假条件而不是渲染 `authority IN ()`，使"无权限即看不到任何行"，与 `JdbcAuthDataHandler.hasDataOperateAuths` 的空集合即拒绝保持同一语义。
- `auth-core`：在 `SqlAuthInject.authTableWhereExpression` 处加 TODO，登记"authority 以字面量拼进 SQL"这笔债务——用户裁决本轮不改。
- `auth-core-spring-boot-starter`（**实现期新增，超出原始四条**）：`AuthRangeStartupValidator` 的 `@ConditionalOnBean(SqlSessionFactory.class)` 使该 bean 永不被创建，导致所有 `@AuthDataRange` 静默失效、启动校验一并消失而应用仍启动成功；改为 `ObjectProvider` 在实例化阶段取。属于用户原始请求中"测试过程中发现的非逻辑性 bug 可以修改"的授权范围。

**示例侧（`sample/auth-core-boot-sample`，全新）**

- 一个可独立启动的 Spring Boot 工程：原生 MyBatis（`mybatis-spring-boot-starter` 2.3.1，非 MyBatis-Plus）+ H2 **文件库**落在 `sample/auth-core-boot-sample/data/`，重启保留数据。
- 示例自己实现 core 的三个扩展点：`AuthCurrentUserContextHolder`、`AuthMenuHandler`、`MenuApiService`；`X-Demo-User` 请求头注入身份，不引入登录与会话。
- 8 张示例自有表（`demo_` 前缀），**不引用任何 `sys_*` 表、不加载 `docs/sql/` 下任何框架脚本**；权限存储表的列名自设计，靠 `cu.auth.auth-data-table.*` 七个属性全量映射。
- `demo_execution_log` 表作为副作用证据，用来证明"方法体没有执行"，而不是只看返回码。
- `README.md`：核心功能清单 + 场景矩阵（声明位置 → 触发命令 → 观察点 → 期望）；`verify.sh` 把矩阵里的用例逐条断言。
- "启动即失败"那类场景（`@AuthDataRange` 声明写错的四种情形）无法在活着的 app 上演示，放 `src/test` 断言。

**范围外**

- 不做字符串主键的端到端演示（用户裁决：本框架示例主键一律 BIGINT，String 泛型能力只服务外部项目；该 scenario 由 `JdbcAuthDataHandlerTest` 的归一化断言覆盖）。
- 不改 `docs/sql/` 下任何脚本的内容与归属，不改 `auth-rbac*` 的任何代码。
- 不在示例里做 `menu-access` 的"鉴权身份不可由请求改写"——该场景依赖菜单更新端点，端点在 `auth-rbac`，README 里标注为示例覆盖边界。

## Capabilities

| Capability | 变更类型 | 内容 |
| --- | --- | --- |
| `auth/starter-assembly` | 新增 | starter 必须自动装配拒绝响应通道；强制与可选扩展点的装配要求 |
| `auth/data-operation` | 修改 | "权限拒绝与配置错误区分表达"落到 HTTP 响应通道的可区分编码 |
| `auth/data-range` | 新增 | 权限集合为空时的注入语义 |
| `sample/demo-database-profiles` | 新增 | 以文件库承载数据的示例：重启保数据、脚本可重复执行 |
| `sample/auth-core-verification-demo` | 新增 | 示例对 core 全部接缝与场景的覆盖义务，以及验证文档与脚本 |

## Impact

**受影响的既有行为（需要用户知情）**

- `AuthExceptionHandler` 一旦注册，`auth-rbac-boot-sample` 的权限拒绝响应会从"basic 的 `Throwable` 兜底 + generic code"变成 `code=403` + 统一消息。前端若按 code 判失败分支，需要同步确认。
- `SqlAuthInject` 的恒假注入会把"未登录用户查询受范围过滤的列表"从（推测的）SQL 语法错误变成返回零行。

**新增依赖**：`sample/auth-core-boot-sample` 引入 `mybatis-spring-boot-starter:2.3.1`（已实测可从项目配置的阿里云镜像取得）、`com.h2database:h2`（版本由 `spring-boot-dependencies` 管理）。框架模块不新增任何依赖。

**代码足迹**：框架侧改动集中在 2 个文件（`AuthCoreConfig.java`、`SqlAuthInject.java`）加 1 个已有的 advice 类；示例侧为新增模块，不触碰其他示例。`.gitignore` 增加一行忽略 `data/`。`scripts/check-sql-compat.sh` 按 `sample/*/sql/*.sql` 通配，新增脚本自动进入检查范围，无需改脚本。
