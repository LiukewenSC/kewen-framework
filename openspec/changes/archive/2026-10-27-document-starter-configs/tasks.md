# Tasks

## 1. 基线与自动装配入口

- [x] 1.1 在仓库根执行 `mvn -q -DskipTests compile`，确认 5 个含 `@ConfigurationProperties` 的模块生成 `target/classes/META-INF/spring-configuration-metadata.json`，并把当前 `properties[].name` 存到 `target/config-metadata-baseline.txt` 作为基线
  验证：5 个 metadata 文件存在，基线文件含全部 `cu.*` 键
- [x] 1.2 统一 7 个 `spring.factories` 的格式：`=` 后无空格、每行一个类、行尾 `,\`、末行无逗号、两空格缩进；`magic-spring-boot-starter` 不在范围
  验证：改前改后按逗号切分并排序的类集合完全一致；每个文件仍只有一个 `EnableAutoConfiguration` 键

## 2. 元数据与 javadoc

- [x] 2.1 `login-spring-boot-starter` 新增 `META-INF/additional-spring-configuration-metadata.json`，登记 `cu.auth.permit-url`（`String`，默认 `""`，描述说明与 `@SecurityIgnore` 合并、多个用分号或逗号分隔）
  验证：编译后 login 的 generated metadata 的 `properties` 含该 name
- [x] 2.2 把 `cu.auth.cache-auth` 条目从 `auth-core-spring-boot-starter` 的 additional metadata 迁到 `auth-rbac-spring-boot-starter` 的新 additional metadata
  验证：编译后 auth-core generated metadata 不再含该 name，auth-rbac generated metadata 含该 name；auth-rbac starter 对 auth-core starter 的依赖不变
- [x] 2.3 补/改 5 处 javadoc：`FangTangProperties.enabled`、`StorageProperties.type`、`SecurityProperties.currentUserUrl`、`Oauth2OidcProperties.authorizationGrantType`、`Oauth2OidcProperties.userInfoAuthenticationMethod`
  验证：`git diff` 只含注释行，字段与默认值未变；编译后 metadata 的 description 反映“未读取/未接线”标注

## 3. 漂移脚本与全量示例

- [x] 3.1 新增 `scripts/CheckConfigDocs.java` 与 `scripts/check-config-docs.sh`：Java 源文件模式、自动定位/可用 `SNAKEYAML_JAR` 覆盖、只解析 metadata 的 `properties` 数组、递归扁平化 YAML、缺失键非零退出
  验证：对完成的 `docs/application.yml` 退出码 0；临时删掉一个键后脚本非零并打印该键
- [x] 3.2 重写 `docs/application.yml`：文件头写三标记约定与“参考手册、非可直接启动”说明；按 basic → storage → tenant → auth-core → auth-rbac → login 分节；覆盖全部顶层与嵌套 Bean 键，默认值写实，无默认留空，逐键注释
  验证：`bash scripts/check-config-docs.sh` 通过；YAML 可被 SnakeYAML 解析
- [x] 3.3 人工核对 metadata 不覆盖的 List 元素键并写入骨架条目：`cu.security.login.oauth2.clients[]` 的 17 个子键、`saml.registrations[]` 的 4 个子键、`sp-signing-keystore` 的 4 个子键；注释注明“示例条目不是默认值；列表本身必填，留空启动失败”
  验证：逐字段对照 `Oauth2OidcProperties` / `SamlProperties` 勾选，无遗漏；`redirect-uri-template` 的值带引号
- [x] 3.4 对六处未接线/废弃键在 YAML 与对应 README 落实标注：`cu.security.current-user-url`、`cu.storage.type`、`authorization-grant-type`、`user-info-authentication-method`、`saml.login-page-url`、`remember-me.*`（只创建 Bean 未挂链）
  验证：`grep` 每个键在 YAML 与所属 README 均带 `[未接线]` / `[已废弃]` / 空转说明

## 4. Starter README

- [x] 4.1 新建 `boot/basic-spring-boot-starter/README.md`：6 个装配类、`cu.request.*` 与 `cu.notify.fang-tang.*` 表、两个方糖开关同时为 true 的条件、`sys_request_log` 等前置表、传递依赖说明
  验证：四段齐全；键表与 YAML basic 分节一致
- [x] 4.2 新建 `boot/storage-spring-boot-starter/README.md`：装配类、`cu.storage.*` 表、`type` 未接线、region 枚举、`is-public=false` 的签名行为、三张表前置
  验证：四段齐全；键表与 YAML storage 分节一致
- [x] 4.3 新建 `boot/tenant-spring-boot-starter/README.md`：两个装配类、`cu.tenant.open`、请求过滤器与 Feign 拦截器行为、`TenantContext` 前提
  验证：四段齐全；键表与 YAML tenant 分节一致
- [x] 4.4 扩写 `auth/auth-core-spring-boot-starter/README.md`：保留现有说明；补装配清单（`AuthCoreConfig` / `AuthWebConfig`）、`cu.auth.auth-data-table.*` 表、MyBatis / `JdbcTemplate` 前置、数据范围启动校验
  验证：四段齐全；键表与 YAML auth-core 分节一致
- [x] 4.5 扩写 `auth/auth-rbac-spring-boot-starter/README.md`：装配清单、`cu.auth.cache-auth`、`PasswordEncoder` / 当前用户上下文前置、`docs/sql/auth-rbac.sql`
  验证：四段齐全；键表与 YAML auth-rbac 分节一致
- [x] 4.6 扩写 `auth/auth-rbac-security-spring-boot-starter/README.md`：装配清单、无自有键、依赖 login starter、两个默认实现的让位规则、`PasswordEncoder` 仍需自备
  验证：四段齐全；明确写出“无自有配置键”
- [x] 4.7 补 `login-spring-boot-starter/README.md` 缺口：7 个装配类清单、顶层 `cu.security.*` 表（`current-user-url` / `session` / `remember-me`）、`cu.auth.permit-url`；三个协议已有章节不动
  验证：新增键表与 YAML login 分节一致；已有章节 `git diff` 为空

## 5. 根文档收敛

- [x] 5.1 删除根 `application.yml`，更新 `README.md` 目录树移除该条目
  验证：`git status` 显示删除；README 目录树无残留；全仓库 `grep` 不再引用根 `application.yml`
- [x] 5.2 修正 `README.md` 配置指南：`cu.message.*` → `cu.notify.fang-tang.*`，`cu.storage.qiniu.*` → `cu.storage.*`，权限/安全段改为指向各 starter README 与 `docs/application.yml`
  验证：指南中每个键都能在 `docs/application.yml` 找到；新增链接目标均存在

## 6. 集成验证

- [x] 6.1 仓库根执行 `mvn -q -DskipTests compile` 全量通过，metadata 生成无回归
  验证：命令退出码 0
- [x] 6.2 执行 `bash scripts/check-config-docs.sh`，输出被检查的 metadata 文件数与键数并以 0 退出
  验证：退出码 0；缺失键清单为空
- [x] 6.3 用脚本实际解析 `docs/application.yml`，抽查 `{baseUrl}` 引号、`root-path: ""`、空值键解析为 null
  验证：解析无异常；抽查项符合预期
- [x] 6.4 执行 `openspec validate document-starter-configs --strict`，并逐个检查 README 相对链接存在
  验证：命令退出码 0；无失效链接
