# Design

## Context

三个注解是同一套权限模型（`authority` 串 + `sys_auth_data` / `sys_menu_api` / `sys_auth_menu`）上的三个执行点，各自管着不同的被管对象：

```
  HTTP 请求                 业务方法                    SQL 语句
      |                        |                           |
      v                        v                           v
 +-----------+         +----------------+          +----------------+
 | @AuthMenu |         |@AuthDataOperation|         | @AuthDataRange |
 | 能不能进   | -------> | 能不能动这条    | -------> | 能看到哪些行    |
 | 这个门     |  URL     | 记录    dataId |          | (只改写 SELECT)  |
 +-----------+ 行身份=   +----------------+          +----------------+
                 sys_menu_api.path                        mappedStatement.id
```

本轮只动 `@AuthMenu`。当前状态与约束：

- `@AuthMenu` 有三个消费点：`MenuAccessInterceptor:46/48` 只判存在性（管辖开关，方法→类回退）；`MenuApiGeneratorProcessor:132/157` 读 `name()` 拼菜单标签。全仓没有任何按菜单名称查询的代码，`name()` 从不出现在判定路径里。
- 鉴权判定是 `DefaultSysAuthMenuComposite.hasMenuAuth` 拿请求 URI 与行的 path 做**精确字符串比较**，授权挂在行的 `id` 上（`sys_auth_menu.api_id`、`sys_menu_route.api_id`）。
- 层级是 **path 之间的父子关系**：生成器用 `menuApiMap.get(parentPath)` 按 path 找父行，父路径取值来自类的 `@RequestMapping`（约定上就是本类方法 URL 的前缀）。`RbacMenuApiService.fillAuthType` 已按 `parentId` 派生权限类型（顶级 1=自身、子级 2=继承父）。
- 菜单同步跑在 `InitMenuApiCommandLineRunner:24-27` 起的 `new Thread` 上，`UncaughtExceptionHandler` 只 `log.error`。
- 用户在开发阶段，明确不考虑旧项目/旧库升级；不接受为"将来才兑现的收益"引入需要长期理解的新概念。

## Goals / Non-Goals

**Goals:**

- 管辖开关与显示文案在类型层面分开，让"给菜单起名"不再可能被误解成"在声明权限"。
- 关掉"请求可改写鉴权身份"这条通道。
- 把类级声明的既有语义固化成规格，不再靠回退代码去推断。

**Non-Goals:**

- 不换鉴权身份（见 Decision 1，评估过并被他否）。
- 不加启动期校验、不改异步同步形态、不做孤儿行检测（Decision 5）。
- 不动 `@AuthDataOperation`、不动 `@AuthDataRange`、不动任何 DDL 与数据库结构。
- 不改 `kewen-web-admin`（独立仓库）。

## Decisions

### 1. 鉴权身份继续用 `sys_menu_api.path`，不引入端点键

评估过的备选：把行身份换成 `method.getDeclaringClass().getName() + "#" + method.getName()`，新增 `endpoint` 列（可空，类分组行没有值），`uk_sys_menu_api_path` → `uk_sys_menu_api_endpoint`，path 降为普通索引并继续承担层级。它能同时解决三件事：`/x/{id}` 型端点因 path 与实际 URI 精确比较而恒拒绝、同路径不同 HTTP 方法共用一份授权、以及 URL 改名等于改权限归属。

**否掉的理由**：`endpoint` 是一个必须长期理解的新概念，而它的主要收益（改名不动授权）在开发阶段兑不了现——库里全是自己的种子数据，改名直接改种子。按"复杂度按概念数量计价"的尺度，净亏。

因此这两个现存缺陷本轮**明确保留**，且都不被现有代码路径触发（全仓 Controller 用 `/list`、`/add`、`/update`、`/delete` 加请求体里的 `IdReq`，没有 `@PathVariable` 型端点）：

- 带路径变量的端点标 `@AuthMenu` 恒拒绝；
- 同 path 不同 HTTP 方法共用一份授权。

将来要动，只有两条路：运行期改用 pattern 匹配（只修第一条），或重提端点键（两条都修）。

### 2. 类级声明保留为"批量声明"，不降级、不补注解

评估过的备选：把类级 `@AuthMenu` 降级成只给分组行命名、管辖开关收到方法级。

**否掉的理由**：类级声明划的是一个**固定集合的边界**（该 Controller 的全部端点），是量词；它不会像 `@AuthDataRange` 改造前的 `ThreadLocal` 那样跨到别人的语句上。降级会取消 9 个端点的管辖，其中 `/menu/api/update`、`/rbac/user/update`、`/rbac/user/delete` 是权限体系自身的写接口——那是安全回退，不是简化。

代价：类里加一个映射方法会自动进入管辖并自动注册，靠 Decision 4 的 `fillAuthType` 继承父行授权，所以不需要人工补授权。

### 3. `@AuthMenuDoc` 承担显示文案，同时就是菜单标签来源

`@AuthMenu` 变成无属性标记（`@Target` 保持 `{METHOD, TYPE, ANNOTATION_TYPE}`），新增 `@AuthMenuDoc` 携带 `name()`。生成器两个消费点改为：

```
类标签 = 类上的 @AuthMenuDoc.name()，缺失时为类简单名   （现有行为，:132/:137）
端点标签 = 方法上的 @AuthMenuDoc.name()，缺失时为方法名 （现有行为，:157/:161）
```

不另设标签字段、不让 `remark` 兼职：`remark` 是运营在管理界面里填的自由备注，代码声明的文案是另一回事，混用会让"重启后是否覆写"这条规则说不清（已有行不被覆写，是规格第 4 条）。

`name()` 从 `@AuthMenu` 移除后，**漏改的标注点直接编译失败**，不需要扫描脚本兜——这是把它做成源码级 BREAKING 的直接好处。

### 4. `path` 不可写用 SET 白名单实现，不靠"不传就不改"

`updateMenu` 现在走 `sysMenuPathMpService.updateById(req)`，而 `MenuApiSaveReq extends SysMenuApi`。MyBatis-Plus 默认字段策略是"**不传就不动、传了就写**"，所以"忽略入参 path"必须有实体动作，不能靠请求方不传。

采用：改成显式列出允许字段的更新（`name`、`remark`、`auth_type`、`parent_id`），`path` 不进 SET。

备选与否决理由：

- 接到 `path` 就抛校验错误——把一个静默无效变成 400，对一个只有自己在用的管理接口是净增摩擦，且要给"传了和库里一致的值"再开特例。
- 新建一个不含 `path` 的显式 DTO——最干净，但要动 `MenuApiSaveReq` 继承关系和 `RbacMenuApiController` 签名，收益与白名单相同、改动更大。
- `path` 置 null 再 `updateById`——依赖默认 null 策略，以后有人调策略就悄悄失效，正是本次要消除的那类静默。

`MenuApiSaveReq` 仍继承实体，所以 `path` 在契约上"可传但无效"；这一点写进 README，不在本轮改 DTO。

### 5. 启动校验、孤儿检测、同步化整条链推后

不新增启动校验器，`InitMenuApiCommandLineRunner` 的 `new Thread` 与只记日志的异常处理保持不变，生成器"只新增、不覆写已有行"保持不变（本轮把它规格化而不是改它）。

要"不认识就抛"就必须先把同步搬回主线程，两者绑死、没有中间形态；他选择等想清楚再调。由此留下的探测真空（按代码路径推演，**未实测**）：

- 只改某个方法的 URL → 新行的 `parentPath` 仍能在库里找到旧父行，`auth_type=2` 继承父授权，**不报错**，只在菜单树上留一条带旧授权的死行；
- 改类的 `@RequestMapping` → 代码侧新父行是本轮刚算出来的、没有授权，全部子行继承它 → **整组拒绝**，旧行连同授权一起变孤儿。

## Risks / Trade-offs

- [path 身份对 URL 改名脆弱，且无人探测] → 本轮接受，不缓解。风险等级由"库里只有自己的种子数据"压低；规格第 3 条已把请求侧那条通道关掉，剩下的是代码与种子数据之间的人工同步。
- [SET 白名单是人工维护的清单，以后给 `SysMenuApi` 加字段容易漏进白名单] → 用一个显式测试锁住行为：带 `path` 的更新请求不改 `path`、其余允许字段照常改；字段清单只出现在一处。
- [36 处标注点拆分过程中漏改] → 编译器兜住（`@AuthMenu(name=...)` 在 `name()` 删除后不能编译），无需脚本。
- [类级批量声明使新增端点自动进入管辖] → 靠既有的 `fillAuthType` 继承父行授权，不产生"新端点谁都不能访问"；行为由规格第 1、4 条共同约束。
- [两个保留缺陷在将来用 `@PathVariable` 时被激活] → 记入本文件 Decision 1 与项目记忆，不靠记忆消失；激活时的两条补法已列出。
- [`path` 在管理界面上变成只读] → 属预期契约变更，`kewen-web-admin` 跟进时把输入框去掉或改成只读展示。

## Migration Plan

无数据库迁移：不改 schema、不改任何行的 `id`，因此 `sys_auth_menu.api_id` 与 `sys_menu_route.api_id` 都不受影响。变更全部是源码级（BREAKING，编译器强制）与种子数据里的两处菜单名称文案。

回滚：本轮不引入结构性变更，回滚即还原四个文件的注解定义与 `updateMenu`，种子数据文案可独立保留。
