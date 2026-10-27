# Design

## Context

动机见 proposal.md - Why；行为边界见 `specs/auth/dept-inherited-access/spec.md`。这里只记录约束方案的技术现状。

判定通路实际是这样闭合的：

```
 配置侧                                    用户侧
 SimpleAuthObject.listBaseAuth()          UserAuthInfo.listBaseAuth()
        |                                        ^
        v                                        |
 sys_auth_menu.authority          SecurityUser.userAuthorities
        +---- hasMenuAuth: 用户串集合.contains(配置串) ----+
```

`FlatteningAuthority` 的 `equals`/`hashCode` 只认权限串。因此链路上任何一环拼出的串不一致，结果是**静默丢权限**而不是报错——这决定了下面的取舍都偏向"宁可当场失败，不要静默为空"。

三个约束现状：

- 用户权限体由 `SysUserUnionCompositeMapper.xml` 的嵌套 resultMap 从一次多表 join 装载，MyBatis 反射建对象，构造期无法强制字段非空。
- 权限实体的类注册表 `AbstractAuthObject.authEntityMap` 在静态块按 `flag()` 注册，key 冲突时**后扫描到的赢**，而扫描顺序依赖 classpath。
- `sample/auth-core-verification-demo` 已在规格中写死两条硬约束：示例 MUST NOT 依赖权限模型层的代码或表；示例 MUST 只用整型业务主键。auth-core 那块的做法不是我偏好选的，是被这两条逼出来的。

## Goals / Non-Goals

**Goals:**

- 让 `RELATIONDEPT` 的运行时行为与"严格子孙"裁定一致，且这条通路有一个能跑出 200/403 差异的载体。
- 把"权限体缺失"从 NPE 改成一个有语义的结果（空权限），使无凭证用户可被装载。
- 让新增的继承型权限实体不会再踩"标记含次级分隔符"这个坑——立成规格而不是靠人记住。

**Non-Goals（设计层边界，不重复 proposal 的范围）:**

- 不重构权限串的编解码规则本身，包括 `IFlagAuthEntity.getAuth()` 靠 `flag().split(次级分隔符).length` 推算位置数这套机制。本次只约束"标记必须是单段"，不改机制。
- 不给 `UserAuthInfo` 加反解能力。它反解不出来是已写入 `../../../../auth` 的 **设计结论**（`RELATIONDEPT__1` 反查不出它是哪个部门的父链），不是待修的洞。
- 不动 `auth-rbac` 里 `DEPT` / `ROLE` / `USER` 三个既有标记的行为，包括它们的笛卡尔积产出。

## Decisions

### D1　去自身的动作放在接口实现侧，不放在消费侧

`SysUserCompositeImpl.getDeptListByDeptId` 当前从 `deptId` 自己起步，返回含自身的链路。改法有两种：在实现里不把自身计入链路，或在 `UserAuthInfo.fillUserAuth` 里丢弃第一个元素。

选**实现侧**。理由是 `SysUserComposite` 是对外接口，方法名和注释读出来都是"取父级"；把过滤留在消费侧，等于每个外部实现者都要知道"这里含自身"这个隐含约定，而规格刚刚把"不含自身"写成契约。

代价：这是一个 public 方法的返回集合少一个元素，外部若已有代码依赖含自身的结果会受影响——与规格里的 BREAKING 是同一条。

### D2　接口方法同步改名，把语义写进名字

`getDeptListByDeptId(Long)` → `listAncestorDepts(Long)`。`ancestor` 本身就排他（不含自身），比 `parentDeptList` 更不容易再被读成"含自己"。

替代方案是保留原名只改语义。否决理由：这条语义恰好是本次裁定的核心，一个读不出排他性的名字会在下一次改动时被"顺手改回去"——这个仓库里已经有一份记忆在防这件事了。

**这条可以单独砍掉**，不影响其余决策与任务拆分：只要 D1 落地，语义就对了，改名只是把成本从读代码挪到改名的一次性扫描。仓库是公开的、无外部依赖方引用该方法名的话，改名的下游成本可控。

### D3　`ParentDept.level` 保留但重新定义，不删

链路不含自身后，`level` 若仍"从根数"会变成 2..n，很难读。改为**距自身的层级：直接上级 = 1，隔级 = 2**。

不删它的理由是：`ParentDept` 会随 `SimpleAuthObject` 序列化进菜单权限树接口回给前端，删字段是对外 JSON 结构的破坏；而重新定义只是数值口径变化。

必须同时写清一件事：**`level` 全仓库没有任何读取点，也不参与序列化**（`AbstractIdNameFlagAuthEntity.serialize` 只输出 id 与 name）。注释只写这一条能确证的事实，不给它编造用途。

### D4　`user == null` 时短路返回空权限集合，而不是抛

`UserAuthInfo.listBaseAuth()` 现在无条件 `user.getAuth()`。规格要求"权限体缺失时装载不得失败、权限集合为空"。

选择在 `user == null` 时**直接返回空集合**，连 roles/depts 也不汇总。理由是：一个连用户本身都装载不出来的权限体，产出的任何权限都不可信；让它去参与 `contains` 判定只会把装载层的缺陷伪装成数据问题。

替代方案一：抛异常。否决理由是无凭证用户会被彻底挡在门外，而 D5 之后那是完全合法的状态。替代方案二：构造期强制非空。否决理由见 Context——对象由 MyBatis 反射创建，构造期约束在这里没有着力点。

这与"唯一入口、不认识就抛"的既有姿态不冲突：那条针对的是**未知类型**，这里是**已知类型缺字段**，属于系统边界上的缺失输入，在边界处判定。

### D5　装载链的空值缺口在两处各自封堵，不做统一兜底

- `RbacSecurityUserDetailsService` 与示例的 `AuthBootSampleCurrentUserContextHolder` 都在无条件解引用 `authInfo`。改为**这两处各自判空**，缺失时给出空权限集合。
- `SysUserUnionCompositeMapper.xml` 的 `<collection property="users">` 改成映射单个 `user`（association），让 `user` 真的被灌上值。

不在 `SysUserCompositeImpl` 里塞一个"造一个空 UserAuthInfo"的兜底。理由是这两处缺口的成因不同（一个是没凭证提前返回，一个是映射没灌值），用一个兜底盖住会把第二种情况——映射写错——变成静默的"所有人都没有权限"，那是最难查的一类故障。规格要求的是"装载成功且权限为空"，不是"权限体永不为 null"。

替代方案：给 `UserAuthObject.authInfo` 加 `@NonNull` 并强制永不为 null。否决理由是它会让"无凭证用户"这个合法状态必须造出一个假的权限体。

### D6　rbac 示例用请求头切换身份，照 auth-core 示例的先例

`AuthBootSampleCurrentUserContextHolder` 现在硬编码 `admin`。三身份对照需要一个端点被三个用户使用，所以加一个按请求头指定用户名的口子，头部名沿用 auth-core 示例已有的 `X-Demo-User`，缺省仍为 `admin`。

替代方案是起三个测试上下文、各配一个默认用户名属性。否决理由是要建三份 Spring 上下文，慢且重复，而三种身份共享同一份权限配置——用上下文区分身份反而把"同一个配置"这个关键前提拆散了。

**边界**：这个口子只存在于 sample。MUST NOT 进框架模块，否则等于给生产环境开一个"请求头声明身份"的越权入口。

### D7　三身份对照的判据用 HTTP 状态码，不用服务层直调

菜单鉴权拒绝是外部可见结果，用响应码作判据成立，且这正是规格要求"不得用对内部方法的直接调用替代"的原因——直调 `hasMenuAuth` 只能证明判定函数认得这个串，证明不了从装载到拦截整条通路。

MockMvc 的用法照 `sample/basic-boot-sample` 已有的 `AbstractBasicSampleWebTest` 基类形态（`@SpringBootTest` + `@AutoConfigureMockMvc` + `@ActiveProfiles("h2")`），不另造一套测试装置。

### D8　auth-core 示例只演示"不对称"，不演示层级

它不能碰 `sys_dept`（规格禁止），为演示层级在自己的表里再造一棵树，等于在一个专门证明"核心层不依赖任何权限模型"的示例里重新引入模型层的形状——把示例的立论拆了。

改为：在示例自有的三件套权限实体里加一个继承型标记的实体（主键用整型，守住规格那条），演示两件事——该串在配置侧权限体上能无损往返，在用户侧权限体上反解必须当场失败。这就是把 `../../../../auth` 里那句"目前的设计也反解不出来"从描述变成能点的用例。

落点沿用该示例既有的四要素约定：新增端点 + `static/index.html` 用例 + README 场景矩阵一行 + 一条可执行命令里的断言。四件缺一不可，因为该能力规格要求"文档与脚本描述的是同一批用例"。

### D9　测试分层与所在体系

| 层 | 位置 | 体系 | 钉什么 |
|---|---|---|---|
| 编解码往返 | `auth-rbac` 单测 | JUnit 4 | `RELATIONDEPT` 串 → 对象 → 串一致；标记单段令牌规则 |
| 用户侧展开 | `auth-rbac` 单测 | JUnit 4 | 父链展开出的串集合；不含自身；`user==null` 得空集合 |
| 层级畸形 | `auth-rbac` 单测 | JUnit 4 | 自引用/成环当场终止 |
| 端到端 | `auth-rbac-boot-sample` | MockMvc | 三身份 200/403 对照 |
| 反解不对称 | `auth-core-boot-sample` | 示例既有用例装置 | 往返一致 + 反解明确失败 |

auth-rbac 现有测试是 JUnit 4（`org.junit.Assert` / `org.junit.Test`），auth-core 侧是 JUnit 5。新增的用**所在模块的**体系，不跨模块统一——把一个模块改成 JUnit 5 是无关的改动足迹。

## Risks / Trade-offs

- **[MyBatis 聚合行为 —— 实现期已实测，结论是"不聚合"，已修]** 实测到的现象：根节点没有 `<id>` 时，两行 join（一个用户隶属两个部门）产出两个 `UserAuthInfo`，单值返回的查询抛 `TooManyResultsException`；即"空 CacheKey 视为同一父行"这个推断是错的，嵌套集合的鉴别列参与了父行身份判定。既有种子数据里每个用户恰好只命中一行 join 结果，所以这个缺陷从未暴露。修法是在根节点补 `<id property="userId" column="user_id"/>` 并在 `UserAuthInfo` 上加对应的所属用户 ID 字段，父行键由此只剩用户维度；补上后同一用例转绿。由 `DeptInheritanceSampleTest#multiDeptUserLoadsOneAuthInfoCarryingBothDepts` 钉住。
- **[存量 `RELATIONDEPT` 授权被收窄]** 去自身后，已经按"含自身"配出去的授权会失去对本部门成员的覆盖。→ 缓解：已确认仓库内 `docs/sql` 与两个示例的种子数据中**没有任何 `RELATIONDEPT__` 行**，因此本次改动不冲击仓库内数据；外部使用者需自行核对，这条要在归档说明里点名。
- **[`level` 数值口径变化影响前端]** 从"根=1"改成"直接上级=1"，且自身不再出现在链路里，前端若展示过层级数字会变。→ 缓解：`level` 不参与判定，最坏是展示口径变化；把它列进 Open Questions 让前端确认，不为此保留旧口径。
- **[`DEPT` key 竞争让单测在不同环境翻脸]** `DeptWithParents` 不覆写 `flag()`，与 `Dept` 抢注册表同一个 key，谁赢取决于 classpath 扫描顺序。→ 缓解：单测只断言 id 与权限串，**不断言 `getDepts()` 里元素的具体运行时类型**。这是既有缺陷，本次不修（修它要给 `DeptWithParents` 定一个新标记，属于另一条语义决策），但要让测试对它免疫。
- **[种子数据非幂等]** `seed-data.sql` 头部已声明带显式主键、非幂等，只在 `h2` profile 自动执行。新增部门树行沿用同一形态，不给这个文件引入幂等外壳。→ 缓解：MySQL 使用者需手动重新导入一次；proposal 的 Impact 已点名，H2 侧每次全新 schema 所以无影响。
- **[新增权限实体注册后 `flag()` 撞名]** 静态块是 `put`，同名标记后注册者覆盖前者。`RELATIONDEPT` 目前唯一。→ 缓解：单测里对"标记必须是单段令牌"的断言顺带覆盖到标记注册的可解析性；不做启动期重名校验（那是另一条独立的能力，规格没要求）。

## Migration Plan

1. 无表结构迁移——`authority` 是 varchar，权限串长度与前缀都在现有列宽内。
2. 装载修复（D5）与语义去自身（D1）先落地，二者必须同批：只修装载不去自身，端到端用例会因为本部门成员也被放行而断言不成立；只去自身不修装载，`user` 仍是 null 走不到判定。
3. 单测与示例种子随后，示例用例作最终验证载体。
4. **回滚**：revert 代码即可，但已按"严格子孙"配出的 `RELATIONDEPT` 授权在回滚后会重新覆盖本部门成员——回滚不是无损操作，需要在回滚说明里带上这一句。

## Open Questions

- 前端是否读取或展示 `ParentDept.level` 的数值。只影响展示口径，不影响规格、方案与任务拆分，可在实现后单独确认。

## 归档说明必须写明的对外后果

以下四条各有一句可直接抄进归档说明，不依赖对话记录：

1. **`SysUserComposite` 契约变化（BREAKING）**：`getDeptListByDeptId(Long)` 已改名为 `listAncestorDepts(Long)`，且返回内容从"含本部门的完整链路"变为"严格上级链路，不含本部门"。外部实现方与调用方需同步修改；仓库内已核对只有接口声明、实现类、`UserAuthInfo.fillUserAuth` 三处。
2. **已按"含自身"配置的存量 `RELATIONDEPT` 授权会被收窄**：这类授权此前覆盖被授权部门自己的成员，改造后不再覆盖，需要改为追加一条 `DEPT__<本部门ID>`。仓库内 `docs/sql` 与两个示例的种子数据中已确认不存在 `RELATIONDEPT__` 的存量行，因此本次改动不冲击仓库内数据；外部使用者需自行核对自己库里的 `sys_auth_menu`。
3. **回滚不是无损操作**：代码可 revert，但改造期间按"严格子孙"语义配出去的 `RELATIONDEPT` 授权在回滚后会重新覆盖本部门成员，权限面变大。回滚前应先导出 `sys_auth_menu` 中该标记的行。
4. **两个此前未暴露的装载缺陷已修，且 `UserAuthInfo` 多了一个对外字段**：用户权限体的 MyBatis 映射仍指向已删除的 `users` 集合字段（导致 `user` 永不为空的前提落空），以及根 resultMap 缺鉴别列导致隶属多个部门（或多个角色）的用户被拆成多个权限体、查询抛 `TooManyResultsException`。后者是既有缺陷，此前每个示例用户恰好只命中一行 join 结果所以从未触发。修复给 `UserAuthInfo` 增加了 `userId` 字段作为父行鉴别列，该字段会出现在权限体的序列化结果里。
