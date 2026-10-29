## 1. 构建系统与总体架构

项目采用 **Maven 多模块聚合工程**，根 `pom.xml`（artifactId=`cu-parent`，packaging=`pom`）声明所有子模块与统一依赖版本。子模块按功能域分目录：`basic`、`basic-support`、`boot`、`auth`、`sample`、`other`，以及存放 SQL 的 `relation/` 和文档 `docs/`。

- 构建工具：Maven（`modelVersion=4.0.0`），Java 源/目标级别统一为 `8`（`maven.compiler.source/target`），编码 `UTF-8`。
- 框架版本：Spring Boot `2.7.18`、Spring Cloud `2021.0.9`、Spring Cloud Alibaba `2021.0.5.0`、MyBatis-Plus `3.5.8`，均通过根 `<dependencyManagement>` + `<scope>import</scope>` 的 BOM 方式引入，子模块不重复声明版本。
- 内部模块版本号统一由属性 `${framework-version}` 控制（当前 `0.0.1`），提交记录 `be5962c` 明确说明“统一子模块依赖版本由父 POM 管理”。

## 2. 关键文件

- `pom.xml`：聚合根，集中声明 Spring Boot / Spring Cloud / MyBatis-Plus / Fastjson / Hutool / Lombok / Qiniu 等第三方依赖版本，以及各子模块 artifactId 到 `${framework-version}` 的版本映射。
- `scripts/README.md`：两个维护脚本的使用说明（不参与 Maven 构建）。
- `scripts/check-sql-compat.sh` + `scripts/CheckCompat.java`：用 H2 MySQL 兼容模式验证 `relation/*` 与 `sample/*/sql/*.sql` 的可执行性、幂等性与建表结果。
- `scripts/check-config-docs.sh` + `scripts/CheckConfigDocs.java`：校验 `docs/application.yml` 覆盖所有 `@ConfigurationProperties` 及 `additional-spring-configuration-metadata.json` 中的配置键。
- `clean-sensitive-files.sh`：从 Git 历史中删除 `.jks`、`logs/`、`target/` 的脚本。
- `sample/auth-core-boot-sample/verify.sh`：针对 auth-core 示例的端到端 API 验收脚本（基于 curl，需应用已启动）。
- 各 sample 子模块 pom 中的 `spring-boot-maven-plugin`：用于打包可运行的 JAR。

## 3. 构建约定与约束

### 3.1 版本管理
- 所有内部模块使用统一的 `${framework-version}`（根 POM 定义），子模块引用时不写具体版本。
- 外部依赖通过 `<dependencyManagement>` 的 `<scope>import</scope>` 引入 Spring Boot / Spring Cloud / Spring Cloud Alibaba BOM，子模块仅声明 groupId/artifactId。
- MyBatis-Plus 未使用其 BOM，而是逐个依赖声明 `${mybatis-plus-version}`（根 POM 内被注释掉的 `mybatis-plus-bom` 表明曾考虑过但弃用）。

### 3.2 子模块结构
- 每个 starter 模块遵循 Spring Boot Starter 规范：在 `src/main/resources/META-INF/spring.factories`（或 `META-INF/additional-spring-configuration-metadata.json`）注册自动装配类。
- 模块名以 `-spring-boot-starter` 结尾（如 `storage-spring-boot-starter`、`tenant-spring-boot-starter`、`login-security-spring-boot-starter`）。

### 3.3 构建产物
- 根 POM 未显式配置 `maven-source-plugin` 插件行为（仅在 dependencyManagement 中出现 artifactId），因此源码包生成依赖 Maven 默认生命周期；样例模块中显式引入了 `spring-boot-maven-plugin` 以生成可执行 JAR。
- 未发现 Dockerfile、CI 配置文件（`.github/`、`.gitee/` 下仅有 PR/Issue 模板）、发布流水线脚本。

### 3.4 维护脚本规则
- `check-sql-compat.sh` 扫描范围固定为 `relation/auth-rbac/*.sql`、`relation/basic-support/*.sql`、`sample/*/sql/*.sql`，并明确排除 `relation/login-security/spring-session-mysql.sql`（MySQL 专用）。
- 该脚本按路径排序执行，因此样例子模块的 `schema.sql` 必须排在 `seed-data.sql` 之前（README 中作为已知盲区强调）。
- `check-config-docs.sh` 要求先执行 `mvn -q -DskipTests compile` 生成 `spring-configuration-metadata.json`，再运行脚本；它只校验“覆盖”，不校验默认值文案与 README 表格一致性。
- 两个脚本均不参与 Maven 构建，需在仓库根目录手工运行。

### 3.5 安全清理
- `clean-sensitive-files.sh` 使用 `git filter-branch` 从所有分支与标签中移除 `.jks`、`logs/`、`target/`，并输出强制推送步骤；脚本本身会修改 Git 历史，需要开发者知悉风险。

## 4. 缺失项

仓库中未发现以下构建相关工件：
- 无 `Makefile`、无 CI 流水线（GitHub Actions / GitLab CI / Gitee Pipelines）。
- 无 `Dockerfile` / `docker-compose.yml`。
- 无 `flatten-maven-plugin`、`maven-deploy-plugin`、`maven-release-plugin` 等发布插件配置。
- 无 `assembly` / `shade` 插件，sample 模块通过 `spring-boot-maven-plugin` 直接打包可执行 JAR。

综上，该项目的构建体系是一个**以 Maven 聚合 POM 为中心的轻量级方案**：依赖版本集中在根 POM，子模块以 Spring Boot Starter 形式组织，额外的质量检查（SQL 兼容性、配置文档覆盖）通过独立 shell + Java 检查器完成，但没有自动化 CI 与容器化流程。