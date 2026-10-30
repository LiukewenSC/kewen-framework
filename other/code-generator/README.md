# code-generator

## 这是什么

MyBatis-Plus `FastAutoGenerator` 的封装：读取数据库表，按本仓库的命名与模板生成 entity、mapper、mapper.xml、service、controller。它不是 starter，也没有 CLI 参数；运行前直接修改 `Properties.java`。

## 何时使用

- 新增业务表后，需要批量生成 MyBatis-Plus 的样板代码。
- 想沿用本仓库的 `*MpMapper`、`*MpService`、`*MpController` 命名与模板。
- 不适用：只需要一两张表、手写更快；或需要生成非 MyBatis-Plus 的代码。

## 5 分钟使用

1. 修改 `src/main/java/com/cu/framework/code/generator/Properties.java`：
   - `url` / `username` / `password`：目标数据库。
   - `absolutePath`：工程根目录（`src` 的上一层）；留空时按 classpath 推断。
   - `basePackage`：生成代码的父包，生成到 `<basePackage>.mp` 下。
   - `containsTablePrefix` / `ignoreTablePrefix`：表名前缀白名单 / 黑名单。
   - `author`、`orderByDescProperty`、`enableSwagger`。
2. 运行 `CodeGenerator.main` 或 `Properties.main`（IDE 直接运行即可）。
3. 检查输出：
   - Java 文件：`<absolutePath>/src/main/java/<basePackage>/mp/{entity,mapper,service,controller}`
   - XML：`<absolutePath>/src/main/resources/mapper/mp`

## 模板

模板在 `src/main/resources/templates/`：

| 模板 | 用途 | 是否启用 |
| --- | --- | --- |
| `entity.java.vm` | 实体 | 是 |
| `mapper.java.vm` | Mapper 接口 | 是 |
| `mapper.xml.vm` | Mapper XML | 是 |
| `service.java.vm` | Service 接口 | 是 |
| `controller.java.vm` | Controller | 是 |
| `serviceImpl.java.vm` | Service 实现 | 否：生成器调用了 `disableServiceImpl()` |
| `service.java.vm.bak` | 备份文件 | 否 |

生成器使用 Velocity，`orderByDescProperty` 通过 `customMap` 注入模板。

## 前置条件与已知限制

- 需要目标库可连接，且 `containsTablePrefix` 能匹配到表；匹配不到且白名单非空时抛 `未查询到表`。
- `Properties` 是硬编码字段，没有外部配置文件；每次调整都要改代码并重新运行。
- 只生成 `mp` 子包；`auth-rbac` 样例的 `mp` 结构就是它的产物。
- `serviceImpl.java.vm` 当前未启用，修改它不会影响生成结果。
- 生成器会覆盖同名文件（`enableFileOverride`），运行前先确认目标文件没有手工改动。
- `enableSwagger=true` 时模板仍输出 `io.swagger.annotations.*` 注解。本框架已不再管理 springfox / swagger 依赖，生成后的工程需要自行引入 `io.swagger:swagger-annotations`，或把模板改为 springdoc 的 `io.swagger.v3.oas.annotations`。

## 相关文档

- [项目根 README](../../README.md)
- [auth-rbac 示例](../../sample/auth-rbac-boot-sample/README.md)
- [MyBatis-Plus 官方文档](https://baomidou.com/)
