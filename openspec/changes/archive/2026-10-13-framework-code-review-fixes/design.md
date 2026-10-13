# Design

## Context

动机见 `proposal.md - Why`；行为要求见四个 spec delta。本文只记录实现层面的约束与取舍。

约束一：本仓库是 Spring Boot `2.7.18` / Spring Security `5.7.11`。经核验，该版本的 `OAuth2UserService` 接口**只有** `loadUser` 一个方法，没有 `supports()`；而 `DelegatingOAuth2UserService.loadUser` 的实现是对列表中**每一个** delegate 都调用一次 `loadUser` 再取首个非空结果。

约束二：`DefaultOAuth2UserService` 的 `RestOperations` 是构造期写入的实例字段，`setRestOperations` 为 `final` 且无按请求切换的入口。

约束三：`ignoreSsl` 字段位于 `Oauth2OidcProperties` 的客户端嵌套类中，天然是 per-client 语义。

约束四（历史考古结论，是本次若干决定的依据）：`RestOperationBuilder` 与 `enableSsl(false)` 出自同一个提交 `cf642d9`（提交说明"调用通了但是没有systemUser"），`Oauth2Config` 中引用 `IdaasSPOAuth2UserService` 的注释行也出自该提交，而该类在 git 全历史中从未存在过。次日 `4ffa11a` 以"Session 保存原始认证数据、使用时再转换"的方案收尾并删除了配套的 Provider。即：当初的故障是**响应解析**问题，SSL 豁免是同一次排障中顺带加入的，并非证书故障所致。

## Goals / Non-Goals

**Goals:**
- 把不安全默认值翻转为安全默认值，同时保留可显式开启的逃生口。
- 让"名字承诺的能力"与"实际提供的能力"一致，宁可删除也不补一个未经验证的实现。
- 删除实现之前，先把它的设计理由固化到不依赖代码的位置。

**Non-Goals:**
- 不重构 MDC 在 `@Async` / 线程池 / `CompletableFuture` 之间的传递机制。本次只保证同一线程内流水号成对清除；跨线程传播是独立议题。
- 不改造 `RestOperationBuilder` 的内部实现。
- 不处理 `JdbcAuthDataPersistent`（SQL 注入面与 `removeByIdsSql` 类型判断），该文件在本次实施中**不得修改**。
- 不为 remember-me 两个类建立 spec。原计划是删除 `AbstractHeaderRememberMeServices` 与其唯一子类 `PersistentHeaderRememberMeService`（两者均 `@Deprecated`、全仓零引用、未注册为 Bean），**实施期间作者决定保留**——废弃标记已足够表达态度，不必靠删除传达。本变更因此不触碰这两个类，其设计理由与两处未修复缺陷只记入决策文档第三节，作为"保留期间的已知风险"而非"删除理由"。

## Decisions

### D1 per-client SSL 用自建 registrationId 映射，不用 `DelegatingOAuth2UserService`

由约束一，`DelegatingOAuth2UserService` 会把 N 个身份源的 userinfo 请求全部发一遍，直接排除。由约束二，也无法在单个 `DefaultOAuth2UserService` 实例上按请求切换 `RestOperations`（会有并发竞态）。

因此：配置期按 `ignoreSsl` 分组，为每个 `registrationId` 构造**独立**的 `DefaultOAuth2UserService` 实例存入 `Map`；运行期按 `userRequest.getClientRegistration().getRegistrationId()` 查表。`OidcUserService` 同理，通过其 `setOauth2UserService` 注入对应实例。

**备选**：全局单一开关。成本更低，但会把 `ignoreSsl` 从 per-client 字段降级为全局字段，与约束三冲突，且"内网自签 IDP + 公网 IDP 并存"时被迫全局放宽。予以否决。

### D2 未匹配时 fail-secure

映射查不到 `registrationId` 时落到一个不调用 `setRestOperations` 的静态安全实例，而不是落到不安全实例。配置漏写应当导致登录失败（可诊断），而不是静默降级成不校验证书（不可诊断）。

### D3 只删调用点的 `.enableSsl(false)`，保留 `RestOperationBuilder`

约束四表明该构建器的实际价值在 `StringHttpMessageConverter` + `MappingJackson2HttpMessageConverter` + `OAuth2ErrorResponseErrorHandler`（处理非标准/HTML 响应），SSL 分支是附带产物。因此保留该类，把 `enableSsl` 交由配置驱动 —— 其字段默认值本就是 `true`，翻转默认只需调用点不再显式传 `false`。

同时改写 `Oauth2Config` 中"用来解决 Oauth2 默认的 ssl 证书校验"这句注释：它与 `RestOperationBuilder` 自己的类注释相互矛盾，正是这段历史被误读的原因。

### D4 OIDC 端点发现保持标准校验，形成有意的不对称

`ClientRegistrations.fromIssuerLocation` 使用 Spring 自带的 `RestTemplate`，执行证书校验。本次不改动它，因此即使某客户端 `ignore-ssl: true`，也只豁免 userinfo、不豁免 discovery。

这是刻意选择：discovery 失败发生在启动期且异常信息明确，为其放宽校验的代价远高于收益。已在 `identity/oauth2-login` spec 中固化为一条 scenario，避免后人当作遗漏"修好"。

### D5 键生成统一为"跳过空段后以 `/` 连接"

`rootPath` / `relativeDirectory` / `fileName` 三段过滤空白后 join，替代三处各自拼接。这同时修掉两个问题：签发键与提交键不一致，以及 `rootPath` 为空时客户端直传产出前导 `/`。

**不迁移历史对象。** 已存在的 `/dir/file` 形式键仍可被 `downloadUrl(key)` 正确访问（该方法直接使用传入的 key），只是新旧文件落在不同前缀下。需要规避该分裂的部署方，显式配置非空 `rootPath` 即可。

### D6 两个 HTTPS 开关区别对待

`useHttpsDomains` 作用于存储服务**自身的 API 域名**，官方域名本就支持 HTTPS，关闭它没有客观理由 → 默认值直接翻转为 `true`。

下载链接的协议作用于**使用方自己的域名**，走 HTTPS 需要该域名先在存储侧配好证书 → 提升为配置项且**默认保持 `false`**，不改变现有行为。

同一个"是否加密"的问题，因为控制的对象不同，一个翻转默认值、一个保持。合并处理会让未配证书的域名全部下载失败。

### D7 `TraceContext.remove()` 去掉参数，而不是修正方法体

`MDC.remove(traceId)` 的错在于参数存在且被误用。只把方法体改成 `MDC.remove(TRACE_ID_KEY)` 会留下一个"看起来该传点什么"的签名，同类误用在下次仍会发生。改为无参，调用点仅 `TraceRequestFilter` 一处。

### D8 `SPECIAL_CHAR*` 删 7 留 1，不逐个实现

8 个常量逐字节相同且仓内引用为 0。按名字真正实现意味着凭名字反推语义 —— 例如 `SPECIAL_CHAR_NO_EN_CN_NO_NUM_NO_SPECIAL_NO_SPACE_NO_DASH` 应当匹配什么，没有任何一处文档或测试能验证猜测结果。

删除是"承认该能力从未存在"，实现是"发明一个新能力并伪装成 bug 修复"。后者不应混入清理类变更。风险方向也已确认：现状不是"校验没生效"，而是"静默放宽校验"，因此删除比补一个未经验证的实现更安全。

### D9 决策记录用单个 md，代码只留指针

新增 `docs/说明文档/已废弃实现与决策记录.md`，条目结构：曾试过什么 / 为什么放弃 / 现在的替代方案在哪 / 对应 commit。收录三处：`Oauth2Config` 的 `IdaasSPOAuth2UserService` 墓碑注释、`RestOperationBuilder` 的 SSL 分支来历、`AbstractHeaderRememberMeServices` 类注释中"header 模式为什么没意义"的推理。

代码中只保留一行指向该文档的引用。**理由**：这些说明目前只活在注释块和 `@Deprecated` 类注释里，而注释块在下一次清理中会被当成垃圾扫掉；作者本人已回忆不起其中至少两处的来龙去脉。放 `openspec/` 不利于日常检索，按模块拆多个文件会让"为什么"重新变得分散。

## Risks / Tradeoffs

- **[去掉 SSL 豁免后，当初对接失败的 IDP 可能再次失败]** → 该环境可用时做回归验证。若验证不可行，本项以"字段已接线、默认安全"交付并在决策记录中标注"未验证"，不把 `enableSsl(false)` 当作必需行为保留。
- **[七牛服务端直传路径可能从未成功运行过]** → 签发键与提交键必然不一致，说明 `FileStorageEndpoint → DefaultFileStorageProcessor.upload → StorageTemplate.upload` 这条链路大概率未被生产使用。修复后需实际调用一次验证；若确认此前不可用，则本项是"启用了一条死路径"，其回归风险由"原本就没有可用"托底。
- **[墓碑注释的重构结论可能错]** → `IdaasSPOAuth2UserService` 从未入库，其意图只能从 `cf642d9` / `4ffa11a` 的提交说明与 `Oauth2AuthenticationSuccessResultConverter` 的现存形态推断。决策记录中须标注为"据 commit 推断"而非"作者确认"。
- **[三项 BREAKING 可能影响外部消费者]** → 按仓库既定的彻底扫改偏好接受，不保留兼容层，不做弃用过渡期。
- **[`SPECIAL_CHAR` 单值语义被误读为"允许特殊字符"]** → 保留的那个常量正则实际是"中文 + CJK 兼容汉字 + `\w`"，仍不含标点。其名称 `SPECIAL_CHAR` 本身也不准确。实施时在常量注释中如实描述其匹配范围，不重命名（重命名会扩大破坏面）。

## Migration Plan

1. 先落地无外部契约影响的项：`TraceContext`、`RegUtil` 正则字面量修正、两处 Controller 异常消息、`Objects.equals`。
2. 落地决策记录文档（D9），**在删除任何实现之前**完成，确保理由先落盘。
3. 落地 `storage`：键生成统一 + 三个配置项。发布说明中标注 `rootPath` 为空时键前缀的变化。
4. 落地 `oauth2-login`：SSL 默认翻转。使用方若依赖旧的跳过行为，需新增 `ignore-ssl: true`。
5. 删除 7 个 `SPECIAL_CHAR*` 常量，编译验证。（原同批的 remember-me 两个类经作者决定保留，未删除。）
6. 回滚策略：第 4 步是唯一可能引发线上登录故障的提交，独立成一个 commit 以便单独 revert。

## Open Questions

- `cu.message.fang-tang.key` 失去默认值后，未配置时发送方是抛 NPE 还是静默不发。实施第 1 步期间确认调用侧，若为 NPE 则补一条"未配置时不注册发送组件"的判空 —— 属实现细节，不影响 spec 与任务拆分。
