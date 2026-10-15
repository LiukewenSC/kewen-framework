# Tasks

## 1. 依赖与上下文骨架

- [x] 1.1 确认设计前提仍在：`BasicBootSampleApp` 带 `@SpringBootApplication(exclude = MybatisPlusConfig.class)`、`BasicBootSampleConfig` 提供 `DbType.H2` 的 `MybatisPlusInterceptor` bean。验证：直接读这两个文件比对；若已不符，停下重新确认 harness 前提，不要改生产代码（实测：两处均在；执行期间被提交进 f98ee5c）
- [x] 1.2 在 `sample/basic-boot-sample/pom.xml` 增加 `spring-boot-starter-test`（test scope，版本由 spring-boot-dependencies 管理），验证：新建一个空测试类后 `mvn -f sample/basic-boot-sample/pom.xml test-compile` 编译通过
- [x] 1.3 按 design D2 建立 `AbstractBasicSampleWebTest`（`@SpringBootTest(webEnvironment = MOCK)` + `@AutoConfigureMockMvc` + `@ActiveProfiles("h2")` + D3 的属性覆盖），配一个只请求 `GET /filter/hello` 并断言 HTTP 200 的临时测试类，验证：`mvn -f sample/basic-boot-sample/pom.xml test` 真实通过。**这是 D1 全量装配假设的唯一实测关口；若上下文起不来，先按 D1 退路切换到 slice + `@Import` 清单再继续，不要绕过**（实测通过，未触发退路）
- [x] 1.4 在 1.3 的测试中补一条断言：上下文中不存在 `FangTangMessageClient` bean，验证：该断言通过，且测试输出里没有任何方糖推送的 HTTP 请求日志（确认 CI 零出网）
- [x] 1.5 把 1.3 的临时测试类改造为第 2 组的正式测试类，验证：`test-compile` 通过且不再有临时/占位命名的测试类残留

## 2. 统一信封与流水号（FilterOrderTestController）

- [x] 2.1 断言成功响应的五字段信封：`code=200`、`success=true`、`message="成功"`、`data` 字段存在且为 null，验证：对应 spec「统一响应信封 / 请求处理成功」的测试通过
- [x] 2.2 断言流水号回显：不带流水号请求头时 `traceId` 为非空字符串；带 `traceId: <固定值>` 请求头时响应 `traceId` 等于该值，验证：spec「响应回显请求流水号」的两个场景各有对应测试并通过（断言写成"非空"而非"字段存在"，理由见 design Risks 第 3 条）

## 3. 日期参数绑定（JacksonSerializerController）

- [x] 3.1 `/jackson/paramSerializer`：`2024-01-01` 与 `2024-01-01 12:30:45` 返回成功信封；两参数全缺省也成功，验证：spec「日期参数按固定格式绑定」的前两个场景通过
- [x] 3.2 `/jackson/paramSerializer` 格式不符：以无分隔符的 `20240101` 提交，断言 **HTTP 500** 且响应为 `success=false`、`code=500` 的失败信封而不是未包装的异常栈，验证：spec「日期格式不符」场景通过；若实测状态码与推演的 500 不一致，以实测为准并同步修正 spec 措辞（实测 500，与推演一致）
- [x] 3.3 `/jackson/paramSerializer2`（对象属性绑定）按 3.1 同样口径断言，验证：证明格式约定对三种声明方式一致生效（spec 同一需求的第三个约束维度）
- [x] 3.4 `/jackson/paramSerializer3`（JSON 请求体）断言解析成功，并按 design D4 在断言上方注释标明该接口是 `@GetMapping` 配 `@RequestBody` 的非标准用法，验证：spec「JSON 请求体中的日期字段」场景通过

## 4. 日期字段序列化（deSerializerDate）

- [x] 4.1 `/jackson/deSerializerDate`：断言 `data` 中 `date`、`localDateTime` 匹配 `yyyy-MM-dd HH:mm:ss`、`localDate` 匹配 `yyyy-MM-dd`，且三者均为字符串而非数字时间戳，验证：spec「日期字段按固定格式序列化」场景通过；按 design D5 只断格式不断值

## 5. 参数校验（ValidatedTestController）

- [x] 5.1 `/validated/hello` 合法请求体返回成功信封，验证：spec「全部字段合法」场景通过
- [x] 5.2 `/validated/hello` 非法请求体（`id` 为 null 且 `name` 为空串）断言 **HTTP 200** + `code=500` + `success=false`，`message` 包含 `[id]` 与 `[name]`，验证：spec「必填字段缺失」场景通过。按 design D4 对完整 `message` 断言当前实际值（含被 `substring(0, length() - 2)` 多删一个字符的结果），并在断言上方一行注释写明该缺陷的位置与"修它时此测试应变红"（补了一个只让 `age` 单字段失败的用例来断完整字符串，因为多字段用例的默认提示文本随语言环境变化，无法钉死）

## 6. 集成校验

- [x] 6.1 三个测试类一次性跑通且只启动一个 Spring 上下文，验证：`mvn -f sample/basic-boot-sample/pom.xml test` 全绿，且输出中 Spring Boot 启动横幅只出现一次（若出现多次说明 D2 的基类配置被某个子类破坏）（实测 14 个测试全绿，横幅出现 1 次）
- [x] 6.2 确认改动面未越界，验证：`git status --short` 中本 change 新增的条目只有 `sample/basic-boot-sample/pom.xml`、`sample/basic-boot-sample/src/test/**` 和 `openspec/changes/add-basic-boot-sample-controller-tests/`（实测一致；apply 开始前工作区已有的无关改动在执行中被提交进 f98ee5c，未由其引入也未改动）

## 7. 回显断言（controller 改为返回绑定结果后追加）

- [x] 7.1 为 `/jackson/paramSerializer`、`/paramSerializer2`、`/paramSerializer3` 的成功用例补上 `data` 回显值断言（提交文本与回显文本逐字相同），验证：三个用例通过，spec「日期参数按固定格式绑定并回显」的往返一致约束成立
- [x] 7.2 为参数缺省用例补上 `data` 中两个键仍存在且值为 null 的断言，验证：`paramSerializer` 与 `paramSerializer2` 的缺省用例通过（`MapUtil.Builder` 基于 HashMap 且无条件 put，故 null 值的键不被丢弃）
- [x] 7.3 新增 JSON 请求体字段格式不符的用例，验证：实测为 HTTP 500 + 失败信封而不是 Spring 对不可读请求体默认的 400，据此补上 spec「请求体字段格式不符」场景
