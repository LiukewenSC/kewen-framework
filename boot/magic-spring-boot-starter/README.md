# magic-spring-boot-starter

## 这是什么

`magic-api` 的接入示例模块，**不是生产可用的正式 starter**。它把 `magic-api-spring-boot-starter`、JDBC 与一个示例启动类放在一起，用来演示 magic-api 的接口脚本如何与本框架的 `Result` 一起工作。

当前行为：

- `SampleMagicApiApp` 是示例启动类，放在 `src/main/java`，不是测试代码。
- `HelloController` 的 `/hello/hello` 与 `/magic/user/all` 都返回固定字符串 `hello`，不是真实业务数据。
- `application-magic.sample` 是示例配置，包含固定的 `secret-key: 123456789`、数据库资源存储与 `magic` 数据源引用，仅用于本地演示。

## 何时使用

- 想了解 magic-api 如何嵌入本框架，或本地试用 magic-api。
- 不适用：生产项目。接入前需要把示例配置、固定密钥、数据源与权限边界全部重做，并自行评估 magic-api 的版本与安全策略。

## 5 分钟本地演示

**依赖**（模块内已声明）：

```xml
<dependency>
    <groupId>org.ssssssss</groupId>
    <artifactId>magic-api-spring-boot-starter</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
```

**配置**：把 `src/main/resources/application-magic.sample` 复制为 `application.yml`，按你的库修改 `magic-api.resource.datasource` 与数据源；示例里的 `magic` 数据源需要你自行提供。

**启动**：运行 `com.cu.framework.boot.magic.SampleMagicApiApp`。

**验证**：

```bash
curl http://localhost:8080/hello/hello
# {"code":200,"success":true,"message":"成功","data":"hello"}
```

magic-api 的 Web 界面默认在 `/magic/web`；接口脚本、资源表与权限策略以 magic-api 官方文档为准。

## 自动装配

本模块没有自己的 `spring.factories` 自动配置；`MagicResultConfig` 只是示例用的响应适配，`HelloController` 是示例端点。实际能力来自 `magic-api-spring-boot-starter`。

## 配置项

没有框架自有配置键。示例配置见 `src/main/resources/application-magic.sample`，其中：

| 配置 | 示例值 | 说明 |
| --- | --- | --- |
| `magic-api.web` | `/magic/web` | Web 界面地址 |
| `magic-api.resource.type` | `database` | 脚本存数据库 |
| `magic-api.resource.tableName` | `magic_api_file` | 资源表 |
| `magic-api.resource.datasource` | `magic` | 需要项目提供同名数据源 |
| `magic-api.secret-key` | `123456789` | **仅本地演示**；生产必须替换 |
| `magic-api.show-sql` | `true` | 打印 SQL |

## 前置条件与已知空转项

- 需要 JDBC DataSource 与 magic-api 的资源表；示例不负责建表。
- 固定 `secret-key` 只用于本地；不要带到生产。
- `HelloController` 返回固定值，不能作为业务能力参考。
- 本模块没有测试与完整装配验证；把它当示例，不要当稳定 API。

## 相关文档

- [项目根 README](../../README.md)
- [basic](../../basic/README.md)
- [basic-spring-boot-starter](../basic-spring-boot-starter/README.md)
