# scripts

维护脚本与它们依赖的 Java 检查器。两个脚本都不参与构建，只在仓库根目录手工运行。

## check-sql-compat.sh

用 H2 的 MySQL 兼容模式实测 SQL 脚本，检查三件事：

- 每条语句 H2 都接受；
- 脚本可重复执行（`spring.sql.init` 每次启动都会重跑）；
- 表真的建出来了。

扫描范围：

- `relation/auth-rbac/*.sql`
- `relation/basic-support/*.sql`
- `sample/*/sql/*.sql`

`relation/login-security/spring-session-mysql.sql` 是 MySQL 专用脚本，明确排除。

```bash
bash scripts/check-sql-compat.sh
# 没有自动找到 h2 jar 时：
H2_JAR=/path/to/h2.jar bash scripts/check-sql-compat.sh
```

环境变量：`H2_JAR`、`JAVA_BIN`。检查器是 `CheckCompat.java`，只依赖 H2 驱动。

**已知盲区**：

- H2 的 MySQL 模式通过，不代表 MySQL 通过；改完仍需在 MySQL 上跑一遍。
- 脚本按路径排序执行，样例子模块的 `schema.sql` 必须排在 `seed-data.sql` 前；重命名时注意顺序。
- “跑两遍”只证明在 H2 上可重复执行，不等于 MySQL 上的幂等与正确性。

## check-config-docs.sh

校验 `docs/application.yml` 覆盖所有 `@ConfigurationProperties` 与 additional metadata 中的配置键。

```bash
mvn -q -DskipTests compile
bash scripts/check-config-docs.sh
# 需要时：
SNAKEYAML_JAR=/path/to/snakeyaml.jar JAVA_BIN=/path/to/java bash scripts/check-config-docs.sh
```

前置：

- 先执行 `mvn -q -DskipTests compile`，生成各模块的 `spring-configuration-metadata.json`。
- 需要 SnakeYAML jar；脚本默认从 `~/.m2` 找最新版本，也可用 `SNAKEYAML_JAR` 指定。

环境变量：`SNAKEYAML_JAR`、`JAVA_BIN`。检查器是 `CheckConfigDocs.java`。

**已知盲区**：

- List 元素字段（`clients[]`、`registrations[]`）不在 metadata 中，脚本不覆盖，需要人工核对。
- 脚本只校验“覆盖”，不校验默认值文案与 README 表格是否一致。

## check-pom-version-sync.sh

版本号从 `cu-dependencies` BOM 之后散落在 5 类位置，升版本时漏改任何一处都会构建失败或静默取到旧版本。脚本把它们全部对齐到根 POM 的 `project/version`：

1. `pom.xml` 的 `<project><version>` —— 基准本身；
2. `pom.xml` 里 `cu-dependencies` 的 import 版本 —— 框架内部取版本表的入口；
3. `dependencies/pom.xml` 的 `<project><version>` —— 外部 import 时写的就是它；
4. `dependencies/pom.xml` 的 `<framework-version>` 属性 —— BOM 内全部 `com.cu.framework` 坐标的版本；
5. 每个子 POM 的 `<parent><version>` —— Maven 要求字面量。

```bash
bash scripts/check-pom-version-sync.sh
# 检查另一棵目录树（夹具）：
VERSION_SYNC_ROOT=/path/to/repo bash scripts/check-pom-version-sync.sh
```

`dependencies/pom.xml` 按设计没有 `<parent>`，不纳入第 5 项；`target/` 与 `logs/` 下的 pom 一律排除（夹具残留曾被当成子模块报“读不到 parent”）。

**已知盲区**：

- 只比对文本一致，不校验 Maven 能否解析；解析由 `mvn validate` 负责。
- 第 2 项是字面量，因此纳入检查。根治办法是版本单源（`${revision}` + flatten-maven-plugin），那是另一个 change；在它落地前靠本脚本兜。

## 相关文档

- [SQL 脚本](../relation/README.md)
- [全量配置参考](../docs/application.yml)
- [项目根 README](../README.md)
