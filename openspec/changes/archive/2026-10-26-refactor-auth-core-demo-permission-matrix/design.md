# Design

## Context

见 `proposal.md` 的 Why/What。实现层面的现状与约束如下：

- 验证台是单个静态文件 `sample/auth-core-boot-sample/src/main/resources/static/index.html`，纯 HTML + fetch，无前端构建、无模板引擎；当前 52 条用例定义在 `S` 数组里，每条带写死的 `demoUser`，`run()` 直接按该身份执行。
- 顶部下拉框只被 `whoami`（当前权限回显）和自由调用读取；README 4.1 对页面语义的描述已与实现不一致。
- 2026-10-22 的 `clarify-auth-core-demo-identity-scope` 裁决（用例身份固定、手工身份仅回显）被本变更取代；本次的边界是"主矩阵身份=镜头，规格断言=脚本/测试的固定夹具，页面不执行写操作"。
- 示例必须继续只依赖 auth-core：不能引用 rbac 层、不能改框架代码；所有新逻辑只能落在示例自己的 controller / 静态页 / 种子脚本里。
- 判定入口已经存在且可复用：
  - 菜单：`DemoMenuAuthHandler.hasMenuAccessAuth(auths, path)`（bean 在 `DemoAuthConfig` 声明），授权来源 `DemoMenuAuthMapper.listAuthsByPath(path)`。
  - 行级：`AuthDataHandler.hasDataOperateAuths(auths, businessFunction, dataIds, operate)` 与 `getDataAuths(businessFunction, dataId, operate)`；bean 是示例的 `DemoAuthDataHandlerProbe`，它只额外记一笔 judge 记录，判定逻辑委托框架的 `JdbcAuthDataHandler`。
  - 当前身份：`DemoAuthContextHolder` 从 `X-Demo-User` 读取并每次查 `demo_user_auth`；无身份时权限集合为空。
- `DemoSqlTrace` 只在请求带 `X-Trace-Id` 时记录；不带追踪头的请求零记录，因此只读预检不会污染证据面板。
- `verify.sh` 是断言的权威载体；`README.md` 第 6 节是场景矩阵。示例规格 `sample/auth-core-verification-demo` 要求"文档 + 可执行脚本同源"，本变更不改变这一要求，只改变页面的呈现职责。

## Goals / Non-Goals

**Goals:**

- 页面只有一个视图：主表一行一个判权场景，顶部身份下拉框驱动整张主表；切换身份自动刷新只读判定。
- 每条判权行分层展示"菜单 / 行级 / 数据范围"的结果与依据，让"能进接口"和"能动数据"不再混成一个 403。
- 主表显示当前身份的**期望结果**与**实际结果**，逐条给出通过/不通过；“测试当前身份”一键重跑并汇总。
- 顶部“当前身份能做什么（既定）”总览把可执行、接口可进但数据不可操作、不可进入、可查看的数据、看不到的数据、不适用分开显示，切换身份即更新。
- 切换身份不产生任何业务写入或副作用；真实调用由使用者显式触发。
- 原 52 条用例的场景覆盖不丢失：判权场景进主表，能力/规格/启动/重启场景进可折叠的覆盖脚注，逐条指向 `verify.sh` 或测试类。
- `verify.sh` 的编号与断言主体保持稳定；只做必要的增量（C22 收紧、可选的只读端点一致性断言）。

**Non-Goals:**

- 不修改 `../../../../auth` 框架代码、框架规格或任何权限判定语义。
- 不引入第二个验证 Tab、第二套身份概念或第二套权限实现。
- 不从页面执行 C15/C19/C20/C21 这类有副作用或多步骤的规格断言。
- 不重构 52 条用例的原始编号，不改 `verify.sh` 的用例划分。
- 不改变示例的 H2/MySQL profile、建表脚本结构或整型主键约束。

## Decisions

### D1. 单视图：主表只承载判权场景

主表渲染 `type: 'perm'` 的行；`cap`（与身份无关的能力）、`script`（verify.sh 断言）、`test`（自动化测试）三类行渲染在页面底部可折叠的"覆盖核对"里。理由是：主表的每一行都必须能回答"这个身份有没有这个接口/数据的权限"；把 `noCarrier → 400`、`trace 可观测`、`启动失败` 这类行混进去只会稀释身份列的信息量。

主表列为“用例 / 判定层 / 期望（当前身份）/ 实际 / 结果 / 依据”：期望来自 D11 的身份真值表，实际来自只读预检与数据范围 GET，结果逐条比较；这样既保留“一行一个场景”，又能在列表里直接拿到既定结果。

替代方案：保留双 Tab（用户明确否决）；把所有行都放进主表并加类型徽标（会让主表变成混合清单，仍不直观）。

### D2. 身份是镜头，不是用例字段

`S` 数组中删除 `demoUser`；每条 `perm` 行只声明场景（菜单路径、行级槽位、数据范围调用）。页面在每次请求时统一带当前下拉框的 `X-Demo-User`；`''` 是显式的"无身份"选项，不回退、不继承。后端只读端点也只从 `DemoUserContext` 取身份，不接受请求体里的用户名参数，避免页面能"扮演"任意人。

替代方案：在每条用例上保留"参照身份"字段用于展示（会让身份重新变成用例的一部分，且容易再次出现"看起来全局、实际不驱动"的误解）。

### D3. 三层判定与结论优先级

每一行按声明的能力层渲染：

| 行类型 | 菜单列 | 行级列 | 数据列 | 结论 |
| --- | --- | --- | --- | --- |
| 仅菜单 | 通过/拒绝 + 依据 | — | — | 允许 / 拒绝（菜单） |
| 仅行级 | 未管辖 | 每个 ID 的通过/拒绝 + 依据 | — | 允许 / 拒绝（行级：ID n） |
| 菜单+行级 | 通过/拒绝 | 每个 ID 的通过/拒绝 | — | 先判菜单；菜单拒绝即"拒绝（菜单）"，否则看行级 |
| 数据范围 | 未管辖 | 未声明 | 可见行集 + 行数 | 可见 n 行 / 空 |

"未管辖"与"未声明"必须显式显示，不能显示成拒绝：前者表示该端点没有 `@AuthMenu`，后者表示没有 `@AuthDataOperation`。数据范围行即使返回空数组也是 200，结论写"空"而不是"拒绝"。

如果某次预检或查询本身失败（HTTP 5xx、非 JSON、端点异常），结论写"判定失败/查询失败"，不能显示成"拒绝"——否则使用者会把系统错误误读成权限结论。

页面在分层结果之外再显示“期望（当前身份）”和“结果”：期望来自 D11 的真值表（`allow` / `menu` / `row` / 空 / `n/a` / 数据ID），实际按上述优先级归类，两者一致为通过、不一致为不通过，`n/a` 与实际 `n/a` 记为不适用。菜单已拒绝时实际列只写“菜单：拒绝”，不再展示行级预检，避免误读成两层都执行了。

替代方案：只显示最终 200/403（会丢掉"菜单能进但数据不能动"这个最有价值的区分）；用响应消息文本在 JS 里猜拒绝层（脆弱且与框架文案耦合；预检端点直接返回分层结果更稳）。

### D4. 只读预检端点 `/demo/permission-view`

新增示例级端点（放在 `DemoMetaController` 或独立 controller，路径在 `/demo` 下、无 `@AuthMenu`）：

请求体：

```json
{
  "menuPaths": ["/methodLevel/guarded", "/classLevel/all"],
  "dataChecks": [
    {"businessFunction": "work_order", "operate": "edit", "ids": [1, 4]}
  ]
}
```

响应体：

```json
{
  "username": "alice",
  "auths": ["ROLE__10", "USER__1"],
  "menu": [
    {"path": "/methodLevel/guarded", "required": ["ROLE__10"],
     "matched": ["ROLE__10"], "missing": [], "ok": true}
  ],
  "data": [
    {"businessFunction": "work_order", "operate": "edit",
     "ids": [
       {"id": 1, "required": ["ROLE__10"], "matched": ["ROLE__10"], "missing": [], "ok": true},
       {"id": 4, "required": ["ROLE__10"], "matched": [], "missing": ["ROLE__10"], "ok": false}
     ]}
  ]
}
```

实现要点：

- `auths` 取 `DemoAuthContextHolder.getAuths()`；`username` 取 `DemoUserContext.username()`，仅供回显。
- 菜单判定调用 `DemoMenuAuthHandler.hasMenuAccessAuth(auths, path)`；`required` 用 `DemoMenuAuthMapper.listAuthsByPath(path)` 取，`matched`/`missing` 由权限串集合求差得到（只是展示，不参与判定）。
- 行级判定对每个 ID 单独调用 `AuthDataHandler.hasDataOperateAuths(auths, module, [id], operate)`，这样页面能显示"哪个 ID 未通过"；`required` 用 `getDataAuths(module, id, operate)` 取。逐 ID 调用与切面一次传整批的"全部命中才放行"语义一致。
- 不调用任何业务方法；不写 `demo_execution_log`；不写 `demo_auth_data`。`DemoAuthDataHandlerProbe` 的 judge 记录在没有 `X-Trace-Id` 时不会落盘，因此预检请求不带追踪头。
- 请求体做基本上限保护（菜单路径 ≤ 50、dataCheck ≤ 50、每个 IDs ≤ 100），超限返回 `AuthServiceException` 的 400，而不是静默截断。
- 空列表合法：返回空结果。无身份时 `auths` 为空，菜单/行级判定均为 false；数据范围行由页面发真实 GET，结果为空。

替代方案：给业务端点加 `checkOnly` 参数（污染业务 API）；在 JS 里按种子数据推算期望（变成第二套权限实现，迟早不一致）；切换身份时真实调用所有端点（会写副作用表、改业务数据、翻转权限槽位）。

### D5. 页面只读、真实调用手动触发

主表的“测试当前身份”只发一个 `/demo/permission-view` 请求 + 若干只读 GET（数据范围行），不发任何写请求；切换身份会自动测试一次，按钮用于重跑。测试结果在页面内与 D11 的期望真值表逐条比较。每行提供一个"真实调用"按钮：它只把方法、路径、请求体填入页面底部现有的"自由调用"面板，并把身份设为当前下拉框，**不自动发送**；使用者点"按当前身份发送"才真正执行。这样既保留了端到端观察能力，又避免切换身份时产生写入。

数据范围行在自动刷新时发真实 GET（本来就是只读），**顺序发送**；每个请求带独立的 `X-Trace-Id`，但不立刻取 trace；行内的"证据"按钮再按需拉 `/demo/trace?traceId=...`，展示注入后的 SQL。写操作行的证据由自由调用面板按需产生。

替代方案：所有行都在切换时真实执行（会污染 `demo_execution_log`、改变工单标题、触发 C19 的授权/清空）；只显示预检、完全去掉真实调用（会让页面失去端到端证据）。

### D6. 用例目录与 P 编号

`S` 数组改为带 `type` 的目录：

```js
{ id: 'P7', type: 'perm', name: '编辑工单 1', covers: ['C1', 'C2', 'C12b'],
  menu: '/workOrder/update',
  data: { module: 'work_order', operate: 'edit', ids: [1] },
  real: { m: 'POST', p: '/workOrder/update', b: { id: 1 } } }

{ id: 'P17', type: 'perm', name: '工单 unified 列表', covers: ['D1', 'D6', 'D15'],
  range: { m: 'GET', p: '/workOrder/rangeList', sum: 'workOrderIds' } }

{ id: 'K1', type: 'cap', name: '用法错误 400', covers: ['C6', 'C7', 'C8'],
  where: 'verify.sh C6-C8' }

{ id: 'S1', type: 'script', name: '授权后翻盘', covers: ['C19'],
  where: 'verify.sh C19' }

{ id: 'T1', type: 'test', name: '缺上下文启动失败', covers: ['A1', 'A2', 'A3'],
  where: 'StarterAssemblyTest' }
```

合并规则只合并"同一个调用、不同身份"的行；不同接口、不同数据 ID、不同槽位保持独立。P 行清单：

| P | 场景 | 判定层 | 覆盖原编号 |
| --- | --- | --- | --- |
| P1 | `GET /methodLevel/guarded` | 菜单 | B1a、B4（无身份镜头） |
| P2 | `GET /classLevel/all` | 菜单 | B2a、B10（改名后半边在 verify.sh） |
| P3 | `GET /classLevel/two` | 菜单 | B2b |
| P4 | `GET /methodLevel/sameDocA` | 菜单 | B9a |
| P5 | `GET /methodLevel/sameDocB` | 菜单 | B9b |
| P6 | `GET /deptInherit/ledger` | 菜单 | B11、B12 |
| P7 | `POST /workOrder/update {id:1}` | 菜单+行级 | C1、C2、C12b |
| P8 | `POST /workOrder/approve {id:1}` | 菜单+行级 | C12a |
| P9 | `POST /workOrder/batchApprove {ids:[4,5]}` | 菜单+行级 | C3 |
| P10 | `POST /workOrder/batchApprove {ids:[4,1]}` | 菜单+行级 | C4 |
| P11 | `POST /asset/scattered` | 菜单+行级 | C5 |
| P12 | `POST /asset/updateIntegerForm {assetId:101}` | 菜单+行级 | C13 |
| P13 | 管理动作（`/authData/*`，槽位 900） | 菜单+行级 | C22；C15/C19/C20/C21 的判权半边 |
| P14 | `POST /probe/quote/call {rawId:900}` | 行级 | C16a |
| P15 | `POST /probe/quote/call {rawId:901}` | 行级 | C16b |
| P16 | `POST /probe/call {id:801}` | 行级 | C11（无身份镜头） |
| P17 | `GET /workOrder/rangeList` | 数据范围 | D1、D6、D15 |
| P18 | `GET /workOrder/editable` | 数据范围 | D4 |
| P19 | `GET /workOrder/approvable` | 数据范围 | D4b |
| P20 | `GET /workOrder/twoSlots` | 数据范围 | D5 |
| P21 | `GET /workOrder/nested` | 数据范围 | D3 |
| P22 | `GET /workOrder/joinCu` | 数据范围 | D7a |
| P23 | `GET /workOrder/joinCb` | 数据范围 | D7b |
| P24 | `GET /asset/list` | 数据范围 | D8 |
| P25 | `GET /workOrder/dynamic?status=open` | 数据范围 | D11 |

P13 的 `real` 指向 `/authData/grant` 的一个安全示例请求；页面不自动发送，只有使用者点"按当前身份发送"才执行。

覆盖脚注按 `cap`/`script`/`test` 分组列出 `K*`/`S*`/`T*` 行，至少覆盖：B1b、B3、B5、B6、B7、B8、C6-C10、C15、C17-C21、D2、D9、D10、D11b、D12、D13/D14、E1-E3、A1-A5。页面在加载时用一份 `EXPECTED_IDS` 常量校验 `union(covers)`，缺失的编号在脚注顶部红色列出，防止以后改用例时悄悄漏掉场景。

### D7. C22 的判据修正

现状：`/authData/grant|remove|fill` 的菜单授权只给 `ROLE__10`，`demo_admin/900/unified` 行级槽位也只给 `ROLE__10`。bob 调 `/authData/grant` 时在菜单层被拒，`DataCheckAspect` 没有执行，C22 标称的"行级管辖"并未被验证。

修正：

- `sql/seed-data.sql` 为 `/authData/grant`、`/authData/remove`、`/authData/fill` 各补一条 `ROLE__20` 的菜单授权（保持 `WHERE NOT EXISTS` 幂等）；`demo_admin/900/unified` 仍只给 `ROLE__10`。
- 结果：alice 菜单 ✓、行级 ✓ → 允许；bob 菜单 ✓、行级 ✗ → 拒绝，消息为 `权限校验不通过`。
- `verify.sh` 的 C22 在原有 `code=403` 之外增加 `grep -q '权限校验不通过'`，证明拒绝来自行级而不是菜单。若该 seed 修正被推迟，C22 的消息断言也应一并推迟，不能只改断言不改数据。

替代方案：新增一个只持 `ROLE__20` 且菜单权限不同的用户（多一套身份数据，没必要）；把 `/authData/*` 的菜单声明去掉，只留行级（会改变"管理动作也受菜单管辖"的演示语义）。

### D8. 文档与编号

- `README.md` 4.1 改写：身份下拉驱动整张主表；主表是“用例 × 身份”的权限透视，显示期望/实际/结果；切换只跑只读判定并自动测试，“测试当前身份”一键重跑；顶部有身份能力总览；"真实调用"只预填自由调用；覆盖脚注指向脚本/测试。
- `README.md` 第 6 节保留原场景矩阵与编号；新增 6.1 "页面判权行与原编号的映射"（即 D6 的 P 表），并删除"页面里的用例编号与 verify.sh 完全一致"的旧说法；新增“每个 P 行带 7 身份期望真值表、一键测试比较期望/实际”的说明。判权期望由页面真值表承担，副作用/启动/其余规格断言仍以 verify.sh/测试为准。
- 旧的 "52 条用例逐条标注身份" 描述改为"约 25 条判权行 + 覆盖脚注"。
- 本变更取代 2026-10-22 的身份边界；apply 完成后同步更新项目记忆中的身份语义，不再保留"手工身份仅回显"的旧结论。

### D9. 规格增量

不新增或修改 `openspec/specs/` 下的规格：页面是示例工具，`sample/auth-core-verification-demo` 的"文档 + 脚本同源"要求仍由 README 矩阵与 `verify.sh` 满足；框架与示例的规格级行为不变。变更元数据使用 `skip_specs: true`。

### D10. 含引号权限串的身份不进入数据范围路径

范围改写目前把权限串按字面量拼进 SQL（README 第 10 节已知债务）。`/workOrder/rangeList`、`/asset/list` 等数据范围行对任何身份都会真实发 GET；erin 的权限串 `ROLE__9' OR '1'='1` 一旦进入就会让 H2 报类型转换错误，页面会把已知债务误显示成“查询失败”，也会破坏原有“引号串只走参数绑定路径与 quote_probe”的边界。

实现：刷新时先取预检响应的 `auths`（失败时退回 `current-user.rawFromStore`），只要任一权限串含单引号，就跳过全部数据范围 GET，行内显示“不适用”并指向 README 第 10 节；summary 单独计数“不适用”，不计入拒绝或失败。该处理只发生在示例页面，不修改 `SqlAuthInject`，债务保持原样。

替代方案：在页面直接展示 500（会把框架债务表现成权限结论）；现在修 `SqlAuthInject`（属于明确的框架改动，超出本变更“不动框架代码”的边界）。

### D11. 身份真值表、一键测试与能力总览

页面内维护一张固定的身份真值表 `EXPECT`：25 个 P 行 × 7 个身份（alice / bob / carol / dave / erin / frank / none）。取值是 `allow`（允许）、`menu`（菜单拒绝）、`row`（行级拒绝）、`空`（数据范围为空）、`n/a`（引号串范围债务，不执行）或数据范围应返回的业务数据 ID（P20 用 `edit ...|approve ...|editAgain ...` 三段式 key）。无身份下拉值是空串，查找时归一化为 `none`。

- 实际分类：菜单/行级按 `res.menu.ok` 与每个 ID 的 `ok` 归类；数据范围用 `RANGE_KEYS` 归一化成同一套 key；`rangeSkip` → `n/a`；预检/查询错误 → `error`。
- `resultOf` 比较期望与实际：一致 `pass`，不一致 `fail`，`n/a` 对 `n/a` 记 `skip`；主表逐行显示通过/不通过/不适用，summary 汇总。
- “测试当前身份”按钮与切换身份都走同一条只读刷新；不写任何业务表或副作用表。
- 身份能力总览直接由 `EXPECT` 生成（不依赖实际请求），分为：可执行（`allow`）、接口可进但数据不可操作（`row`）、不可进入（`menu`）、可查看的数据（数据 ID）、看不到的数据（`空`）、不适用（`n/a`）。切换身份即更新。
- 真值表是示例的“既定结果”规范，按种子数据维护；它不是第二套权限实现，实际判定仍由框架的 handler 给出。改种子或 P 行时必须同步真值表，漂移会在主表立即显示“不通过”。

替代方案：从种子数据运行时反推期望（会复制权限规则，且期望退化成实际，失去测试意义）；只在 `verify.sh` 里断言、页面只显示实际（用户明确反馈太隐晦，无法一眼看到“既定结果”和“通过/不通过”）；按人拆用例（用户最初就否决）。

## Risks / Trade-offs

- [只读预检与真实拦截的判定漂移] → 预检直接调用拦截器/切面使用的同一组 handler（`hasMenuAccessAuth`、`hasDataOperateAuths`），不复制规则；在 `verify.sh` 增加 2-3 条只读行的一致性断言（预检结论与真实调用 code 一致），把漂移变成可发现的问题。
- [未管辖/未声明被误显示成拒绝] → 行定义里显式区分 `menu: null`、`data: null`、`range: null`，页面分别渲染"未管辖/未声明/—"；只有显式声明且判定为 false 才显示"拒绝"。
- [预检调用探针产生 judge 记录] → 预检请求不带 `X-Trace-Id`，`DemoSqlTrace` 不记录；真实 GET 的 trace 独立按需拉取。
- [逐 ID 判定与切面整批判定的语义差异] → 切面语义是"任一 ID 未命中即整体拒绝"；逐 ID 调用只是把同一判定拆开以显示依据，页面结论仍按"全部 ID 通过才允许"合成，不改变放行条件。
- [切人自动刷新产生大量请求] → 一个批量预检请求 + 9 个只读 GET，**顺序发送**；示例 `maximum-pool-size=5`，范围请求在拦截器持有一条连接的同时还要取一条鉴权连接，9 路并发会耗尽连接池。trace 只在行内按需拉取；对同一身份的重复切换做去抖。
- [C22 seed 修正改变拒绝层与消息] → 同步收紧 `verify.sh` 的 C22 断言；`demo_menu_auth` 行数增加不影响 B6 的 `demo_menu_api` 行数与 E3 的重启前后行数比较。若选择推迟，C22 的断言也不收紧，并在 README 保留现状说明。
- [页面单文件复杂度上升] → 用例目录保持数据驱动（`type` + `covers`），渲染逻辑按类型分支；不引入构建工具或第三方依赖。
- [旧决策被推翻] → README 与项目记忆同步改写；新的边界明确写成"主矩阵身份=镜头、规格断言=固定夹具、页面不写数据"，避免下次再按旧规则改回去。
- [有副作用的规格用例从页面消失] → 覆盖脚注明确指向 `verify.sh`；README 4.1 说明"页面不用于跑副作用断言"，避免使用者误以为页面能替代脚本。
- [含引号权限串撞上范围字面量拼接] → 页面检测 `auths` 中的单引号并跳过数据范围 GET，显示“不适用”；README 第 10 节的债务保持原样，不在本次修复。
- [真值表与种子数据漂移] → 真值表是页面的期望规范，按种子维护；一键测试逐条比较，漂移会立刻显示“不通过”。改种子或 P 行时必须同步 `EXPECT`。
- [一键测试被误读为写操作测试] → 只做只读预检/GET，summary 和页脚说明副作用断言在 `verify.sh`；页面的“真实调用”仍需手动发送。

## Migration Plan

1. 新增 `/demo/permission-view`（示例 controller + 请求/响应 DTO），不启动数据库迁移。
2. 改写 `index.html`：单视图主表、期望/实际/结果、一键测试、身份能力总览、覆盖脚注、真实调用预填；删除逐用例 `demoUser` 与旧 `runAll`。
3. 修改 `seed-data.sql` 的三条 `/authData/*` 菜单授权（幂等）；重启应用后自动生效，无需删库。
4. 更新 `README.md` 4.1、6 节（含真值表/一键测试说明）；可选在 `verify.sh` 增加 C22 消息断言与只读端点一致性断言。
5. 验证：从模块目录启动（`cd sample/auth-core-boot-sample && java -jar ...`），打开 `/coredemo/`，依次切换 alice/bob/carol/dave/erin/frank/无身份，确认 alice/bob/carol/dave/frank/无身份为 25/0/0、erin 为 16/0/9，主表无“不通过”；执行 `bash verify.sh` 全量通过；再用 curl 对比 `/demo/permission-view` 与真实接口在 2-3 条只读行上的结论。
6. 回滚：还原 `index.html`、`README.md`、`seed-data.sql`、`verify.sh`，移除新增 controller/DTO；已插入的 `ROLE__20` 菜单授权可用 SQL 删除，或删除 H2 数据文件后重启恢复种子。

## Open Questions

无。P 行清单、覆盖映射与 C22 修正已在本文固定；实现细节按 `tasks.md` 执行。
