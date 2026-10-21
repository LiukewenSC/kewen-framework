# Tasks

## 1. 模块脚手架与依赖

- [x] 1.1 用启动类替换 `sample/auth-core-boot-sample/src/main/java/com/cu/framework/sample/Main.java`（`@SpringBootApplication` + `@MapperScan`），并在本模块 pom 加入 `spring-boot-starter-web`、`mybatis-spring-boot-starter:2.3.1`、runtime 作用域的 `com.h2database:h2`、test 作用域的 `spring-boot-starter-test`；验证 `mvn -q -pl sample/auth-core-boot-sample -am package` 编译打包成功
- [x] 1.2 本模块 pom 自行声明 `<resources>`，只挂本模块 `sql/` 到 `classpath:sql/`，使 `docs/sql` 的框架脚本不进 classpath；并加 `spring-boot-maven-plugin`（参照 rbac 模块写法）；验证打包产物 `target/classes/sql/` 下只有本模块脚本，无 `auth-rbac.sql`
- [x] 1.3 写 `application.yml`：默认 `h2` profile 用 `jdbc:h2:file:./data/auth_core_sample;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE`，H2 的数据源/脚本/控制台声明全部只写在 `h2` 段内，`mysql` 段只连接不建表；`cu.auth.auth-data-table` 七项全部映射到示例自有列名；开 mapper 包的 DEBUG 日志与 `logging.level.org.springframework.jdbc.core=DEBUG`；验证从模块目录启动成功且 `./data/` 下生成数据库文件
- [x] 1.4 `.gitignore` 增加 `sample/auth-core-boot-sample/data/`；验证启动后再 `git status` 不出现数据文件
- [x] 1.5 建 `README.md` 骨架（核心功能清单 / 扩展点契约 / 工程结构 / 启动与重置 / 场景矩阵空表 / 覆盖边界）与 `verify.sh` 骨架（按编号读矩阵、逐条打印通过或失败）；验证 `bash verify.sh` 能空跑并输出"无用例"结论而不是报错

## 2. 示例自有表与可重复执行的脚本

- [x] 2.1 写 `sql/schema.sql`：8 张 `demo_` 前缀表（权限存储 `demo_auth_data(pk,biz_module,biz_data_id,bigint 数据ID,action,auth_str,auth_desc)`、`demo_menu_auth`、`demo_menu_api`、`demo_user_auth`、`demo_work_order`、`demo_asset(asset_id)`、`demo_customer`、`demo_execution_log`），一律 `CREATE TABLE IF NOT EXISTS`、不写 `DROP`、只写 MySQL 与 H2 双方接受的语法；验证启动后 8 张表都在
- [x] 2.2 写 `sql/seed-data.sql`：业务行、用户权限行、菜单授权行、权限槽位行，全部用 `INSERT ... SELECT ... WHERE NOT EXISTS` 守卫；验证脚本可重复执行且不产生重复行
- [x] 2.3 跑 `scripts/check-sql-compat.sh` 并把两个新脚本纳入结论；若 H2(MySQL 模式) 不接受无 `FROM` 的写法，按设计 D1/Risks 改 `FROM DUAL` 后重跑；验证脚本输出的文件清单包含这两个新脚本且检查全部通过
- [x] 2.4 连续三次启动同一数据文件，比对三次的表行数与一处手工改过的授权行；验证第三次启动后结构与人工改动仍在、种子行数不翻倍

## 3. 框架侧：拒绝通道与用法错误出口

- [x] 3.1 在 `AuthCoreConfig` 里以 `@Bean @ConditionalOnWebApplication @ConditionalOnMissingBean` 注册 `AuthExceptionHandler`（不改其行为，只补装配）；验证纯 core 示例里请求一个无权限的受管辖端点得到 `code=403` 的统一结果，不再是 HTTP 500
- [x] 3.2 在 `AuthExceptionHandler` 增加 `AuthServiceException` 出口，输出 `code=400`，消息只带业务功能、操作类型或数量上限，不含框架类名与接口名；验证触发"实参与权限未命中"两种失败时响应编码可区分（400 对 403）
- [x] 3.3 在 starter 模块补装配测试：容器内已有同类拒绝处理时框架出口不注册、非 web 环境时不注册；验证两个断言各自通过
- [x] 3.4 回归既有 rbac 示例：启动并触发一次菜单拒绝，把它的实际响应体记录进本任务的验证结论（该响应从 generic code 变为 `code=403`，是 proposal 已声明的影响面）；验证 rbac 示例其余接口无第二处变化
- [x] 3.5 把本组两条场景（拒绝通道、两类编码）写入 README 场景矩阵 A 组与 C 组对应行，并在 `verify.sh` 加断言；验证 `bash verify.sh` 对本组用例给出通过结论

## 4. 框架侧：权限集合为空的注入语义

- [x] 4.1 在 `SqlAuthInject.convert2NewSql` 里于构造权限子查询前判权限集合为空，为空时给命中语句挂恒假条件 `1 = 0`，`IN` 与 `EXISTS` 两条分支同一处理；在 `auth-core` 单测里断言渲染出的语句含恒假条件且不含空 `IN ()`，两种匹配方式都覆盖；验证该单测通过
- [x] 4.2 在 `SqlAuthInject.authTableWhereExpression` 处加 TODO 注释，写明"authority 以字面量拼进语句"是已知债务、本轮按裁决不改、触发条件是权限串本身含引号；验证注释存在且代码行为零变化（原有 range 单测全绿）
- [x] 4.3 把"权限集合为空返回零行"的场景写入 README 矩阵 D 组并在 `verify.sh` 加断言（依赖第 5、8 组的载体，先在矩阵与脚本里占位为待跑）；验证脚本对该用例能给出明确状态而不是跳过

## 5. 示例：三个扩展点与身份注入

- [x] 5.1 实现 `DemoAuthContextHolder`（实现 `AuthCurrentUserContextHolder`）与写入身份的 MVC 拦截器：`preHandle` 读 `X-Demo-User` 存 ThreadLocal、`afterCompletion` 清理，权限集合每次经 MyBatis 查 `demo_user_auth`；验证带与不带请求头两种调用 `/demo/current-demoUser` 分别返回权限集合与空集合，且连续两个不同用户的请求互不串号
- [x] 5.2 实现权限集合体：`DemoAuthObject extends AbstractAuthObject` 加三个 flag 实体（`USER`、`ROLE`、`DEPT_ROLE` 双位置组合），验证 `/demo/current-demoUser` 回显的权限原文与解析后的实体字段往返一致，含双位置组合那条
- [x] 5.3 实现 `DemoMenuAuthHandler`（实现 `AuthMenuHandler`）读 `demo_menu_auth`：该 path 登记过则要求与当前权限有交集，未登记视为未授予任何人；验证三条 curl 分别得到放行、`code=403`、`code=403`
- [x] 5.4 实现 `DemoMenuApiStore`（实现 `MenuApiService<Long>`，hutool 雪花生成 ID、`path` 唯一）读写 `demo_menu_api`；验证首次启动日志出现"添加的接口有"且表内出现类虚拟路径行与端点行、父子关系正确，二次启动日志出现"没有生成新的API接口"
- [x] 5.5 在示例 `src/test` 加两个装配上下文：缺强制扩展点时启动失败且信息指到缺失的那个扩展点；缺菜单持久化实现时启动成功并记录跳过；验证两个断言通过（对应 `auth/starter-assembly` 的三条场景）
- [x] 5.6 把 A 组场景（装配结果、权限体解析往返）写入 README 矩阵并在 `verify.sh` 加断言；验证脚本对 A 组全部用例通过

## 6. 示例：菜单鉴权场景

- [x] 6.1 建三个 Controller 承载 B1..B3：方法级声明（一个受管辖方法 + 一个不受管辖方法）、类级声明（全部端点受管辖）、完全无声明；验证四组 curl 的通过/拒绝结果与规格场景逐条一致，且类级声明不影响另一个 Controller
- [x] 6.2 承载 B4 与 B5：不带 `X-Demo-User`（空权限集合）访问受管辖端点被拒；配置 `server.servlet.context-path` 后请求带前缀的路径，鉴权身份仍是应用内路径；验证两条 curl 结论并写入矩阵
- [x] 6.3 承载 B6..B9：`@AuthMenuDoc` 缺失时菜单行标签回落类简单名/方法名、两个端点用相同文案仍是两条独立授权行、只有管辖声明没有文案的端点被正确注册；验证查 `demo_menu_api` 得到的 name 与期望一致
- [x] 6.4 承载 B7 之外那条"已有行不被覆写"：手工 UPDATE 一条已有菜单行的 name 与 remark，重启后验证两值保持、授权不变，并把该用例标为需 `--stage=restart`
- [x] 6.5 把 B 组全部场景写入 README 矩阵并补进 `verify.sh`；验证脚本 B 组用例全通过（restart 类除外，单独说明）

## 7. 示例：行级操作权限场景

- [x] 7.1 定义承载对象与写方法：单值、批量、ID 分散在多个实参/集合元素/数组元素三类，每个受管辖写方法第一行插入 `demo_execution_log`；验证 `mvn test` 与接口可调用，日志表初始为空
- [x] 7.2 承载 C1..C4：单 ID 命中放行、单 ID 未命中拒绝、多 ID 全命中放行、多 ID 部分命中整体拒绝；验证每条 curl 的响应编码 + `demo_execution_log` 是否新增对应行（拒绝类必须零新增）
- [x] 7.3 承载 C5 与 C18：ID 分散在三个实参（其中一个集合、一个数组）时全部纳入判定；未被声明的写接口在有受控记录的数据上照常执行且不判定；验证 curl 与日志表结论
- [x] 7.4 承载 C6..C8：实参无承载对象、ID 集合为空、集合含 null 三类用法错误；验证三者响应均为 `code=400` 且消息不含框架类名或接口名
- [x] 7.5 承载 C9 与 C10：恰好 100 个去重 ID 正常判定、101 个直接失败；验证 101 那次在开启 `logging.level.org.springframework.jdbc.core=DEBUG` 时权限查询语句零条（证明确实没打库）
- [x] 7.6 承载 C11 与 C12：不带请求头（空权限集合）时判定被拒不放行；同一数据只配了 `edit` 槽位时调 `approve` 被拒（不跨操作回退）；验证两条 curl 结论
- [x] 7.7 承载 C13：请求体用短整型形态提交、权限表按 BIGINT 存储同一主键；验证判定放行、不因类型形态误判
- [x] 7.8 承载 C15 与 C16：通过权限维护接口写入含单引号与注释符的权限描述，验证原样入库、其他行不多不少；提交值中含引号的业务数据ID，验证按整值等值比对、不匹配到别的授权行
- [x] 7.9 实现并承载 C19..C21：`AuthDataAdaptor` 的编辑权限→同一调用从拒绝变放行、按数据ID清空→回归拒绝、填充读回某条数据的权限集合；验证三步的 curl 与 `demo_auth_data` 行内容
- [x] 7.10 把 C 组全部场景写入 README 矩阵并补进 `verify.sh`（副作用证据以查 `demo_execution_log` 的断言表达）；验证脚本 C 组用例全通过

## 8. 示例：数据范围场景

- [x] 8.1 写 `demo_work_order` 的映射语句：`edit` 与 `approve` 两个槽位各一个查询方法（一个用 `IN`、一个用 `EXISTS`）、一个未声明的对照查询、一个 insert；验证四条 curl 的行集与日志中实际语句符合各自声明
- [x] 8.2 承载 D4/D5 与 D1/D2/D6：同一请求内先后调用两个槽位互不污染；被声明语句被注入且结果只含可见行、同请求未声明语句（含权限装载那条查询）原文下发；`IN` 与 `EXISTS` 两种写法返回行集一致；验证观察 mapper DEBUG 日志与行集
- [x] 8.3 承载 D3 嵌套声明：用嵌套查询的映射配置让被声明的外层语句在执行期触发另一条被声明的内层语句；验证两条语句各按自己方法上的声明注入，外层不因内层结束而失去过滤
- [x] 8.4 承载 D7/D8：`demo_customer` 作为关联表参与 join 并用 `table`+`tableAlias` 把条件挂到关联表而非主表；`demo_asset` 用非 `id` 的主键列演 `dataIdColumn`；验证两条 curl 的行集与注入语句中的列前缀
- [x] 8.5 承载 D10/D11：同一映射文件里的 insert/update/delete 语句原文下发；`<if>` 动态语句在执行期正常注入，另配一条运行期拼不出目标表的语句验证抛异常而不是返回未过滤结果；验证 DEBUG 日志与异常响应
- [x] 8.6 承载 D15 端到端：不带请求头调用被声明的查询，验证返回零行而非数据库语法错误（与 4.3 的占位用例在此转为实跑）
- [x] 8.7 在示例 `src/test` 加启动失败断言：解析不到映射语句、声明在非查询方法上、语句启用二级缓存、静态 SQL 中目标表不存在、声明不在映射接口上，各一个独立上下文；验证五个断言各自通过并指到对应位置
- [x] 8.8 把 D 组全部场景写入 README 矩阵并补进 `verify.sh`（启动失败类指向对应测试并说明"由测试断言，非 HTTP 可验"）；验证脚本 D 组用例全通过

## 9. 整体核对

- [x] 9.1 跑 `bash verify.sh` 全量，并把每条用例的实际结论回写 README 的"已验证到"栏；验证脚本输出零条失败、README 中每个编号都能对上脚本输出
- [x] 9.2 跑 `mvn -q -pl qy-auth/auth-core,qy-auth/auth-core-spring-boot-starter,qy-auth/auth-rbac test` 与全仓 `mvn -q -DskipTests package`；验证原有单测无回归、构建通过
- [x] 9.3 在 README 的覆盖边界一节确认只留下"菜单身份改写"一条越界说明，且 `openspec validate build-auth-core-boot-sample-demo --strict` 通过；验证校验无错误，五份增量与实际实现逐条对应

## 10. 实现期发现的框架装配缺陷（用户原始授权覆盖：测试过程中发现的非逻辑性 bug）

- [x] 10.1 `AuthCoreConfig` 里的 `@ConditionalOnBean(SqlSessionFactory.class)` 让 `AuthRangeStartupValidator` 永不被创建（该条件在配置类注册阶段求值，而 MyBatis 自动配置按类名排序在 `com.cu.*` 之后）：声明缓存永远为空，所有 `@AuthDataRange` 静默失效、启动校验一并消失，应用还启动成功。改为 `ObjectProvider<SqlSessionFactory>` 在实例化阶段取，校验器原有的"没有 SqlSessionFactory 就跳过"分支自此才真正可达；验证=示例启动日志出现"数据范围声明校验通过，受管辖的查询语句共 12 条"，且 D 组 15 条断言由全红转全绿
- [x] 10.2 登记（不修改）：`AuthDataAdaptor` 是 core 的类，却只在 rbac 层的配置里注册成 bean，纯 core 接入拿不到。本示例自行 `@Bean` 声明，并写进 README 的扩展点契约表
- [x] 10.3 登记（不修改）：拒绝出口与 basic 层 `Throwable` 兜底同存时依赖 advice 排序，实测框架出口胜出（rbac 示例新实例 `/test/checkMenuNone` 返回 code=403）；`@ConditionalOnMissingBean` 只对同类型让位，使用方用别的类名自定义出口时不会让位——这一点与 `auth/starter-assembly` 的"出口唯一且可被使用方替换"仍有差距，留待单独决定

## 11. 用户追加要求：编译回归与前端页面

- [x] 11.1 修掉自己引入的编译回归：给 starter 加 `spring-webmvc` 时写成 test 作用域，直接声明覆盖了 `auth-core` 传递来的 compile 作用域，导致主源码 `AuthWebConfig` 在 `mvn clean package` 时编译失败（增量编译看不出来）。删掉该声明；验证=根目录 `mvn clean package -DskipTests` 零 ERROR 且 `mvn clean test -pl qy-auth/auth-core,qy-auth/auth-core-spring-boot-starter,qy-auth/auth-rbac,sample/auth-core-boot-sample` 全绿
- [x] 11.2 新增浏览器验证台 `src/main/resources/static/index.html`：49 条用例与 README 第 6 节、verify.sh 同编号，逐条显示期望/结果/实际下发 SQL/副作用计数，另有当前权限往返、各表行数、菜单树三个面板与自由调用区；验证=真浏览器打开 `http://localhost:8082/coredemo/` 点"全部运行"，结果 49 通过 0 不通过
- [x] 11.3 README 增补 4.1 节说明页面用途与入口；验证=文档里的 URL 与按钮名和页面上实际一致
