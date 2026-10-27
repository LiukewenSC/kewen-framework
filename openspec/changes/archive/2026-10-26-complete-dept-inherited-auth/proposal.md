# Proposal

## Why

`RELATIONDEPT` 这个父级继承权限标记刚落地，但它所在的装载链路有两处当场就坏的空指针，且它现在的实际语义与已定的产品裁定相反（父链含本部门自身）。新标记既没有任何单元测试，也没有任何能演示"子部门的人有权限、本部门的人没有"的示例载体——它是仓库里唯一一条改了权限体模型却没有规格的通路。

## What Changes

- 修正用户权限体的装载：MyBatis 映射还指向已删除的 `users` 集合字段，改为映射单个 `user`；权限体或用户缺失时认证装载不再抛 NPE。
- **BREAKING** 父级链语义改为**严格上级、不含本部门**：`RELATIONDEPT__<deptId>` 只授予该部门子孙部门的成员，本部门成员改由 `DEPT__<deptId>` 覆盖。承载它的接口方法因此契约变化，并拟同步改名以表意"严格上级链"。
- 父链装载对畸形层级（自引用、成环）增加防护，不再可能无限查库。
- 权限实体的标记必须是单段令牌（不得包含次级分隔符），把本次 `DEPT_RELATION` → `RELATIONDEPT` 改名的根因立成规则防复发。
- 为 `RELATIONDEPT` 补单元测试：配置侧编解码往返、用户侧父链展开、语义判定的边界场景。
- `auth-rbac-boot-sample` 补端到端用例：把 `sys_dept` 种子从两个平铺节点改造成多层级树，并以"同一端点、三种部门身份 → 200/403/403"的对照把新语义钉死。为此示例需要按请求切换身份。
- `auth-core-boot-sample` 补一个继承型标记的示例：用示例自有的权限实体与自己的表，演示"用户权限体不可反解、配置权限体可往返"这一已写入文档的不对称。
- 不改动 `DEPT_ROLE` 笛卡尔积（见 Non-goals）。

## Capabilities

### New Capabilities

- `auth/dept-inherited-access`: 部门级权限沿部门层级向下继承的语义、展开时机、编解码规则、装载健壮性与验证载体。现有 `auth/menu-access` 只管"哪些端点受管辖、身份能不能被改写"，不管权限串如何展开与匹配，两者不重叠。

### Modified Capabilities

- `sample/auth-core-verification-demo`: 新增一条要求——核心层"权限体是否可反解"这一能力差异必须有可指认的示例用例承载。

## Impact

- 代码：`../../../../auth`（`UserAuthInfo`、`SimpleAuthObject`、`ParentDept`、`DeptWithParents`、`SysUserComposite` 及其实现）、`../../../../auth`、`../../../../auth`（`RbacSecurityUserDetailsService`）。
- **BREAKING** `SysUserComposite.getDeptListByDeptId` 是对外接口方法，返回内容由"含自身的链路"变为"严格上级链路"，外部实现方与调用方需同步；这是已定语义裁定的必然派生，不是额外的破坏。
- 测试：`../../../../auth`（JUnit 4 体系）、两个 sample 的测试与脚本。
- 数据：`sample/auth-rbac-boot-sample/sql/seed-data.sql` 新增部门树与部门归属种子行。该文件头部已声明带显式主键、非幂等，只在 `h2` profile 自动执行，MySQL 侧手动导入一次；因此 MySQL 使用者需要重新导入或手工补行才能看到示例。
- 文档：`../../../../auth` 已写明"用户权限体反解不出来"，本次把这句话变成可执行的用例而不是纯描述。
- 验证边界：H2 每次启动都是全新 schema，本次改动不涉及表结构迁移，可在本机 H2 完整验证；MySQL 侧无实测环境。

## Non-goals

- **`DEPT_ROLE` 角色×部门笛卡尔积不动**，既不实现"改为结构化匹配"的既定方向，也不给它立契约。它与部门继承正交，测试只钉 `RELATIONDEPT`，避免把一个尚未裁决的方向写死成规格。
- **不做父链的 N+1 查询优化**。当前是逐级单查，同级部门会重复走公共祖先段；没有观测证据前不改，避免把递归 CTE 或路径字段引入框架。
- **不修部门/角色名称含 `_` 时权限描述被截断**的既有反解析缺陷。它早于本次改动存在，影响所有标记而非 `RELATIONDEPT` 独有，需要单独设计分隔符策略。
