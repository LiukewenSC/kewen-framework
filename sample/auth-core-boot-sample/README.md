# auth-core 独立验证示例（auth-core-boot-sample）

# 1. 这个工程是干什么的

`auth-core` 是权限框架的**接缝层**：它只规定"权限怎么声明、在哪一步判定、语句怎么改写"，
不给任何具体权限模型——用户从哪来、路径怎么变成权限、权限行存在哪张表，全是用法侧的事。
`auth-rbac` 是那套"带实现"的层。

本工程的唯一目的，是**在完全不引用 auth-rbac 的前提下**，把 auth-core 的每一项能力跑通一遍：

- 不依赖任何权限模型层的代码，也不引用任何 `sys_*` 表，不加载 `docs/sql/` 下的框架建表脚本；
- 示例自己实现三个扩展点，自己设计全部数据表；
- 每一项规格场景都落到"一个声明位置 + 一条触发命令 + 一个可查的观察点"。

所以它同时也是两份东西的证明：证明接缝层确实与具体权限模型无关；
证明规格文档里那三十多个场景不是纸面承诺。

场景编号与仓库 `openspec/specs/auth/`、`openspec/specs/sample/` 下的规格条目对应，
`verify.sh` 按同一批编号逐条断言。

# 2. 核心能力清单

## 2.1 auth-core 交出的东西

| 能力 | 声明方式 | 生效位置 | 规格出处 |
| --- | --- | --- | --- |
| 菜单管辖声明 | `@AuthMenu`（标方法或标类） | `MenuAccessInterceptor.preHandle` | auth/menu-access |
| 菜单显示文案 | `@AuthMenuDoc`（不参与鉴权） | `MenuApiGeneratorProcessor` 生成标签 | auth/menu-access |
| 菜单行自动注册 | 启动后异步扫描 HandlerMapping | `demo_menu_api` | auth/menu-access |
| 行级数据操作权限 | `@AuthDataOperation(businessFunction, operate)` 标业务方法 | `DataCheckAspect` 方法执行前 | auth/data-operation |
| ID 承载约定 | 请求体实现 `DataIdCarrier<ID>` | 切面从实参收集（含一层集合/数组） | auth/data-operation |
| 数据范围过滤 | `@AuthDataRange(...)` 标 **Mapper 接口查询方法** | `MybatisDataRangeInterceptor` 改写语句文本 | auth/data-range |
| 声明错误启动即失败 | 同上 | `AuthRangeStartupValidator` | auth/data-range |
| 权限表读写 | 框架内置 `JdbcAuthDataHandler`（JdbcTemplate，全参数绑定） | `demo_auth_data` | auth/data-operation |
| Service 层直接维护权限 | 注入 `AuthDataAdaptor` | edit / fill / delete | auth/data-operation |
| 权限串编解码 | `FlatteningAuthority` + `IFlagAuthEntity`（`标记__ID` 与 `标记__名`） | `AbstractAuthObject` | 见本文 3.2 |
| 表名列名可重命名 | `cu.auth.auth-data-table.*` 七项 | 所有权限表读写与注入 | 见本文 3.3 |
| 统一拒绝响应 | starter 自动注册 `AuthExceptionHandler` | `AuthException`→code 403，`AuthServiceException`→code 400 | auth/starter-assembly |

## 2.2 使用方必须自己交出的东西

| 扩展点 | 强制？ | 缺失后果 | 本示例的实现 |
| --- | --- | --- | --- |
| `AuthCurrentUserContextHolder` | **强制** | 启动失败，原因指到该类型 | `DemoAuthContextHolder`：请求头 `X-Demo-User` + 每次查 `demo_user_auth` |
| `AuthMenuHandler` | **强制** | 启动失败，原因指到 `menuAccessInterceptor` | `DemoMenuAuthHandler`：查 `demo_menu_auth`，未登记即视为无人被授予 |
| `MenuApiService<T>` | 可选 | 只记一条"menuApiService is null"，启动照常 | `DemoMenuApiStore`：`demo_menu_api` + hutool 雪花 ID |
| `AuthDataAdaptor` 的 bean | 需自己声明 | core 的自动配置里没有它（只有 rbac 层配了），不声明就没法在 Service 层直接维护权限 | `DemoAuthConfig` 里 `@Bean` |

# 3. 示例的数据设计

## 3.1 八张表，全部示例自有

| 表 | 作用 | 列设计里刻意的地方 |
| --- | --- | --- |
| `demo_auth_data` | 权限槽位存储 | 表名与**全部列名**都不同于框架默认约定，见 3.3 |
| `demo_menu_auth` | path → 授予的权限串 | 路径存不进整型的数据ID列，所以必须独立一张表 |
| `demo_menu_api` | 菜单行注册结果 | 主键 BIGINT 非自增，由示例的 `MenuApiService` 生成 |
| `demo_user_auth` | 用户 → 权限串 | 装载权限那条查询是"未声明语句"的观察对象 |
| `demo_work_order` | 业务表 | 两个客户外键，用来区分同一张表的两个别名实例 |
| `demo_asset` | 业务表 | 主键列名是 `asset_id` 而不是 `id` |
| `demo_customer` | 业务表 | 多表语句中的关联表 |
| `demo_execution_log` | 副作用证据 | 拒绝发生在方法体之前 → 计数不增长就是"方法体没执行" |

## 3.2 权限串的编解码

框架用两个分隔符：`AUTH_SPLIT="__"` 分开标记与值，`AUTH_SUB_SPLIT="_"` 分开组合权限的各个位置。

| 权限串 | 描述 | 解析出的实体 |
| --- | --- | --- |
| `USER__1` | `USER__爱丽丝` | `UserFlagAuthEntity`（单位置） |
| `ROLE__10` | `ROLE__工单管理员` | `RoleFlagAuthEntity`（单位置） |
| `DEPT_ROLE__100_20` | `DEPT_ROLE__研发部_资产角色` | `DeptRoleFlagAuthEntity`（**双位置**，自己实现 `serialize/deSerialize`） |

示例的 `DemoAuthObject` 刻意让 `listBaseAuth()` 从解析出的实体**重新序列化**，
所以 `GET /demo/current-user` 里"原文"和"重序列化"两栏必须一致——不一致就说明编解码没走通。
两个约束要记住：权限标识里的名字不能含 `_`（会被当成第二个位置），也不能含 `__`。

## 3.3 权限存储被整张重命名

```yaml
cu.auth.auth-data-table:
  table-name: demo_auth_data
  id-column: pk
  business-function-column: biz_module
  data-id-column: biz_data_id
  operate-column: action
  authority-column: auth_str
  description-column: auth_desc
```

七项全部覆盖。框架只要有任何一处硬编码了表名或列名，本工程立刻起不来——这本身就是验证。
业务主键与数据ID都是整型；框架代码不假设类型（泛型能力留给外部项目），但本示例只用整型形态。

# 4. 启动、数据文件与重置

```bash
cd sample/auth-core-boot-sample      # 必须从模块目录启动，数据与日志路径都是相对的
mvn -q package -DskipTests
java -jar target/auth-core-boot-sample-0.0.1-SNAPSHOT-exec.jar
```

> **启动目录必须是本模块目录。** Spring Boot 会同时读取工作目录下的 `application.yml`，且文件配置优先于 classpath。
> 如果从仓库根目录启动，根目录的 `application.yml` 会把 `cu.auth.auth-data-table.table-name` 覆盖成 `sys_auth_data`，
> 而本示例的 H2 库只有 `demo_auth_data`：菜单鉴权与权限回显正常，但所有 `@AuthDataRange` / `@AuthDataOperation`
> 接口都会以 HTTP 500 报 `Table "sys_auth_data" not found`。
> IDEA 运行配置里要把 **Working directory** 设为 `sample/auth-core-boot-sample`（或 `$MODULE_WORKING_DIR$`）；
> 命令行启动就是上面那条 `cd`。

## 4.1 浏览器验证台

启动后打开 **`http://localhost:8082/coredemo/`**（就是 `src/main/resources/static/index.html`，纯静态页 + fetch，不引任何前端构建与模板引擎）。

它是本文第 6 节那张矩阵的可点击版本，52 条用例逐条标注"身份 / 期望 / 结果 / 证据"：

- 顶部"手工身份"下拉框（alice / bob / carol / dave / erin / frank / 不带请求头）只影响"当前权限"回显与"自由调用"，切换后权限回显自动刷新；
- 场景矩阵里每条用例自带固定身份（行内标注），"全部运行"按各用例自己的身份逐条跑并给通过与否；
- 每条用例的证据栏回显**该次请求实际下发的 SQL**（含框架改写后的最终语句）与**副作用计数增量**——判定拒绝时看的是"没有新增行"，不是响应码；
- 三个面板分别看当前权限的编解码往返、各表行数（重启后应不变）、`demo_menu_api` 的菜单树；
- 底部"自由调用"可以手填路径与 JSON 体，按"手工身份"发一次并取回语句，结果首行标明本次使用的身份。

页面里的用例编号与 `verify.sh` 完全一致，两边描述的是同一批场景。

- 服务：`http://localhost:8082/coredemo`（`server.servlet.context-path=/coredemo`，是 B5 场景的载体）
- 数据库：`jdbc:h2:file:./data/auth_core_sample;MODE=MySQL;...`，即 `sample/auth-core-boot-sample/data/`
- 建表与种子：`sql/schema.sql` + `sql/seed-data.sql`，`spring.sql.init.mode=always`。
  脚本全部幂等（建表 `IF NOT EXISTS`、种子 `WHERE NOT EXISTS`），
  所以**重启不丢你手工授的权限，也不会重复灌种子**——已实测三次重启，行数一致、人工改的菜单行仍在。
- 重置到初始状态：停掉应用，删掉 `data/` 下的 `.mv.db` 文件，再启动。
- 直接看数据：`http://localhost:8082/coredemo/h2-console`，JDBC URL 填上面那串。
  注意 H2 文件库同一时刻只允许一个进程持有，控制台要在应用停着的时候用，
  或者用 `AUTO_SERVER=TRUE` 连接。
- 换 MySQL：`--spring.profiles.active=mysql`，该 profile **不执行任何脚本**，
  需自行按顺序执行本工程 `sql/` 下两个脚本。

# 5. 三条观察通道

| 通道 | 看什么 | 怎么开 |
| --- | --- | --- |
| HTTP 响应 | `Result.code`：200 放行 / 403 权限拒绝 / 400 用法错误 | 任何 curl |
| `GET /demo/trace?traceId=x` | 该次请求**实际下发的语句全文**与判定次数 | 请求带 `X-Trace-Id: x`，之后用另一个请求取回 |
| `GET /demo/log?apiKey=x` | 方法体执行了几次 | 副作用证据表计数 |

`/demo/trace` 由示例自己的一个 MyBatis 插件 + 一个 `AuthDataHandler` 装饰 bean 提供，
只做记录、不参与任何判定；框架的 `MybatisDataRangeInterceptor` 改的是同一个 `BoundSql` 实例，
所以取到的是**改写之后**的最终语句。

# 6. 场景矩阵

"已验证到"一栏写的是实际执行过的载体：`脚本` = verify.sh 里有对应断言；`测试` = src/test 断言。

## A 装配与接缝

| 编号 | 内容 | 声明位置 / 操作 | 观察点 | 期望 | 已验证到 |
| --- | --- | --- | --- | --- | --- |
| A1 | 缺当前用户上下文 | 测试里不注册该 bean | 启动异常文本 | 启动失败且指到 `AuthCurrentUserContextHolder` | 测试 `StarterAssemblyTest` |
| A2 | 缺菜单权限处理 | 测试里不注册该 bean | 启动异常文本 | 启动失败且指到 `AuthMenuHandler` | 测试 `StarterAssemblyTest` |
| A3 | 缺菜单持久化 | 测试里不注册该 bean | 启动结果 + `processor.run()` | 正常启动，只记跳过 | 测试 `StarterAssemblyTest` |
| A4 | 权限串编解码往返 | `DemoAuthObject.listBaseAuth()` 重新序列化 | `GET /demo/current-user` | 原文与重序列化一致，双位置组合能解析 | 脚本 `A4` |
| A5 | 权限体可反解与不可反解的不对称 | 同一条 `DEPTINHERIT__100` 分别喂 `DemoAuthObject` 与 `DemoUserAuthInfo` | `GET /demo/inherit-asymmetry` | 配置侧往返一致且归位 1 条；用户侧当场抛错，原因点名 `DEPTINHERIT` | 脚本 `A5`、测试 `DemoInheritAuthObjectTest` |

## B 菜单鉴权

| 编号 | 内容 | 声明位置 / 操作 | 观察点 | 期望 | 已验证到 |
| --- | --- | --- | --- | --- | --- |
| B1 | 方法级只管辖该端点 | `MethodLevelController.guarded` 有、`.free` 没有 | bob 分别调两个端点 | guarded 403、free 200 | 脚本 `B1` |
| B2 | 类级管辖全部端点 | `@AuthMenu` 标在 `ClassLevelController` | alice 调 all/two | all 200、two 403（逐路径独立授权），且不影响别的类 | 脚本 `B2` |
| B3 | 两处都没声明 | `OpenController` | 无身份调 `/open/ping` | 200，不发生菜单校验 | 脚本 `B3` |
| B4 | 空权限集合被拒 | 不带 `X-Demo-User` | 调 `/methodLevel/guarded` | 403 | 脚本 `B4` |
| B5 | contextPath 剥离 | `server.servlet.context-path=/coredemo` | 拒绝消息里的路径 | 身份是不带前缀的 `/methodLevel/guarded` | 脚本 `B5` |
| B6 | 端点自动注册成菜单行 | 启动后异步扫描 | `GET /demo/menu-api` | 出现类虚拟路径行 + 端点行，parent 指向类行 | 脚本 `B6` |
| B7 | 已有行不被覆写 | 人工 `POST /demo/menu-api/touch` 后重启 | 该行的 name/remark | 保持人工值，且总行数不涨 | 需重启，脚本 `--stage=restart` |
| B8 | 文案缺失回落 | 类无 `@AuthMenuDoc`、方法无 | `demo_menu_api.name` | 分别是类简单名、`类名>方法名` | 脚本 `B8` |
| B9 | 同文案两行独立 | `sameDocA`/`sameDocB` 同名 | bob 分别调两个 | A 通 B 拒 | 脚本 `B9` |
| B10 | 改文案不改归属 | 把 `/classLevel/all` 的 name 改掉 | alice 通、bob 仍拒 | 授权归属只认 path | 脚本 `B10` |
| B11 | 继承型标记只放行持有者 | `/deptInherit/ledger` 仅授予 `DEPTINHERIT__100`，frank 持有该串 | frank 调该端点 | 200 | 脚本 `B11` |
| B12 | 同一条串对不持有者拒绝 | 同上，bob 只有 `ROLE__20` | bob 调同一端点 | 403 | 脚本 `B12` |

## C 行级数据操作权限

| 编号 | 内容 | 声明位置 / 操作 | 观察点 | 期望 | 已验证到 |
| --- | --- | --- | --- | --- | --- |
| C1 | 单 ID 命中放行 | `POST /workOrder/update {"id":1}` alice | 响应 + `/demo/log?apiKey=workOrder.update` | 200，计数 +1 | 脚本 `C1` |
| C2 | 单 ID 未命中拒绝且方法体不执行 | 同上但用 bob | 响应 + 计数 | 403，计数**不变** | 脚本 `C2` |
| C3 | 多 ID 全命中 | `POST /workOrder/batchApprove {"ids":[4,5]}` alice | 响应 + 计数 | 200 | 脚本 `C3` |
| C4 | 多 ID 部分命中整体拒绝 | `{"ids":[4,1]}` alice | 响应 + 计数 | 403，计数**不变**（已命中的那条也不许执行） | 脚本 `C4` |
| C5 | ID 分散在散参/集合/数组 | `DemoBusinessService.scattered` 上声明，`POST /asset/scattered` | 返回的 collected 列表 | 三类位置的 ID 全部纳入 | 脚本 `C5` |
| C6 | 实参没有承载对象 | `POST /probe/noCarrier` | 响应 | **400** + "未提供业务数据ID"，不是 403 | 脚本 `C6` |
| C7 | 交出空集合 | `POST /probe/emptyIds {"ids":[]}` | 响应 | 400 | 脚本 `C7` |
| C8 | 集合含 null | `POST /probe/nullId {"ids":[null]}` | 响应 | 400 + "业务数据ID不能为空" | 脚本 `C8` |
| C9 | 恰好 100 条正常判定 | `POST /probe/bulk` 1..100 | trace 里的 judge 记录 | `idCount=100`，判定确实发生（结果 403） | 脚本 `C9` |
| C10 | 101 条直接失败且不打库 | 同上传 1..101 | trace 里的 judge 记录 | 400 + 上限消息，judge 记录**为零条** | 脚本 `C10` |
| C11 | 权限集合为空即拒绝 | 不带身份调 `POST /probe/call {"id":801}` | 响应 | 403（库里 801 明明有授权行） | 脚本 `C11` |
| C12 | 不跨操作回退 | 工单 1 只授了 `approve` 给 bob | bob 调 update(edit 槽位) | 403；同一条数据调 approve 则 200 | 脚本 `C12` |
| C13 | 整型不同形态不误判 | `POST /asset/updateIntegerForm {"assetId":101}` dave | 响应 | 200（Integer 交出、BIGINT 存储、Long 读回） | 脚本 `C13` |
| C15 | 含引号与注释符的权限描述 | `POST /authData/grant`，desc=`ROLE__77'); DROP TABLE demo_auth_data; --` | `/demo/auth-data` + `/demo/counts` | 原样存进一行，表仍在、别的行不多不少 | 脚本 `C15` |
| C16 | ID 里的语句内容不扩大匹配 | `POST /probe/quote/call`，rawId=`900' OR '1'='1` | 服务端异常里的 SQL 文本 | 语句是 `biz_data_id IN (?)`——载荷**没有**变成语句结构；因数据ID列是整型，结果是类型转换错误而非放行 | 脚本 `C16` |
| C17 | 拒绝与配置错误编码可区分 | C2 与 C6 对比 | 两个 code | 403 对 400，且消息不含框架类名 | 脚本 `C17` |
| C18 | 未声明的写接口不判定 | `POST /workOrder/updateUnchecked`（工单 6 有受控记录） | 响应 + 计数 | 照常 200 执行，零判定 | 脚本 `C18` |
| C19 | 授权后同一调用翻盘 | alice `grant` 给 `probe/802` 后 bob 与 alice 分别调 | 响应 | alice 由 403 变 200；bob 仍 403 | 脚本 `C19` |
| C20 | 清空后回归拒绝 | alice `remove` 再调 | 响应 | 回到 403 | 脚本 `C20` |
| C21 | 读回某槽位的权限 | alice `POST /authData/fill` | 返回 rows 与 filledIntoAuthObject | 两处内容一致 | 脚本 `C21` |
| — | 字符串主键端到端 | 见本文 8.1 | — | 本示例按裁决不做 | 单测 `JdbcAuthDataHandlerTest` |

## D 数据范围

| 编号 | 内容 | 声明位置 | 观察点 | 期望 | 已验证到 |
| --- | --- | --- | --- | --- | --- |
| D1 | 被声明语句被注入 | `WorkOrderMapper.listRangeIn` | alice 调 `/workOrder/rangeList` + trace | 只回 1,2,4；语句里有 `IN (SELECT biz_data_id FROM demo_auth_data ...)` | 脚本 `D1` |
| D2 | 同链其他语句不受影响 | `DemoUserAuthMapper.listByUsername` 无声明 | trace 语句列表 | 装载权限那条原文下发 | 脚本 `D2` |
| D3 | 嵌套声明互不覆盖 | `listNested` 的 resultMap 嵌套 `listSameCustomer`（unified 对 edit） | trace 两条语句 | 各自按自己的槽位注入，外层不丢过滤 | 脚本 `D3` |
| D4 | 同表两槽位各判各的 | `listEditable` / `listApprovable` | 两个端点的行集 | 1,2,3 对 4,5 | 脚本 `D4` |
| D5 | 同请求先后两槽位不污染 | `GET /workOrder/twoSlots` | trace + 行集 | 第二次 editable 结果与第一次一致 | 脚本 `D5` |
| D6 | IN 与 EXISTS 等价 | `listRangeExists` | 两端点行集 | 完全相同 | 脚本 `D6` |
| D7 | 目标表与别名精确定位 | `listJoinCu` / `listJoinCb`（同表两个别名） | 两端点行集 | cu 得 1,3,5；cb 得 3,4,6 | 脚本 `D7` |
| D8 | dataIdColumn 非 id | `AssetMapper.listRange(dataIdColumn=asset_id)` | `GET /asset/list` dave | 只回 101,102 | 脚本 `D8` |
| D9 | 未声明不过滤 | `listAll` | `GET /workOrder/all` | 全部 6 行 | 脚本 `D9` |
| D10 | 写语句不被改写 | `WorkOrderMapper.updateStatus` | trace | 语句原文，无权限条件 | 脚本 `D10` |
| D11 | 动态语句只在执行期判定 | `listDynamic` / `listDynamicMissingTable` | 两端点 | 拼得出目标表时照常过滤；拼不出时抛异常，不返回未过滤结果 | 脚本 `D11` |
| D12 | 改写结果可观测 | 示例的观察插件 | `GET /demo/trace?traceId=` | 拿到改写后的最终语句 | 脚本 `D12` |
| D13 | 四类声明错误启动即失败 | 测试里各装一个坏接口 | 启动异常文本 | 解析不到/非查询/二级缓存/目标表不存在，各指到位置 | 测试 `AuthRangeStartupFailureTest` |
| D14 | 残留在业务 bean 上的声明 | 测试里 `LegacyPlacementService` | 启动异常文本 | 失败并说明"不在 MyBatis Mapper 接口上" | 测试 `AuthRangeStartupFailureTest` |
| D15 | 空权限集合返回零行 | 不带身份调 `rangeList` | 响应 | 200 + 空数组，语句带恒假条件，**不是** SQL 语法错误 | 脚本 `D15` |

## E 配置与数据

| 编号 | 内容 | 载体 | 已验证到 |
| --- | --- | --- | --- |
| E1 | 权限表表名与六个列名全部可重命名 | 本工程默认就是这么配的（见 3.3） | 脚本 `E1`（任何一条 C/D 场景通过即证明） |
| E2 | 数据落在项目 `data/` 下，可用 H2 客户端直接打开 | `data/auth_core_sample.mv.db` | 人工 |
| E3 | 重启后人工授权、人工改的菜单行仍在，种子不重复 | 三次重启实测行数一致 | 脚本 `--stage=restart` |

# 7. verify.sh 怎么用

```bash
cd sample/auth-core-boot-sample
bash verify.sh                      # 跑全部一次性能过的用例（需要应用已在跑）
bash verify.sh --stage=restart      # 只跑需要两次启动的用例（自己会提示重启时机）
BASE=http://localhost:8082/coredemo bash verify.sh   # 换地址
```

脚本只做 curl 与断言，不启停应用。每条用例打印 `PASS/FAIL 编号 说明`，末尾汇总。

# 8. 覆盖边界：这些不在本示例验证

| 场景 | 为什么不在这里 | 由谁验证 |
| --- | --- | --- |
| 字符串主键的行级判定端到端 | 按 2026-10-20 裁决，示例主键一律整型；框架的泛型能力留给外部项目 | `auth-core` 单测 `JdbcDataHandlerTest` 的归一化断言 |
| "菜单鉴权身份不可由请求改写"（SET 白名单） | 该场景依赖菜单**更新端点**，而更新端点属于权限模型层；core 只交 `list/saveBatch` | auth-rbac 的 `MenuApiUpdatableColumnsTest` 与 rbac 示例 |
| 菜单层级继承、角色/部门/用户体系、菜单管理界面 | 都是权限模型层的职责，不是接缝层 | auth-rbac 示例 |
| 登录、会话、记住我 | 属于 login 层 | `login-boot-sample` |
| MySQL 上的实际执行 | 本机无 MySQL 实例，未实测；脚本只写 MySQL 与 H2 双方接受的语法，并由 `scripts/check-sql-compat.sh` 用 H2 实测 | 需要使用者在 MySQL 上跑一遍 |

# 9. 本轮发现并修掉的框架问题

这一条清单是搭 demo 的直接产出——纯 core 接入才暴露得出来。

| # | 问题 | 处理 |
| --- | --- | --- |
| 1 | `AuthExceptionHandler` 只写了注解，既不在 `spring.factories` 也不在任何 `@Bean` 里 → 纯 core 接入时权限拒绝对外是 **HTTP 500**，规格里"统一拒绝语义"根本不成立 | `AuthCoreConfig` 里以 `@ConditionalOnWebApplication + @ConditionalOnMissingBean` 注册；使用方自带出口时框架出口让位 |
| 2 | `AuthServiceException`（用法错误）没有任何出口，与权限拒绝同形 → 违背"权限拒绝与配置错误区分表达" | 同一个 advice 里加 `code=400` 出口 |
| 3 | 权限集合为空时 `SqlAuthInject` 渲染出 `auth_str in ( )`，语法不成立 | 空集合改注入恒假条件 `1 = 0`（语义=看不到任何行，与写路径"空集合即拒绝"一致），并补了单测 |
| 4 | **`@ConditionalOnBean(SqlSessionFactory.class)` 使 `AuthRangeStartupValidator` 永不装配**：条件在配置类的注册阶段求值，而 MyBatis 的自动配置按类名排序在 `com.cu.*` 之后 → 声明缓存永远是空的，**所有 `@AuthDataRange` 静默失效、启动校验一起消失，应用还启动成功**。rbac 示例碰巧能用，只因为 MyBatis-Plus 的自动配置排在前面 | 改用 `ObjectProvider<SqlSessionFactory>` 在实例化阶段取；校验器原本就写好的"没有 SqlSessionFactory 就跳过"那条分支终于能用上了 |
| 5 | `AuthDataAdaptor` 是 core 的类，却只在 rbac 层的配置里被注册为 bean；纯 core 接入拿不到 | 未改框架，本示例自己 `@Bean` 声明（见 2.2），并在此登记 |

影响面提醒：第 1、2 条改完之后，`auth-rbac-boot-sample` 的拒绝响应体从"basic 层 Throwable 兜底的 generic code"变成 `code=403 / 400`。按 code 判失败分支的前端需要同步确认。

# 10. 已知债务（本轮按裁决不动）

- `SqlAuthInject.authTableWhereExpression` 把权限串作为**字面量**拼进改写后的语句。
  今天不可达的前提是权限串只由各位置的 **ID** 拼成，而 ID 是整型；
  一旦有外部项目用含引号的字符串权限ID接入，这里就是可打通的注入面。
  已就地留 TODO 注明触发条件与修法（做字面量转义，而不是改成 `?`——加占位符会打乱 MyBatis 的 parameterMappings）。
  代码位置：[SqlAuthInject.java](file:///C:/Users/kewen/Projects/kewen-framework/qy-auth/auth-core/src/main/java/com/cu/framework/auth/core/data/range/SqlAuthInject.java) 的 `authTableWhereExpression`。
  本示例刻意让含引号的权限串只走 `demo_auth_data` 的读写路径（已参数绑定）与 `quote_probe` 这个**没有任何数据范围声明**的业务功能，避免撞上这条债务。
- `menu-access` 规格里"两个洞与启动校验仍搁置"那部分（端点覆盖、并发注册）不在本轮范围。
