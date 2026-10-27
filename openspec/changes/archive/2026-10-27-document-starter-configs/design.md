# Design

## Context

- 7 个自动装配 starter 已注册在 `spring.factories`；`magic-spring-boot-starter` 没有自动装配入口（模块内是 demo 启动类），不在范围。
- 配置面由 8 个 `@ConfigurationProperties` 类 + 4 个 `@Value` / 条件开关组成。其中 5 个键当前无消费方，remember-me 只创建 Bean 未挂链。
- 根 pom 统一引入 `spring-boot-configuration-processor`，javadoc 会生成 `spring-configuration-metadata.json`；3 个模块已有 `additional-spring-configuration-metadata.json`，缺 `cu.auth.permit-url`。
- 现有示例：根 `application.yml` 与 `docs/application.yml` 都过期且互相矛盾；`sample/login-boot-sample/application.yml` 是全量范本，但混有 demo 值与 profile 段。
- 技术约束：Boot 2.7.18 / javax；`spring.factories` 仍是当前官方支持方式；元数据里 List 元素字段（`clients[]` / `registrations[]`）不会生成独立条目。

## Goals / Non-Goals

**Goals:**

- 使用方在 starter README 内能查到：这个 starter 装配了什么、有哪些键、默认值、必填性、已知空转项。
- `docs/application.yml` 是唯一的全量键清单，可按 starter 小节单独复制。
- 新增顶层/嵌套 Bean 配置键后，漏写示例会被脚本发现。

**Non-Goals:**

- 不改变任何默认值、自动装配条件或运行时行为。
- 不把示例做成可直接启动的配置：login starter 的 `clients` / `registrations` 必填，且不能 invent 凭据。
- 不迁移 `.imports`，不补 remember-me 接线，不修 `current-user-url` 无消费方。
- 不把 README 配置表纳入自动校验；表格是人工摘要，允许比 YAML 简略。

## Decisions

### D1 单一全量示例 + 分模块 README

`docs/application.yml` 是唯一全量示例；README 不内嵌完整 YAML，只给配置表并指向对应小节。

- 备选：每个 starter 一份 `application-<module>.yml`——模块自治，但文件多、公共段（`cu.auth`）重复。
- 备选：README 内嵌完整 YAML——只有一份文档，但无法整体复制，且 README 过长。
- 选择理由：YAML 只有一份，最不容易漂移；模块说明仍在各 starter 旁边。

### D2 示例定位为参考手册

必填项留空并标注；可启动配置继续由 `sample/*/application.yml` 承担。

- 备选：可启动基线——需要为 `clients` / `registrations` / 方糖放占位凭据，容易被误认为真实配置。
- 备选：两套都维护——完整性最好，但维护两份配置。

### D3 键归属按实际消费方，不按 metadata 所在模块

| 键 / 前缀 | 归属 README | 属性来源 |
| --- | --- | --- |
| `cu.request.persistent.database`、`cu.request.message.fang-tang` | `basic-spring-boot-starter` | `@ConditionalOnProperty`（无属性类） |
| `cu.notify.fang-tang.*` | `basic-spring-boot-starter` | `FangTangProperties` |
| `cu.storage.*` | `storage-spring-boot-starter` | `StorageProperties` |
| `cu.tenant.open` | `tenant-spring-boot-starter` | `@ConditionalOnProperty` |
| `cu.auth.auth-data-table.*` | `auth-core-spring-boot-starter` | `AuthDataTableProperties` |
| `cu.auth.cache-auth` | `auth-rbac-spring-boot-starter` | `@Value` in `DefaultSysAuthMenuComposite` |
| `cu.auth.permit-url` | `login-security-spring-boot-starter` | `@Value` in `PropertiesUrlSecurityCustomizer` |
| `cu.security.*` | `login-security-spring-boot-starter` | `SecurityProperties` 等 7 个类 |
| （无自有键） | `auth-rbac-security-spring-boot-starter` | — |

`cu.auth` 在 YAML 中由三个 starter 共用，用分节注释隔开，不重复定义。

### D4 默认值以字段初始化值为唯一事实来源

- 有初始化值：YAML 写实值，例如 `maximum-sessions: 10`。
- 无初始化值：键留空，例如 `access-key:`，并在注释标 `[必填]` 或 `[未接线]`。
- 空串默认值必须写成 `""`（如 `root-path: ""`），与“无默认留空”区分。
- 布尔/数值不做文字化改写；不把代码默认值“优化”成更合理的值。

### D5 统一三标记

README 表格与 YAML 注释使用同一套词汇：

| 标记 | 含义 |
| --- | --- |
| `[必填]` | 无默认值，且缺失会导致启动失败或功能不可用；注明失败现象 |
| `[未接线]` | 属性存在但当前无消费方，配置后不生效 |
| `[已废弃]` | `@Deprecated` 且未实现 |

`remember-me.*` 单独标注“只创建 `RememberMeServices` Bean，未挂入过滤器链”。

### D6 README 固定四段

```markdown
# <module>

## 自动装配
| 配置类 | 生效条件 | 产出 |

## 配置项
### cu.xxx（PropertyClass）
| key | 默认值 | 必填 | 说明 |

## 启动前置条件与已知空转项

## 完整示例
指向 docs/application.yml 对应分节
```

自动装配清单以 `spring.factories` 为准，逐类核对条件注解与 Bean 产出；不写与配置无关的实现细节。

### D7 spring.factories 只做格式统一

- 统一为 `org.springframework.boot.autoconfigure.EnableAutoConfiguration=\` + 每行一个类 + 行尾 `,\`（末行无逗号）。
- 改前改后按“逗号切分 + 排序”比较类集合，保证不增不减、顺序不变。
- 不新增 `.imports`：Boot 2.7 同时支持，但双写会形成两份注册表；迁移属于 Boot 3 升级。

### D8 元数据补缺与迁移

- `login-security-spring-boot-starter` 新增 `META-INF/additional-spring-configuration-metadata.json`，登记 `cu.auth.permit-url`（`String`，默认 `""`）。
- `cu.auth.cache-auth` 条目从 `auth-core-spring-boot-starter` 移到 `auth-rbac-spring-boot-starter`；`auth-rbac-spring-boot-starter` 依赖 auth-core，迁移不影响现有使用方。
- javadoc 只改注释：`FangTangProperties.enabled` 补描述；`StorageProperties.type`、`SecurityProperties.currentUserUrl`、`Oauth2OidcProperties.authorizationGrantType` / `userInfoAuthenticationMethod` 标注“当前未读取”。字段与默认值不动。

### D9 漂移脚本：Java 源文件模式 + SnakeYAML

沿用 `scripts/check-sql-compat.sh` 的形态：

- `scripts/check-config-docs.sh`：发现 `*/target/classes/META-INF/spring-configuration-metadata.json`（要求先 `mvn compile`），从 `~/.m2` 定位 SnakeYAML jar（可用 `SNAKEYAML_JAR` 覆盖），执行 `java -cp "$SNAKEYAML_JAR" scripts/CheckConfigDocs.java docs/application.yml <metadata...>`。
- `CheckConfigDocs.java`：
  - JSON 只取 `properties` 数组的 `name`（手写括号匹配 + 正则，不引入 Jackson 三件套 classpath）；忽略 `groups`。
  - YAML 用 SnakeYAML 解析后递归扁平化；区分叶子值、空值、映射节点与列表节点。
  - metadata 名与 YAML 路径同名匹配；`[]` / `[*]` 元素键不在 metadata 中，脚本不覆盖，交由任务清单人工核对。
  - 输出缺失键清单并以非零退出。
- YAML 写法约束为文档约定：只用块映射、简单标量、列表；`{baseUrl}` 开头的值必须加引号。脚本是维护闸门，不承诺解析任意 YAML。

### D10 删除根 application.yml

仓库根没有启动类，该文件只是第二份示例；保留会与 `docs/application.yml` 漂移。README 目录树同步删除；文件受 git 管理，可恢复。

## Risks / Trade-offs

- [默认值在字段、README 表、YAML 三处出现] → 脚本只守 YAML；README 表保持摘要粒度，评审时以 YAML 为准。
- [metadata 不包含 List 元素字段] → 任务清单单独列出 `clients` / `registrations` 的逐键核对，脚本明确不覆盖。
- [SnakeYAML jar 定位失败] → 脚本报明确错误并支持 `SNAKEYAML_JAR` 覆盖，与 `H2_JAR` 的处理一致。
- [格式统一误删自动配置类] → 改前改后做类集合对比，任务中留验证步骤。
- [删除根 application.yml 影响外部引用] → 该文件不参与打包；README 目录树与配置指南同步更新，提交信息注明。
- [javadoc 修改会让 IDE 描述变化] → 仅注释，不触发任何运行时行为；编译后 metadata 描述随之更新是预期结果。

## Migration Plan

1. `mvn -q -DskipTests compile` 生成基线 metadata，记录当前键集合。
2. 改元数据与 javadoc，重新编译确认 metadata 包含 `cu.auth.permit-url`、不再包含 auth-core 的 `cu.auth.cache-auth`。
3. 重写 `docs/application.yml`，扩写 7 个 README。
4. 统一 `spring.factories` 格式并做题集对比。
5. 删除根 `application.yml`，修正 `README.md` 目录树与配置指南。
6. 运行 `scripts/check-config-docs.sh`，人工核对 List 元素键，校验 YAML 可解析。
7. 回滚策略：整次变更无运行时产物，回退提交即可。
