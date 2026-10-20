# Design

## Context

行级写权限链路是 `@AuthDataOperation` → `DataCheckAspect` → `AuthDataHandler` → `JdbcAuthDataPersistent`（直接经 `JdbcTemplate` 访问 `sys_auth_data`），与 `@AuthDataRange` 那条"改写 Mapper 语句"的链路只在权限表上相交，实现互不复用。

约束与已确证的现状：

- 切面从实参里用 `filter(instanceof IdDataEdit).findFirst()` 取判定对象，因此一次调用只判定一个 ID。
- `JdbcAuthDataPersistent` 用 jsqlparser 的表达式类组装 AST，最终 `.toString()` 出 SQL 文本交给 `jdbcTemplate.update(sql)` / `query(sql, rm)`，**没有任何绑定参数**。实测 jsqlparser 4.7 的 `StringValue.toString()` 不转义单引号（`new StringValue("1' OR '1'='1")` 渲染为 `'1' OR '1'='1'`），`LongValue(String)` 同样原样透传。
- 权限串的 `description` 一半来自 `IFlagAuthEntity.getAuth()`，其实现在 `AbstractIdNameFlagAuthEntity.serialize()` 返回 `Pair.of(id.toString(), name)`；`name` 在示例链路里由 `@RequestBody` 反序列化得到，因而可被调用方控制，并被拼进 INSERT 的字面量。
- `listAuthData` 的 RowMapper 用**传入业务 ID 的类型**去读权限表自己的主键：主键该读成 `long`、`int` 还是 `String`，取决于业务数据ID是 `Long`/`Integer`/`String`；两条删除路径对同一列的字面量写法也不一致（查询侧恒用字符串字面量，按业务功能删除侧用数值字面量）。注：`removeByIdsSql` 原先把 `String` 误判成 `instanceof StringValue`，已由 `9171a0a` 修正为 `instanceof String`；本变更不依赖那处修复，类型分支随参数绑定整体删除。
- 数据范围侧的字面量来源只有注解常量与 `BaseAuth.getAuth()`（由标记与各位置 ID 拼成，仅含数字与分隔符），`description` 不参与注入；且该侧必须用 SQL 解析器改写语句 AST。
- `sys_auth_data` 的 `data_id` 列在框架 DDL 中是 `bigint`，五份 sample 共用同一份 DDL；h2 每次启动重建 schema。
- 该注解目前没有任何活着的测试（示例模块唯一测试类的 `@Test` 被注释）。

动机见 proposal.md - Why；行为要求见 `specs/auth/data-operation/spec.md`。

## Goals / Non-Goals

**Goals:**

- 把判定单位从"一次调用一个 ID"改为"一次调用全部 ID"，并把这一变化做成可回归验证的行为。
- 让权限表的读写不再把任何调用方可控的值解释为查询内容，且不依赖"转义写对了"。
- 让业务主键的类型（整型、长整型、字符串）成为对框架透明的细节。
- 改动前先有对照测试，改动后有能证明注入已闭的断言。

**Non-Goals:**

- 不改数据范围侧（`AuthDataRange`、`SqlAuthInject`、`AuthRangeCache`、`MybatisDataRangeInterceptor`、`AuthRangeStartupValidator`）与 `auth/data-range` 规格。
- 不改 `sys_auth_data` 的列类型与 DDL 形状；字符串主键业务的列类型适配留到该场景真正出现时。
- 不引入行级操作权限的启动期校验器（保持运行期失败，但失败分类先做对，为将来上移留接缝）。
- 不把 `editDataAuths`、`getDataAuths`、`fillDataAuths`、`deleteBusinessFunctionAuthByDataId` 改成批量形态——它们的调用语义本就是"针对一条数据"。
- 不改动 `operate` 默认值 `unified` 的取值语义。

## Decisions

### D1 判定范围由承载对象交出，而不是切面猜

承载契约从 `getDataId()` 改为交出 `Collection<ID>`，切面收集实参中所有承载对象（直接实参加一层数组/集合元素）的 ID 后汇总去重。

- 备选：切面反射下钻对象内部字段找出所有像 ID 的字段——被否，属魔法且规模不可控，判定范围变得不可解释。
- 备选：保留单值契约、要求业务把批量拆成多个参数——被否，`List<Req>` 这种最自然的批量写法仍会落到"找不到判定对象"。
- 现契约的 `findFirst()` 是静默越权面：一旦某DTO 内部装多个 ID 而只交出一个，其余全部放行。

边界只到"一层集合元素"，更深的嵌套由业务在 `getDataIds()` 里自行摊平——它本来就知道自己的结构。

### D2 权限串留在内存求交，代价是必须做归一化键

SQL 只按业务功能、操作类型与候选 ID 集合过滤，把候选行的授权记录取回后在内存求交；用户权限串不进 SQL 的 `IN` 列表。

- 理由：当前登录人的权限集合规模不可控（可达数百上千），把它变成占位符会让 SQL 体积由登录态决定；而候选 ID 的规模已被 D3 限制在 100 内。
- 备选：`authority IN (...)` 整体下推、由数据库判定——被否（同上），且那样会让一次校验的开销随角色数线性增长。
- 该选择的配套义务：两侧都转 `Set` 求交（避免嵌套遍历），且比对键统一 `String.valueOf(...)` 归一化。不归一化会因 `Long 1` 与 `Integer 1` 落不进同一个 Map 键而把已授权的调用误判为拒绝——这是正确性问题，不是风格问题。

### D3 ID 上限 100，硬编码常量，不内部分批

超出直接以配置类错误失败且不发查询。分批会把"一次请求的授权边界"模糊成多次判定，也让失败语义无法回答"是哪一批失败"。放开上限时改一个常量即可，不进配置项以免形成新的碎形开关。

### D4 值改为 `JdbcParameter` 绑定，语句结构继续由 JSqlParser 构造

四个读写方法的值全部换成占位符 + 参数数组执行；`data_id IN (?,...)` 的占位符数量由去重后的 ID 数决定（上限 100，因此占位符数量有界）。**SQL 仍然在 JSqlParser 的 AST 上组装**（表、列、条件、值列表），只是叶子位置从 `StringValue`/`LongValue` 换成 `JdbcParameter`。

- 为什么不用手写字符串拼 SQL：本框架的 SQL 构造统一走解析器，`AuthDataTable` 暴露的就是解析器的 `Table`/`Column` 对象；一旦改成手写字符串，复杂语句（表不在最外层、别名、多表）的表达能力就丢了，标识符的引号处理也重新变成自己负责。实测 4.7 的 `JdbcParameter` 可以正常进 AST 并按书写顺序渲染：`WHERE business_function = ? AND operate = ? AND data_id IN (?, ?, ?)`。
- 收益是三个问题一起消解：注入面从"依赖转义正确"变成"值不进语句文本"；业务主键直接绑原始 `Object` 因而天然支持字符串主键；`removeByIdsSql` 的 `Long/Integer/StringValue` 分支与 `ID类型异常` 整段删除，不需要补 String 分支。
- 备选：只给 `removeByIdsSql` 补一个 `String` 分支——被否，那保留着无转义的字面量，下次新增主键类型还得再补，且注入面不动。
- 备选：把这个类的持久化整体迁到 MyBatis——被否，权限表读写是框架内部基础设施，引入 mapper 反而与"语义不落 mapper"的既有取向冲突。
- 表名与列名继续由 `AuthDataTable` 的配置映射提供并参与语句构造：其值来自部署配置而非请求，属可信来源。

### D5 权限表主键"读回什么类型就绑回什么类型"

RowMapper 不再依据传入业务 ID 的类型去解读权限表主键，直接以 `getObject` 读回、回删时原样绑定。

- 备选：恒按 `getLong` 读（断言主键必为 `bigint`）——被否，框架自带的 DDL 是这样，但表名与列名都可配，断言别人的表形状不合理。
- 这一改动让"业务主键类型"与"权限表主键类型"彻底解耦，D4 之后二者都不需要类型分支。

### D6 配置类错误与权限拒绝分两类异常

"找不到承载对象""ID 集合为空""超出上限"抛已有的 `AuthServiceException`，权限未命中继续抛 `AuthException("权限校验不通过")`；未命中的 ID 明细与被拒绝方法的签名只进服务端日志。

- 备选：新增专用异常类型——被否，现有 `AuthException`（业务拒绝）/`AuthServiceException`（框架侧问题）已构成清晰二分。
- 现况是两者都落在全局 `Throwable` 兜底里，对外形态相同且会把内部接口名吐给调用方，因此这条属于对外契约修正。
- 兼容性注意：下游若统一捕获 `AuthException`，配置类错误的捕获类型会变；在迁移计划中列明。

### D7 承载类改名 `IdDataEdit` → `DataIdCarrier`

`Edit` 后缀源自早期只用于编辑场景的命名，而现在"预约""删除"等非编辑操作同样依赖它交出 ID；既然方法签名本轮必改，类名同批改的成本只在四处仓库内实现与文档一处。

- 备选：只改方法名保留类名——被否，留下一个名字与语义相悖的公开接口，比一次性扫改更贵。
- `.qoder/repowiki` 下的文档由生成器产出，不做手改，下次生成时自然更新。
- 孤儿类 `MeetingRoomAddReq`（有 `getDataId()` 却未实现契约、全仓零引用）按"接上契约"处理而非删除：废弃实现的删除需要单独批准。
- 批量判定的可验证载体：示例模块新增 `POST /test/dataEditBatch`（请求体为 `List<EditDataIdCarrier>`），用于手工验证"部分命中即整体拒绝"。

## Risks / Trade-offs

- [多 ID 语义收紧后，原本静默通过的批量调用开始被拒绝] → 这是修复而非回归，但会被使用者当作故障上报；示例模块补一个批量承载类的样例，README 明确"全部命中才放行"。
- [BREAKING：外部应用实现旧承载接口会编译失败] → 迁移面已被契约收窄为"方法名 + 返回值包一层 `List.of`"；在 README 与规格里各写一条对照示例，回滚只需还原本批类。
- [100 上限让真实大批量场景失败] → 由调用方显式切片，切片后每片仍是完整判定；上限常量集中在一处，放开不影响任何契约。
- [归一化键把 `Long 1` 与字符串 `"1"` 视为同一 ID] → 同一业务功能槽位下二者本就指向同一行数据，不构成越权；跨业务功能不会互相污染，因为过滤条件已含业务功能。
- [无 MySQL 实测环境，`data_id IN (?,...)` 的方言差异只能静态保证] → 只用公共子集；h2 起示例应用做运行期验证，交付说明中显式写"MySQL 无运行期证据"。
- [值改成占位符后，占位符数量必须与参数顺序严格一致] → 顺序由 AST 的书写顺序决定，实测渲染为 `业务功能 → 操作类型 → data_id IN (...)`；`listAuthData` 与 `deleteBatchAuthData` 的占位符数量各有单测断言，写错会直接红。
- [切面仍可能在方法执行前就发起权限查询，与 `@Transactional` 顺序未定义] → 现有顺序无害（校验先于方法体），本轮不改，留作可观察事实。

## Migration Plan

1. 先落测试（对旧行为建立对照，其中"多 ID 部分命中"预期为失败——它正是被修复的行为）。
2. 改契约与切面、改处理器与持久层，使测试转绿。
3. 扫改仓库内实现类与文档。
4. 示例应用以 h2 profile 启动，手工点单条放行、单条拒绝、批量部分命中、含引号的权限描述写入四种场景；用 H2 控制台确认权限表未多出记录。
5. 回滚策略：本轮无数据迁移，DDL 与既有权限数据形状不变，还原这批类与文档即回到旧行为；但旧行为含注入面，回滚只应在判定语义出现阻断性故障时短期使用。

## Open Questions

- `operate` 默认值 `unified` 是否要保留"占位即通配"的双重语义，还是强制显式声明——不影响本轮规格与任务拆分，待下一轮权限语义梳理。
- 行级操作权限是否要像数据范围那样引入启动期校验器——D6 已把失败分类先做对，届时只增校验器、不改对外语义。
- `getDataAuths` / `fillDataAuths` 是否需要批量版本——取决于列表页"逐条填充权限"是否成为性能瓶颈，当前无证据。
