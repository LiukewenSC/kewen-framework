# Proposal

## Why

`@AuthDataOperation` 负责"当前登录人能否对某一条业务数据执行某个操作"的行级写权限校验，它自 2022 年成形后没有跟上一轮 `@AuthDataRange` 的改造，存在三个会导致错误结果的问题：一次请求携带多个业务数据 ID 时只校验第一个（其余静默放行）；权限表的写入路径把请求体里的字符串原样拼进 SQL 字面量（实测 jsqlparser 4.7 的 `StringValue` 不转义单引号），可被用来向权限系统自己的表签发任意授权行；业务主键为字符串时权限维护操作必抛类型异常。同时该注解当前没有任何活着的回归测试，改动缺少对照。

## What Changes

- 行级写权限校验的判定单位从"一个请求一个 ID"改为"一个请求的全部待授权 ID"：切面收集方法实参中所有的权限承载对象（含一层数组/集合元素），汇总去重后**逐个 ID 都必须命中**才放行，任一未命中即整体拒绝。
- **BREAKING** 承载对象契约由单值改为集合：`IdDataEdit<ID>.getDataId()` 改为返回 `Collection<ID>` 的方法，且承载类名由 `IdDataEdit` 改为 `DataIdCarrier`（`Edit` 后缀与"预约/删除"等非编辑操作早已不符）。业务侧单值场景改为 `return List.of(id)`。
- 单次请求的待授权 ID 总数上限固定为 100，超出直接失败，不做内部分批；更大的批量由调用方显式切片。
- 权限判定中"用户持有哪些权限串"不下推进 SQL，改为把候选行的授权记录取回后在内存求交；ID 的比对键统一做字符串归一化，避免 `Long`/`Integer`/`String` 主键之间错配成"有权限却拒绝"。
- `JdbcAuthDataPersistent` 的四个读写方法全部改为占位符绑定；语句结构继续由 JSqlParser 构造，只是叶子位置从字面量换成 `JdbcParameter`。权限表自增主键不再按传入业务 ID 的类型去解读。**这同时修复上述注入问题**。
- 权限校验失败与方法配置错误（实参里没有承载对象、ID 集合为空、超出上限）区分为两类结果，配置类错误不再把框架内部信息作为拒绝原因返回给调用方。
- 新增 auth-core 侧的回归测试，覆盖放行、拒绝、多 ID 部分命中（旧行为会错误放行）、空集合、含空值、超上限、以及"含单引号的权限串原样入库且不产生额外行"。
- 更新 `README.md`、`README_Auth.md` 与示例模块 README 中关于该注解与承载契约的说明。

范围边界：`@AuthDataRange` 一侧的类与规格不变（其字面量来源仅为注解常量与由权限 ID 拼成的权限串，不含请求体驱动的字符串，且必须使用 SQL 解析器改写语句）；`sys_auth_data` 的列类型不变。

## Capabilities

### New Capabilities

- `auth/data-operation`: 行级数据操作权限的声明位置、承载契约、多 ID 判定语义、上限与失败区分，以及权限数据读写不得受调用方输入影响的要求。

### Modified Capabilities

无。`auth/data-range` 与 `auth/menu-access` 的需求均不改变。

## Impact

代码：
- `../../../../auth`：`AuthDataOperation`、`data/edit/IdDataEdit`、`data/edit/DataCheckAspect`、`data/AuthDataHandler`、`data/JdbcAuthDataHandler`、`data/JdbcAuthDataPersistent`、`data/AuthDataDO`
- `../../../../auth`：`AuthCoreConfig`（切面与处理器装配签名若变动）
- `sample/auth-rbac-boot-sample`：`MeetingRoomApproveReq`、`MeetingRoomUpdateReq`、`MeetingRoomAddReq`、`TestAuthAnnotationController` 内部承载类

外部契约：所有实现旧承载接口的下游应用需同步改方法名与返回类型；`auth-core` 的公开接口 `hasDataOperateAuths` 参数形态改变。

验证：`auth-core` 单元测试新增；示例应用可在本机以 H2 启动手工验证。**MySQL 侧无实测环境**，跨方言 SQL 仅能靠"只使用 MySQL/H2 公共子集"约束。

依赖：无新增、无移除。`JdbcAuthDataPersistent` 继续使用 `jsqlparser` 构造语句，`auth-core` 的依赖清单不变。
