# Tasks

## 1. 承载契约改造（接口与全部实现同批，保持编译通过）

- [x] 1.1 把 `../../../../auth` 改名为 `DataIdCarrier.java`，方法由 `ID getDataId()` 改为 `Collection<ID> getDataIds()`，并同步 `AuthDataOperation.java` javadoc 中的 `{@link}` 引用；验证 `mvn -pl qy-auth/auth-core -am -DskipTests install` 通过，且 `grep -rn "IdDataEdit" --include=*.java .` 无残留
- [x] 1.2 扫改示例侧三个实现类（`MeetingRoomApproveReq`、`MeetingRoomUpdateReq`、`TestAuthAnnotationController.EditIdDataEdit`）为 `getDataIds()` 返回单元素集合，并处理孤儿类 `MeetingRoomAddReq`（它有 `getDataId()` 却没实现契约且全仓零引用）：接上契约或删除；验证 `mvn -pl sample/auth-rbac-boot-sample -am -DskipTests install` 通过
- [x] 1.3 更新 `README_Auth.md`、`README.md`、`sample/auth-rbac-boot-sample/README.md` 中 `@AuthDataOperation` 与承载契约的说明，含"单值也要返回集合""多个 ID 全部命中才放行""上限 100"三条对照示例；验证文档中的示例代码与 1.1/1.2 的实际签名逐字一致
- [x] 1.4 用注解声明位置、批量语义、ID 类型无关与失败分类重写 `AuthDataOperation.java` 的类注释（保留 `@author kewen`、`@since` 原值）；验证只写能从代码确证的内容，注释里没有不存在的旧注解名

## 2. 切面多 ID 判定

- [x] 2.1 在 `../../../../auth` 新建切面回归测试（直接构造切面 + 桩 `AuthDataHandler` 与用户上下文，不启 Spring），覆盖：单 ID 放行、单 ID 拒绝、多 ID 全命中、多 ID 部分命中、ID 分散在多个实参、ID 在 `List`/数组元素中、实参无承载对象、ID 集合为空、含 null 元素、去重后 100 个、去重后 101 个、当前登录人无权限；验证测试可运行，并记录"多 ID 部分命中"与"一层集合元素"两条在改造前为红
- [x] 2.2 把 `DataCheckAspect.before` 从 `filter(...).findFirst()` 改为收集全部承载对象（直接实参 + 一层数组/集合元素）的 `getDataIds()`，汇总去重后一次性判定，任一未命中即整体拒绝；验证 2.1 中上述两条转绿且其余用例保持绿
- [x] 2.3 把"实参无承载对象/ID 集合为空/含 null/去重后超过 100"四类改为抛已有的 `AuthServiceException` 且在去重后超过 100 时不发起任何权限查询；权限未命中继续抛 `AuthException("权限校验不通过")`；验证对应四条用例断言异常类型，并有"未调用 handler"的桩断言
- [x] 2.4 把 `log.info("...拦截方法:{}", joinPoint)` 降为 `debug`，去掉未命中 ID 明细与内部类型名进入对外消息的路径（明细只进日志）；验证断言拒绝响应文案不含 `DataIdCarrier`、`AuthServiceException` 等内部名

## 3. 处理器批量判定与归一化

- [x] 3.1 把 `AuthDataHandler.hasDataOperateAuths` 的 `Object dataId` 改为 `Collection<Object> dataIds`（含 `IAuthObject` 重载）并在 `JdbcAuthDataHandler` 实现批量判定：用户权限转 `Set`、结果按 `String.valueOf(dataId)` 归一化归组、求交得差集；验证新增 handler 层测试断言"权限集合为空即拒且不发查询"与"`Long 1` 与 `Integer 1` 归一化后视为同一 ID"
- [x] 3.2 清理 `AuthDataHandler.java` 注释里指向不存在注解的 `@AuthCheckDataOperation`、`@AuthEditDataAuth` 两处引用，改写为批量语义说明；验证 `grep -rn "AuthCheckDataOperation\|AuthEditDataAuth" --include=*.java .` 无残留
- [x] 3.3 确认 `editDataAuths`、`getDataAuths`、`fillDataAuths`、`deleteBusinessFunctionAuthByDataId` 保持单条数据语义未被批量改造波及；验证相关既有测试与示例调用点（`MeetingRoomController` 的两处）行为不变

## 4. 持久层参数绑定与注入修复

- [x] 4.1 先写注入回归测试：写入含单引号与注释符的 `authority`/`description` 后原样存成一行且权限表只新增预期行数；查询侧传含引号的 ID 不扩大匹配；验证这两条在旧实现下为红，作为改造前证据记录在提交说明或 PR 描述
- [x] 4.2 把 `JdbcAuthDataPersistent` 的 `listAuthData`、`insertBatchAuthData`、`deleteBatchAuthData`、`deleteBusinessFunctionDataIdAuth` 四条 SQL 的值全部改为 `?` 绑定并同步传参；语句结构仍由 JSqlParser 构造，只把叶子位置从 `StringValue`/`LongValue` 换成 `JdbcParameter`，不改回手写字符串拼 SQL；验证 4.1 转绿、`mvn -pl qy-auth/auth-core -am test` 全绿，且该文件不再出现 `new StringValue(` 拼接调用方传入值
- [x] 4.3 把查询改为按 ID 集合过滤的 `data_id IN (?,...)`，占位符数量由去重后的 ID 数决定；验证断言 100 个 ID 时占位符恰为 100，并覆盖 1 个与 100 个两种边界
- [x] 4.4 删除 `removeByIdsSql` 的 `Long/Integer/StringValue` 类型分支与 `ID类型异常`，把 `listAuthData` 的 RowMapper 改为 `getObject` 读回、回删时原样绑定，解除"业务 ID 类型绑架权限表主键类型"，并在 `AuthDataDO.id` 上注明读回即回绑；验证新增一条字符串主键业务的 `editDataAuths` 测试不再抛 `ID类型异常`
- [x] 4.5 在 `docs/sql/auth-rbac.sql` 的 `data_id` 列注释加一句"列类型随业务主键，框架代码不假设类型"；验证只改注释、DDL 形状与既有种子数据不变

## 5. 装配同步

- [x] 5.1 按 3.1 的签名变化复核并同步 `AuthCoreConfig` 中 `dataCheckAspect`、`jdbcAuthDataHandler` 的装配与过时注释；验证 `mvn -pl qy-auth/auth-core-spring-boot-starter -am -DskipTests install` 通过，示例应用启动无 bean 创建失败

## 6. 集成验证

- [x] 6.1 示例应用以 h2 profile 启动（8081），手工验证四条：单条放行、单条拒绝、批量部分命中被整体拒绝、含引号的权限描述写入后经 H2 控制台确认权限表无多余行；验证记录实际响应与行数，并显式注明"MySQL 侧无运行期证据"
- [x] 6.2 跑 `mvn test` 与 `openspec validate refactor-auth-data-operation`，验证全绿且规格增量与实现一致
