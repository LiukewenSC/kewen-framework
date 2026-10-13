# Proposal

## Why

一轮代码扫描 + 人工核验暴露出 11 处既有缺陷，集中在三类：安全默认值取反（OAuth2 userinfo 无条件跳过 TLS 校验）、跨平台契约不一致（七牛上传 key 三处生成规则互不相同、MDC 流水号永不清除）、以及"名字承诺了不存在的能力"（8 个 `SPECIAL_CHAR*` 常量逐字节相同）。这些都不是新增功能，而是让现有行为回到设计上应有的样子；其中 `enableSsl(false)` 硬编码会影响**所有**引入该 starter 的应用，属于最高优先。

同时，本次核验发现多段被注释掉或标记 `@Deprecated` 的实现，其设计理由只活在注释块里、作者本人已回忆不起。这些理由必须在删除代码之前被还原并落到不依赖代码的位置，否则随代码一并消失。

## What Changes

- **OAuth2/OIDC userinfo 的 TLS 校验默认改为开启**。`Oauth2Config` 不再硬编码 `enableSsl(false)`；`Oauth2OidcProperties` 中已声明但全仓零读取的 `ignoreSsl` 字段被真正接线，按 `registrationId` 生效，未匹配的注册落到安全实例（fail-secure）。**BREAKING**：依赖"自动跳过证书"的现有部署需为对应客户端显式配置 `cu.security.login.oauth2.clients[*].ignore-ssl: true`。
- 修正 `Oauth2OidcProperties` 中 `ignoreSsl` 的注释（原文"生产环境务必保持 true"与字段语义相反）。
- **七牛存储 key 生成统一为单一规则**，`upload()` 的凭证签发 key 与实际上传 key 一致，且与 `createPreUploadToken()` 采用同一套含 `rootPath` 的拼接逻辑并跳过空段。**BREAKING**：`rootPath` 为空时客户端直传的 key 由 `/dir/file` 变为 `dir/file`，历史对象不迁移。
- 七牛 `persistentNotifyUrl`、`Region`、下载链接是否走 HTTPS 三项由硬编码提升为 `StorageProperties` 配置项；`useHttpsDomains` 默认改为 `true`（下载协议开关默认保持 `false` 以不破坏未配证书的域名）。
- `TraceContext.remove()` 去掉参数并改用 `LoggerConstant.TRACE_ID_KEY` 作为 MDC key，使 `TraceRequestFilter` 的 `finally` 清理真正生效。
- `RegUtil` 正则修正：`CHINESE` 的 `,{0,}` 笔误、`MOBILE` 字符类内的字面逗号、`PHONE`/`MOBILE` 号段过期；`DOUBLE` 改为引用 `FLOAT`；**删除 7 个与 `SPECIAL_CHAR` 完全同值的常量**。**BREAKING**（public API 移除，仓内引用为 0）。
- `RbacDeptController` / `RbacRoleController` 的 update 校验异常消息由复制粘贴残留的"用户ID为空"改为与实体一致，并修正 `RbacRoleController` 的形参名 `sysUser`。
- `RbacMenuApiController` 的 `Integer` 引用比较改为 `Objects.equals`，并保持 `authType` 为 `null` 时不抛 NPE。
- ~~**删除 `AbstractHeaderRememberMeServices` 及其唯一子类 `PersistentHeaderRememberMeService``**~~ —— **实施期间经作者决定保留**，不删除。两个类继续以 `@Deprecated` 形式存在，其设计理由与两处未修复缺陷（`setHeader(name, null)` 不清除响应头、`key` 默认 `"123456"`）记入决策记录文档第三节。本项不构成分支内的破坏性变更。
- 新增 `docs/说明文档/已废弃实现与决策记录.md`，承载上述被删除/迁出实现的设计理由，以及保留但已废弃实现的已知风险。代码中只保留指向该文档的一行引用。

**明确排除**：`JdbcAuthDataPersistent` 的 SQL 拼接注入面与 `removeByIdsSql` 类型判断错误。该问题由用户决定单独讨论，不并入本次清理，实施阶段不得顺手修改该文件。

## Capabilities

### New Capabilities

- `identity/oauth2-login`: 多 IDP 登录时 userinfo 请求的 TLS 校验行为，以及 `ignoreSsl` 按注册生效、缺省 fail-secure 的契约。
- `storage/file-storage`: 对象存储的 key 生成规则，与区域、回调地址、传输协议等端点参数的可配置性契约。
- `observability/request-tracing`: 请求流水号写入与必须成对清除的上下文契约。
- `basic/regex-validation`: 正则常量集合的成员构成，以及"常量名必须与其正则语义一致"的契约。

### Modified Capabilities

（无 —— 本仓库尚未建立任何主 spec，以上四个 capability 均为首次引入。）

## Impact

**代码**：`qy-idaas/idaas-authentications`（`Oauth2Config`、`Oauth2OidcProperties`）、`boot/storage-spring-boot-starter`（`QiNiuStorageTemplate`、`StorageProperties`、`StorageAutoConfiguration`）、`boot/basic-spring-boot-starter`（`FangTangProperties` 已由 `27d6c6d` 处理，本次仅确认调用侧无 NPE）、`basic`（`TraceContext`、`TraceRequestFilter`、`RegUtil`）、`qy-auth/auth-rbac`（三个 Controller）。`qy-auth/auth-spring-boot-starter` 的 remember-me 两个类经作者决定**未改动**。

**对外契约**：两项 BREAKING —— OAuth2 TLS 默认值翻转、`SPECIAL_CHAR*` 常量删除。本框架无外部消费者仓内可见，按用户既定偏好接受破坏并在后续自行处理。

**运行时数据**：七牛历史对象的 key 前缀不回溯迁移；`downloadUrl()` 直接使用传入 key，不受影响。

**依赖**：无新增依赖。`RestOperationBuilder` 保留（其 `HttpMessageConverter` 与 `OAuth2ErrorResponseErrorHandler` 配置才是当初 IDP 对接的实际修复点）。

**验证负担**：`enableSsl` 翻转需要用当初对接报错的 idaas 环境做一次回归；若该环境不可用，本项降级为"保留配置项但默认安全"并记录未验证。
