## 1. 使用的系统与方式

本项目是一个 Maven 聚合工程，根 `pom.xml` (`com.cu.framework:cu-parent`) 通过 `<dependencyManagement>` 统一声明第三方依赖版本，子模块在各自 `pom.xml` 中仅引用 groupId/artifactId，不写 version。

- **构建工具**: Maven（无 Gradle/Node.js/Go 等其他语言包管理器）
- **仓库策略**: 未配置私有 Maven 仓库或 mirror；依赖解析走默认中央仓库 + 企业镜像（由本地 Maven settings 决定，项目内未锁定）。
- **锁文件**: 无 lockfile（无 `*.lock`、`BOM` 锁定等），依赖版本以 POM 中的版本号为准。
- **Vendoring**: 未发现 vendored 第三方代码目录，所有依赖均为远程仓库拉取。

## 2. 关键文件

- `pom.xml` — 根聚合 POM，定义 `framework-version`、`mybatis-plus-version` 等属性，并在 `<dependencyManagement>` 中集中声明全部依赖版本。
- 各子模块的 `pom.xml`（如 `auth/pom.xml`、`boot/pom.xml`、`basic/pom.xml`、`sample/*/pom.xml`、`other/code-generator/pom.xml`）— 仅引入需要的依赖坐标，不指定版本。

## 3. 架构与约定

### 3.1 版本属性层
根 POM 的 `<properties>` 暴露两个对外可改的版本变量：
- `framework-version=0.0.1` — 本项目自产模块的统一版本。
- `mybatis-plus-version=3.5.8` — MyBatis-Plus 系列依赖版本。

### 3.2 三方依赖 BOM 导入
根 POM 通过 `<scope>import</scope>` 引入三个上游 BOM，由其统一管理传递性依赖版本：
- `org.springframework.boot:spring-boot-dependencies:2.7.18`
- `org.springframework.cloud:spring-cloud-dependencies:2021.0.9`
- `com.alibaba.cloud:spring-cloud-alibaba-dependencies:2021.0.5.0`

### 3.3 本项目的自产模块版本统一
所有 `com.cu.framework.*` 子模块依赖均以 `${framework-version}` 引用，因此升级项目版本只需改根 POM 一处。提交 `be5962c chore(build): 统一子模块依赖版本由父POM管理` 即为此约定的落地。

### 3.4 已集中声明的主要三方库（节选）
| 领域 | 依赖 | 版本 |
|---|---|---|
| JSON | fastjson / fastjson2 | 2.0.9 / 2.0.25 |
| 工具集 | hutool-all | 5.8.18 |
| ORM | mybatis-plus-boot-starter / mybatis-plus / mybatis-plus-generator | 3.5.8 |
| SQL 解析 | jsqlparser | 4.7 |
| DB 驱动 | mysql-connector-java | 8.0.33 |
| 对象映射 | lombok | 1.18.30 |
| 缓存/集合 | guava | 32.1.3-jre |
| 存储 SDK | qiniu-java-sdk | 7.12.1 |
| API 文档 | springfox-swagger2 / swagger-bootstrap-ui | 2.9.2 / 1.9.6 |
| 通用工具 | commons-collections4 / commons-io | 4.3 / 2.11.0 |

### 3.5 构建插件
根 POM 的 `<build>` 仅声明 `maven-source-plugin`（`jar-no-fork`），用于生成源码包；未发现其他 CI 或自动化更新依赖的插件。

## 4. 约定与约束

- **版本声明位置**: 第三方依赖版本集中在根 POM 的 `<dependencyManagement>` 中声明；子模块引用时不写 `<version>` —— 这是 Maven 的强制行为，违反会编译失败。
- **本项目模块版本**: 所有 `com.cu.framework.*` 内部模块依赖统一使用 `${framework-version}`，不可在子模块中硬编码具体版本号。
- **Spring 生态版本对齐**: Spring Boot、Spring Cloud、Spring Cloud Alibaba 三者分别通过对应 BOM 锁定，版本分别为 2.7.18、2021.0.9、2021.0.5.0；新增相关依赖时应沿用 BOM 管理的版本，避免覆盖。
- **MyBatis-Plus 家族**: 通过 `mybatis-plus-version` 属性统一 mybatis-plus-boot-starter、mybatis-plus、mybatis-plus-generator 的版本，保持三者一致。
- **仓库来源**: 项目 POM 中未配置 `<repositories>` / `<pluginRepositories>`，依赖均从 Maven 中央仓库（及本地 `settings.xml` 配置的 mirror）获取；若需私有仓库，需在外部 Maven settings 中配置而非修改项目 POM。
- **无锁文件 / 无 vendoring**: 不存在 `pom.lock`、`vendor/` 等机制，依赖版本以 POM 文本为准，每次构建都可能拉取最新快照（除非命中本地缓存）。
- **变更依据**: 提交 `be5962c` 明确“统一子模块依赖版本由父 POM 管理”，是上述约定的直接证据。