# Proposal

## Why

所有 sample 目前要么必须连本地 MySQL 才能启动，要么 H2 配置散落在 profile 之外，验证一个示例要先装库、建表、灌种子数据。框架侧的建表脚本已在 `docs/sql/` 统一成 MySQL 与 H2(MODE=MySQL) 通用的一份，并有 `scripts/check-sql-compat.sh` 实测校验，但只有 2/5 个 sample 接上了这条路；已接上的两个还存在一激活 `mysql` profile 就每次启动 `DROP TABLE` 真库的数据丢失风险。

## What Changes

- 5 个 sample 的 profile 统一为 `h2` 与 `mysql` 两个，**H2 数据源、H2 建表脚本、H2 种子数据只出现在 `h2` profile 段内**；框架模块与非 sample 环境一律仍指向 MySQL，不引入任何 H2 配置或依赖。
- `spring.sql.init`（`mode` / `schema-locations` / `data-locations`）从各 sample 的顶层文档段移入 `h2` profile 段，顶层不再声明。修掉 basic 与 storage 现有的激活 `mysql` 后每次启动重建并清空真库表的问题。
- **BREAKING** `sample/auth-rbac-boot-sample/sql/init.sql` 按用途重新归堆，拆为 `sql/schema.sql`（示例专属建表）与 `sql/seed-data.sql`（全部种子 INSERT），原 `init.sql` 不再存在；README 中的手动导入命令随之改为两条。
- `init.sql` 中的 `SPRING_SESSION` / `SPRING_SESSION_ATTRIBUTES` 建表段删除，交由 spring-session 的 `spring.session.jdbc.initialize-schema` 独占，消除双头建表。
- `docs/sql` 挂载到 `classpath:sql/` 的 `<resources>` 从 `basic-boot-sample`、`storage-boot-sample` 两个 pom 上提到 `sample/pom.xml`；`auth-rbac-boot-sample` 补挂自身 `sql/` 目录（该目录当前不在 classpath 上，`spring.sql.init` 读不到）。
- `login-boot-sample` 的 H2 数据源从主 `spring.datasource` 段移入 `h2` profile，补 `mysql` profile；`tenant-boot-sample` 补 H2 依赖与两个 profile（不需要任何建表脚本）。
- **BREAKING** `auth-rbac-boot-sample` 的 `dev` profile 退役，`application-dev.yml` 并入 `mysql` profile；此前依赖 `--spring.profiles.active=dev` 的启动方式失效。
- 修正 `jdbc:pyspy:mysql://` → `jdbc:p6spy:mysql://`（basic、storage 各一处），使 `mysql` profile 真正可连。
- 修正被 YAML 读成字符串的 Hikari 配置（`auto-commit:true`、`connection-test-query:select 1` 冒号后缺空格），并同步 README 中 `auth-boot-sample` 的旧模块名与目录树。

## Capabilities

### New Capabilities

- `sample/demo-database-profiles`: 约束 sample 示例工程按 profile 选择数据库的行为 —— H2 的可达范围、MySQL 兜底的正确性、建表与种子脚本的归属和加载顺序。

### Modified Capabilities

无。`storage/file-storage`、`basic/request-response-contract` 等现有 spec 描述的是框架行为，本次不改框架模块，其需求不变。

## Impact

- **配置**：`sample/*/src/main/resources/application.yml`（5 个），`sample/auth-rbac-boot-sample/src/main/resources/application-dev.yml`（移除），`sample/login-boot-sample/src/main/resources/application.yml`
- **SQL**：`sample/auth-rbac-boot-sample/sql/init.sql` → `sql/schema.sql` + `sql/seed-data.sql`；`docs/sql/` 不变
- **构建**：`sample/pom.xml`（新增 `<build><resources>`），`auth-rbac-boot-sample/pom.xml`、`login-boot-sample/pom.xml`、`tenant-boot-sample/pom.xml`（补 `com.h2database:h2` runtime 依赖与资源挂载），`basic-boot-sample/pom.xml`、`storage-boot-sample/pom.xml`（移除已上提的重复挂载）
- **文档**：`README.md`（目录树、手动导入命令、profile 说明）
- **不在范围**：不给 sample 新增 `src/test` 与自动化测试；不改动框架模块的 SQL、依赖或配置；不处理已提交进公开仓库的 MySQL 凭据吊销
- **验收**：每个 sample 在不安装 MySQL 的前提下以默认 profile 启动成功，且 `bash scripts/check-sql-compat.sh` 保持通过
