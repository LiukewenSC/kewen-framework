# Tasks

本仓库测试栈为 JUnit 4（`org.junit.Test` / `org.junit.Assert`），`basic/pom.xml` 已声明 junit 依赖。`basic`、`boot/*`、`qy-idaas` 目前无 `src/test` 目录，相关任务需先建立。

## 1. 决策记录先落盘（必须早于任何删除动作）

- [x] 1.1 新建 `docs/说明文档/已废弃实现与决策记录.md`，按"曾试过什么 / 为什么放弃 / 现在的替代方案在哪 / 对应 commit"四段结构写入三条：`Oauth2Config` 中 `IdaasSPOAuth2UserService` 墓碑注释、`RestOperationBuilder` 的 SSL 分支来历（`cf642d9` 引入、`4ffa11a` 收尾、真实故障是响应解析而非证书）、`AbstractHeaderRememberMeServices` 类注释中"header 模式为什么没意义、被 SpringSession 取代"的推理。验证方式：三条均已写入，且每条的 commit 号可用 `git show <hash>` 复核存在

- [x] 1.2 在文档中对第一条明确标注"据 commit 推断，非作者确认"（该类在 git 全历史中从未存在，意图无法直接考证）。验证方式：文档中该条含此标注

- [x] 1.3 确认 `docs/说明文档/` 下既有文档（如 `oidc接入踩坑记录.md`）中关于 `ignoreSsl` 的表述与本次翻转后的行为一致，不一致则同步修改。验证方式：全仓检索 `ignoreSsl` 与 `ignore-ssl`，各处描述不再互相矛盾

## 2. 基础模块：流水号上下文与正则常量

- [x] 2.1 `TraceContext.remove()` 去掉 `String` 参数，方法体改为 `MDC.remove(LoggerConstant.TRACE_ID_KEY)`；同步修改唯一调用点 `TraceRequestFilter` 的 `finally` 块。验证方式：`grep -rn "TraceContext.remove" --include=*.java .` 结果中不再出现带参调用

- [x] 2.2 新建 `basic/src/test/java/.../logger/trace/TraceContextTest.java`，覆盖 `observability/request-tracing` 的三条 scenario：清除后固定键不存在、流水号值恰好等于其他文本时仍清除正确键、set/remove 成对后线程复用不残留。验证方式：`mvn -pl basic test` 通过

- [x] 2.3 `RegUtil` 正则字面量修正：`CHINESE` 改为 `^[\u4e00-\u9fa5]+$`（去掉 `,{0,}` 笔误）；`MOBILE` 去掉字符类内的字面逗号并更新号段；`PHONE` 补齐在用号段；`DOUBLE` 改为引用 `FLOAT` 而非重复字面量。验证方式：`CHINESE` 匹配"你好"为 true、匹配"你好,,,"为 false；`MOBILE` 匹配含逗号串为 false

- [x] 2.4 删除 `RegUtil` 中与 `SPECIAL_CHAR` 逐字节相同的 7 个 `SPECIAL_CHAR_NO_*` 常量，并为保留的 `SPECIAL_CHAR` 补一条如实描述其匹配范围（中文 + CJK 兼容汉字 + `\w`，不含标点）的注释，不重命名。验证方式：`grep -c "SPECIAL_CHAR" RegUtil.java` 仅剩 1 处定义；`mvn -q compile` 全仓编译通过

- [x] 2.5 新建 `basic/src/test/java/.../utils/RegUtilTest.java`，覆盖 `basic/regex-validation` 的三条 scenario，含"集合内不存在名称不同而正则相同的成员"这一条。验证方式：`mvn -pl basic test` 通过

## 3. RBAC 控制器：错误提示与比较方式

- [x] 3.1 `RbacDeptController` update 的异常消息改为"部门ID为空"；`RbacRoleController` update 的异常消息改为"角色ID为空"，并将其形参名 `sysUser` 改为 `sysRole`（含方法体内引用）。验证方式：两文件 `grep -n "用户ID为空"` 无结果，`grep -n "sysUser" RbacRoleController.java` 在该方法内无结果

- [x] 3.2 `RbacMenuApiController.updateMenu` 的 `req.getAuthType()== AUTH_TYPE.OWNER` 改为 `Objects.equals(...)`，并确认 `authType` 为 `null` 时不抛 NPE、行为与改动前一致（`null` 不等于 OWNER 即跳过该校验）。验证方式：新增或补充 `../../../../auth` 下的用例，覆盖 `authType` 为 `null`、`1`、`2` 三种输入的分支结果

## 4. 存储模块：键生成统一与端点参数配置化

- [x] 4.1 在 `QiNiuStorageTemplate` 中新增私有键生成方法（`rootPath` / `relativeDirectory` / `fileName` 过滤空白段后以 `/` 连接），`upload()` 与 `createPreUploadToken()` 均改为调用它；`upload()` 中 `uploadManager.put` 提交同一个 key 变量而非 `fileName`。验证方式：`storage/file-storage` spec 中"两条路径生成同一文件的键"与"空路径段不产生多余分隔符"两组 scenario 对应的测试用例通过

- [x] 4.2 `StorageProperties` 新增异步处理通知地址、存储区域、下载链接是否加密三项配置；`persistentNotifyUrl` 未配置时不写入 putPolicy 且删除硬编码的第三方域名，`StorageAutoConfiguration` 中 `Region.region2()` 改为读配置，`DownloadUrl` 的协议参数改为读配置且**默认 `false`**。验证方式：`grep -n "oicp.vip\|region2()" ` 在 `boot/storage-spring-boot-starter` 下无结果

- [x] 4.3 `Configuration.useHttpsDomains` 由 `false` 改为 `true`。验证方式：该字段赋值处不再为 `false`，且与 4.2 的下载协议开关相互独立（前者无配置项、恒为加密）

- [x] 4.4 新建 `boot/storage-spring-boot-starter/src/test/.../QiNiuStorageTemplateTest.java`，用固定输入断言键生成结果（含 `rootPath` 为空、`relativeDirectory` 为空两种边界），并断言签发凭证所用 key 与提交 key 为同一值。验证方式：`mvn -pl boot/storage-spring-boot-starter test` 通过

## 5. OAuth2/OIDC：TLS 校验默认翻转与 per-client 接线

- [x] 5.1 在 `Oauth2Config` 中按 `ignoreSsl` 分组构造 `Map<registrationId, OAuth2UserService>`，配置期完成实例创建，运行期按 `userRequest.getClientRegistration().getRegistrationId()` 查表；查不到时落到不调用 `setRestOperations` 的静态安全实例。**不要使用 `DelegatingOAuth2UserService`**（Spring Security 5.7.11 下它会对每个 delegate 都发一次 `loadUser`）。验证方式：`identity/oauth2-login` spec 的"未匹配身份源回落安全实例"scenario 对应测试通过

- [x] 5.2 删除 `ignoreSslOAuth2UserService()` 中的 `.enableSsl(false)` 硬编码，改为由配置驱动 `enableSsl(c.isIgnoreSsl())`；`OidcUserService` 通过 `setOauth2UserService` 注入对应实例而非共享不安全实例。验证方式：未配置 `ignore-ssl` 时，构造出的 `RestOperations` 走 `RestOperationBuilder` 的 `enableSsl=true` 分支（可通过日志或断点确认未调用 `addSsl`）

- [x] 5.3 在 `ignoreSsl=true` 生效路径上输出可检索的告警日志，指明是哪个 `registration-id` 处于不校验状态。验证方式：配置一个 `ignore-ssl: true` 客户端启动应用，日志中出现该 registrationId

- [x] 5.4 改写 `Oauth2Config` 中"用来解决 Oauth2 默认的 ssl 证书校验"这句与 `RestOperationBuilder` 类注释矛盾的说明，并把 `//return new IdaasSPOAuth2UserService();` 替换为一行指向决策记录文档的引用。验证方式：`grep -n "IdaasSPOAuth2UserService" Oauth2Config.java` 仅剩文档引用形式

- [x] 5.5 修正 `Oauth2OidcProperties.ignoreSsl` 的注释：生产环境应取值 `false`。验证方式：注释文本与字段语义一致，且与 1.3 同步后的文档不冲突

- [x] 5.6 新建 `qy-idaas/idaas-authentications/src/test/.../Oauth2UserServiceWiringTest.java`，覆盖 per-client 混合配置（一个 `ignore-ssl: true`、一个未配置）时两者拿到的实例校验行为不同。验证方式：`mvn -pl qy-idaas/idaas-authentications test` 通过

- [x] 5.7 用当初对接报错的 idaas 环境做一次回归：去掉 SSL 豁免后重新走一遍登录。若该环境不可用，在决策记录文档中标注"未验证"，**不要**为此把 `enableSsl(false)` 恢复成默认。验证方式：回归结论（通过 / 失败 / 未验证）以文字形式记入决策记录文档

## 6. 删除废弃实现（本组作废，见下）

> **实施期间变更**：本组原计划删除 `AbstractHeaderRememberMeServices` 与其唯一子类
> `PersistentHeaderRememberMeService`。删除动作曾执行过，2026-10-13 作者决定**保留**
> ——废弃标记本身已足够表达态度，不需要靠删除来传达。两个类已恢复原样，内容未改动。
> 以下两条按未执行记录，不做粉饰。

- [ ] 6.1 ~~一并删除 `AbstractHeaderRememberMeServices` 与其唯一子类 `PersistentHeaderRememberMeService`~~ —— **未执行，作者决定保留**。删除前的核验结论仍然成立且已转入决策记录：`PersistentHeaderRememberMeService` 是全仓唯一子类，两者均无 `@Component`/`@Bean`、`*.factories` 与 `*.imports` 中无登记、除彼此外零引用，因此保留与否不影响现有应用的启动与运行

- [ ] 6.2 ~~确认自动配置未因缺 Bean 而失败~~ —— 随 6.1 一并作废。实际采用的核验方式也已不需要（未删除即无缺 Bean 风险），全仓 `mvn clean test` 20 个模块通过

- [x] 6.3 把 `docs/说明文档/已废弃实现与决策记录.md` 第三节从"删除理由"改写为"保留期间的已知风险"：新增"当前状态：保留，但已废弃"小节，说明两处缺陷（`setHeader(name, null)` 不清除响应头、`key` 默认 `"123456"` 可被用于伪造 remember-me 认证）**至今仍然存在**，并把"现在的替代方案"改为指向 `SessionConfig` 的 `SpringSessionRememberMeServices`。验证方式：该节不再出现"已删除"表述，且明确标注本变更未触碰这两个类

## 7. 集成验证

- [x] 7.1 全仓构建并跑全部测试：`mvn -q clean test`。验证方式：构建成功，无失败用例

- [x] 7.2 确认 `JdbcAuthDataPersistent.java` 未被本次任何改动触及（该文件的 SQL 注入问题由用户决定单独讨论）。验证方式：`git diff --name-only` 与 `git status --short` 的结果中不包含该文件

- [x] 7.3 核对两项 BREAKING（OAuth2 TLS 默认翻转、`SPECIAL_CHAR*` 删除）已写入变更说明，供下游使用方升级时对照。remember-me 两个类经作者决定保留，原第三条 BREAKING 已撤销。验证方式：说明文档中两条均可逐条指认，且含完整配置键
