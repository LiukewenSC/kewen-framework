# Tasks

## 1. 声明层：两个注解各管一件事

- [x] 1.1 在 `../../../../auth` 新增 `@AuthMenuDoc`（`String name()` 必填、`@Target({METHOD, TYPE, ANNOTATION_TYPE})`、`@Documented`、`RetentionPolicy.RUNTIME`），验证：`mvn -q -pl qy-auth/auth-core -am -DskipTests compile` 通过，且此步没有任何消费者（零行为变更）
- [x] 1.2 `AuthMenu` 移除 `name()` 变成无属性标记，并在同一步内改写全部标注点：`../../../../auth` 的 5 个控制器、`sample/auth-rbac-boot-sample` 的 3 个主控制器与 `src/test/java/controller/authmenu` 的 4 个测试控制器、模板 `other/code-generator/src/main/resources/templates/controller.java.vm:50`；验证：全仓 `mvn -q -DskipTests compile` 通过，且 `grep -rn "@AuthMenu(" --include=*.java --include=*.vm .`（排除 `target/`）不再出现带参数的调用
- [x] 1.3 `MenuApiGeneratorProcessor` 的两个标签消费点（`:132` 类、`:157` 方法）改读 `@AuthMenuDoc`，缺失时保留现有回落（`:137` 类简单名、`:161` 方法名）；验证：在 `auth-core` 新增测试覆盖三种组合——只有类上有文案、类与方法都有文案、两处都没有文案；断言标签分别等于类文案、`类文案>方法文案`、`类简单名>方法名`

## 2. 管辖边界可验证

- [x] 2.1 `MenuAccessInterceptor` 保持"方法→类"的存在性回退语义不变，去掉对注解实例的无用持有；验证：在 `auth-core` 新增测试——类上有声明、方法上没有的端点仍被判定为受管辖并走到 `hasMenuAccessAuth`；类与方法都没有声明的端点直接放行且不调用校验

## 3. 鉴权身份不可由请求改写

- [x] 3.1 把 `DefaultSysAuthMenuComposite.updateMenu` 从 `updateById(req)` 改为显式 SET 白名单（只写 `name`、`remark`、`auth_type`、`parent_id`），字段清单收在一处；验证：在 `../../../../auth` 新增测试断言白名单集合恰含这四个字段且不含 `path`，并与 `RbacMenuApiControllerTest` 现有 4 个用例一起 `mvn -q -pl qy-auth/auth-rbac test` 全绿

## 4. 数据与文案

- [x] 4.1 修正两处纯文案残留：`RbacDeptController` update 的显示文案改为"修改部门"，`sample/auth-rbac-boot-sample/sql/seed-data.sql:84` 同步；验证：起 sample 后 `/menu/api/tree` 中 `/rbac/dept/update` 的标签为"部门管理>修改部门"，且该行 `id` 与授权记录未变（`sys_auth_menu.api_id` 仍指向同一行）

## 5. 文档

- [x] 5.1 更新 `README.md`、`README_Auth.md`、`sample/auth-rbac-boot-sample/README.md`：两个注解的分工、类级声明=该 Controller 全部端点受管辖、`/menu/api/update` 不再接受 `path` 修改，以及本轮明确保留的两个缺陷（带路径变量的端点标 `@AuthMenu` 会被恒拒绝、同路径不同 HTTP 方法共用一份授权）；验证：文档示例代码与 `controller.java.vm` 的生成形态逐字一致

## 6. 集成验证

- [x] 6.1 全仓 `mvn -q test` 达到 BUILD SUCCESS
- [x] 6.2 起 `sample/auth-rbac-boot-sample` 的 `h2` profile 打通载体：`/menu/api/tree`（标签来自 `@AuthMenuDoc`）、`/rbac/user/update`（只靠类级声明受管辖的代表端点，无授权时应拒）、`/meetingRoom/page`（确认数据范围未回归）、`/test/checkMenu` 与 `/test/checkMenuNone`（有授权通过、无授权拒绝）
- [x] 6.3 端到端确认身份不可写：对某个菜单行调 `/menu/api/update` 同时提交改过的 `path` 与改过的 `remark`，验证 `remark` 生效而 `path`、授权、行数三者均未变化
- [x] 6.4 归档前经用户选择批准，顺带修掉卡住 6.1 的两处既有缺陷（属 `auth/data-range` 能力，不在本 change 声明范围内）：`SqlAuthInject.parseExpression` 收到无 WHERE 子句的 SQL 时以 null 进入表达式匹配分支抛 NPE，改为返回未匹配，使"目标表不存在"能按规格报出来；`AuthRangeStartupValidator` 在 `sqlSessionFactory` 为 null 时按装配处 `@ConditionalOnBean` 已声明的契约跳过而非 NPE；验证：全仓 `mvn -q test` 退出码 0
