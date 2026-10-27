# relation

本目录存放框架产品自身需要的可执行 SQL 资产，按模块分目录。示例专用的建表与种子数据不在这里，仍在各示例的 `sample/*/sql/` 下。

## 脚本清单

| 脚本 | 归属模块 | 用途 | MySQL | H2(MODE=MySQL) |
| --- | --- | --- | --- | --- |
| [auth-rbac/auth-rbac.sql](auth-rbac/auth-rbac.sql) | `auth-rbac` | 权限体系 10 张表与 `view_menu_api_auth` 视图；`auth-core`、`auth-rbac-security` 共用其中的 `sys_auth_data` | 支持 | 支持 |
| [basic-support/basic-support.sql](basic-support/basic-support.sql) | `basic-support` | 文件存储 3 张表与请求日志表 `sys_request_log` | 支持 | 支持 |
| [login-security/spring-session-mysql.sql](login-security/spring-session-mysql.sql) | `login-security` | Spring Session JDBC 的会话表 | 仅支持 | 不支持 |

`relation/login-security/spring-session-mysql.sql` 是 MySQL 专用脚本，不能交给 H2 执行。H2 示例使用 `spring-session-jdbc` jar 内的 `schema-h2.sql` 自动建会话表；MySQL 部署则需要先手工执行本脚本，并把 `spring.session.jdbc.initialize-schema` 设为 `never`。

## 执行顺序

1. 权限体系：执行 `auth-rbac/auth-rbac.sql`。
2. 文件存储或请求日志：执行 `basic-support/basic-support.sql`。
3. MySQL 会话表：在应用首次启动前执行 `login-security/spring-session-mysql.sql`。
4. 示例业务表：按对应示例 README 执行 `sample/*/sql/schema.sql`，再执行 `seed-data.sql`。

`auth-rbac.sql` 与 `basic-support.sql` 开头包含 `DROP TABLE IF EXISTS`，重复执行会清空同名表；只应在初始化或明确要重建时运行。`spring-session-mysql.sql` 使用 `CREATE TABLE IF NOT EXISTS`，可以重复执行；如果库里还留着历史小写的 `spring_session` / `spring_session_attributes`，先按外键顺序删除，再执行本脚本。

## 与示例的关系

示例工程的 `spring.sql.init` 通过 pom 的 `<resources>` 把本目录下的脚本挂到 `classpath:sql/`，脚本文件名保持不变，所以示例里的 `classpath:sql/auth-rbac.sql`、`classpath:sql/basic-support.sql` 仍然有效。示例专用表（如 `meeting_room`、`demo_*`）的 DDL 与种子数据留在 `sample/auth-rbac-boot-sample/sql/`、`sample/auth-core-boot-sample/sql/`，不随框架发布。

## 改完 SQL 先跑检查

```bash
bash scripts/check-sql-compat.sh
```

脚本用 H2 的 MySQL 兼容模式实测 `relation/auth-rbac/*.sql`、`relation/basic-support/*.sql` 与 `sample/*/sql/*.sql`，检查三件事：每条语句 H2 都接受、脚本可重复执行、表真的建出来。`relation/login-security/` 因只支持 MySQL 被排除。H2 通过不等于 MySQL 通过，改完仍需在 MySQL 上过一遍。

## 相关文档

- [项目根 README](../README.md)
- [auth-rbac 模块](../auth/auth-rbac/README.md)
- [basic-support 模块](../basic-support/README.md)
- [login-security starter](../boot/login-security-spring-boot-starter/README.md)
- [维护脚本说明](../scripts/README.md)
