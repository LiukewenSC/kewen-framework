# Kewen Framework

> 基于 Spring Boot 的企业级权限与登录安全框架：注解驱动的菜单 / 数据权限、默认 RBAC 实现、账号密码与 OAuth2 / OIDC / SAML2 单点登录，以及基础 Web、文件存储与多租户能力。

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![JDK](https://img.shields.io/badge/JDK-8+-green.svg)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)]()
[![Maven](https://img.shields.io/badge/Maven-3.6+-red.svg)]()

## Overview (English)

Kewen Framework is a Java 8+ / Spring Boot 2.7 multi-module framework for authorization and login in enterprise applications. It is built around four composable capabilities:

- **Menu permission** — `@AuthMenu` / `@AuthMenuDoc` register menu entries at startup and match request paths.
- **Row-level data scope** — `@AuthDataRange` sits on MyBatis mapper methods and injects permission conditions at statement level; declarations are validated at startup.
- **Data operation** — `@AuthDataOperation` collects the IDs a request intends to modify and checks them one by one.
- **Login** — JSON username/password login, plus OAuth2 / OIDC / SAML2 single sign-on built on Spring Security.

A default user–department–role–menu RBAC implementation is included, along with Spring Boot starters for basic web utilities, file storage, multi-tenancy and an experimental magic-api integration. Every starter ships with a runnable sample application. You can adopt the whole stack, or use only the annotations and SPI seams and bring your own permission model.

Licensed under the MIT License.

## 这是什么

Kewen Framework 是一个 Java + Spring Boot 多模块框架，专注解决企业应用里的权限与登录问题。它把“菜单能不能进、列表能看哪些行、这条数据能不能改、权限怎么配”拆成四个可组合的能力，并提供一套默认的用户-部门-角色-菜单实现。你可以整套引入，也可以只用注解与接缝层，自建权限模型。

## 能力与状态

| 能力 | 说明 | 状态 |
| --- | --- | --- |
| 菜单权限 | `@AuthMenu` / `@AuthMenuDoc`；启动时注册菜单行，请求路径精确匹配 | 可用 |
| 数据范围 | `@AuthDataRange` 声明在 Mapper 查询方法上，按语句注入权限条件；启动期校验声明 | 可用 |
| 数据操作 | `@AuthDataOperation` 收集 `DataIdCarrier` 交出的 ID，逐个判定，全部命中才放行 | 可用 |
| 权限编辑 | `AuthDataAdaptor` 命令式编辑 / 读取 / 删除某条数据的权限 | 可用；旧 `@AuthDataAuthEdit` 已移除 |
| 默认 RBAC | 用户-部门-角色-菜单，`RELATIONDEPT` 只覆盖严格子孙部门 | 可用 |
| 账号密码登录 | JSON 登录、会话并发控制、统一 `SecurityUser` | 可用；`remember-me` 当前未接线 |
| OAuth2 / OIDC | 授权码模式，OIDC 支持 discovery / JWKS / ID Token 校验 | 可用；OIDC 登出未接线 |
| SAML2 | SP 发起、metadata 解析、SP Metadata 端点、SLO | 可用；只文档化 SP 发起 |
| 基础 Web | 统一 `Result`、异常处理、traceId、请求日志、MyBatis-Plus 分页、方糖通知 | 可用 |
| 文件存储 | 七牛云上传 / 下载 / 回调 / 文件记录 | 可用；当前固定七牛，`cu.storage.type` 未接线 |
| 多租户 | `tenantId` 请求头上下文与 Feign 透传 | 可用；不做 SQL 级隔离 |
| Magic API | magic-api 接入示例 | 示例 / 实验性，不建议直接用于生产 |
| 代码生成 | MyBatis-Plus 样板代码生成 | 可用；配置硬编码在 `Properties` |

## 技术栈

| 类别 | 技术 | 版本 |
| --- | --- | --- |
| 语言 / 构建 | Java / Maven | 8+ / 3.6+ |
| 核心框架 | Spring Boot / Spring Security | 2.7.18 / 5.7.x |
| 持久层 | MyBatis-Plus / MySQL | 3.5.8 / 8.0 |
| SQL 解析 | JSQLParser | 由 MyBatis-Plus BOM 管理 |
| JSON | FastJSON2 / Jackson | 随项目 BOM |
| 工具 | Hutool / Lombok / Guava | 随项目 BOM |
| 会话 | spring-session-jdbc | 随 Spring Boot BOM |
| 单点登录 | spring-security-oauth2-client / spring-security-saml2-service-provider | 随 Spring Security BOM |

## 模块地图

| 模块 | 一句话 | 入口 |
| --- | --- | --- |
| `basic` | 统一响应、异常、trace、请求日志与工具类 | [basic/README.md](basic/README.md) |
| `basic-support` | 请求日志落库、方糖通知、MyBatis-Plus 分页工具 | [basic-support/README.md](basic-support/README.md) |
| `boot/basic-spring-boot-starter` | 基础 Web 能力的自动装配 | [README](boot/basic-spring-boot-starter/README.md) |
| `boot/login-security-spring-boot-starter` | 账号密码、OAuth2 / OIDC / SAML2 登录 | [README](boot/login-security-spring-boot-starter/README.md) |
| `boot/storage-spring-boot-starter` | 七牛文件存储 | [README](boot/storage-spring-boot-starter/README.md) |
| `boot/tenant-spring-boot-starter` | 多租户上下文与 Feign 透传 | [README](boot/tenant-spring-boot-starter/README.md) |
| `boot/magic-spring-boot-starter` | magic-api 示例 / 实验性模块 | [README](boot/magic-spring-boot-starter/README.md) |
| `auth/auth-core` | 注解、权限模型接口、数据范围注入、菜单拦截器 | [README](auth/auth-core/README.md) |
| `auth/auth-rbac` | 用户-部门-角色-菜单的默认 RBAC 实现 | [README](auth/auth-rbac/README.md) |
| `auth/auth-core-spring-boot-starter` | core 自动装配 | [README](auth/auth-core-spring-boot-starter/README.md) |
| `auth/auth-rbac-spring-boot-starter` | RBAC 自动装配 | [README](auth/auth-rbac-spring-boot-starter/README.md) |
| `auth/auth-rbac-security-spring-boot-starter` | RBAC 接入 Spring Security 登录态 | [README](auth/auth-rbac-security-spring-boot-starter/README.md) |
| `sample/auth-core-boot-sample` | core 接缝层的独立验证台 | [README](sample/auth-core-boot-sample/README.md) |
| `sample/auth-rbac-boot-sample` | RBAC + 会议室权限案例 | [README](sample/auth-rbac-boot-sample/README.md) |
| `sample/login-boot-sample` | 账密登录与三协议入口示例 | [README](sample/login-boot-sample/README.md) |
| `sample/basic-boot-sample` | 基础 Web 能力示例 | [README](sample/basic-boot-sample/README.md) |
| `sample/storage-boot-sample` | 七牛存储接入示例 | [README](sample/storage-boot-sample/README.md) |
| `sample/tenant-boot-sample` | 多租户上下文示例 | [README](sample/tenant-boot-sample/README.md) |
| `other/code-generator` | MyBatis-Plus 代码生成器 | [README](other/code-generator/README.md) |

```text
kewen-framework/
+-- basic/                 基础库
+-- basic-support/         持久化与通知扩展
+-- boot/                  5 个 Spring Boot Starter
+-- auth/                  core、rbac 与 3 个 starter
+-- sample/                6 个可运行示例
+-- other/code-generator/  代码生成器
+-- relation/              框架 DDL（示例 DDL 仍在 sample/*/sql/）
+-- docs/                  全量配置参考与决策记录
+-- scripts/               SQL / 配置文档校验脚本
+-- openspec/              运行时规格与变更记录（维护者）
```

## 5 分钟跑起来

### 路径一：H2 示例，零外部依赖

先看 RBAC 与会议室权限案例：

```bash
git clone https://gitee.com/LiuKewenSc/kewen-framework.git
cd kewen-framework
mvn clean install -DskipTests

cd sample/auth-rbac-boot-sample
java -jar target/auth-rbac-boot-sample-0.0.1-exec.jar
```

> 该示例请用打包后的 exec jar 启动；`mvn spring-boot:run` 在当前工程下可能扫描不到权限实体，表现为 `权限标记错误: ROLE`。

另开一个终端：

```bash
curl -H "X-Demo-User: admin" "http://localhost:8081/meetingRoom/page?page=1&size=10"
```

- 默认 H2 内存库，端口 8081；H2 控制台 <http://localhost:8081/h2-console>。
- 身份由 `X-Demo-User` 声明，不带时回落到 `admin`；换 `admin2` / `subordinate` / `outsider` 可以观察部门继承的差异。
- 示例没有登录页；账密与 SSO 看 `sample/login-boot-sample`。

登录与单点登录示例：

```bash
cd sample/login-boot-sample
mvn spring-boot:run
```

打开 <http://localhost:8080/login.html>，账密账号 `admin/admin123`、`demo/demo123`。默认配置含一个 OIDC 客户端，启动时会访问本地 IDP（端口 9010）；没有 IDP 时先注释掉 `demo-oidc` 条目。多数示例都用 8080，不要同时启动多个。

### 路径二：接入自己的 MySQL 项目

完整 RBAC + Spring Security 登录态的组合：

```xml
<parent>
    <groupId>com.cu.framework</groupId>
    <artifactId>cu-parent</artifactId>
    <version>0.0.1</version>
</parent>

<dependencies>
    <dependency>
        <groupId>com.cu.framework.boot</groupId>
        <artifactId>basic-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>com.cu.framework.auth</groupId>
        <artifactId>auth-rbac-security-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
</dependencies>
```

1. 执行 [relation/auth-rbac/auth-rbac.sql](relation/auth-rbac/auth-rbac.sql) 建权限表。
2. 使用 MySQL 持久化会话时，执行 [relation/login-security/spring-session-mysql.sql](relation/login-security/spring-session-mysql.sql)，并设置 `spring.session.jdbc.initialize-schema: never`。
3. 配数据源与 `cu.security.current-user-url`（默认 `/currentUser`），准备 `sys_user` / `sys_user_credential` 等用户数据。
4. 启动后 `POST /login`（JSON）登录，`GET /currentUser` 取当前用户。

更细的依赖组合、必需 Bean 与验证方式见 [auth/README.md](auth/README.md) 的“5 分钟接入”。

## 文档导航

| 我想… | 读… |
| --- | --- |
| 先跑一个示例 | [auth-rbac 示例](sample/auth-rbac-boot-sample/README.md) / [login 示例](sample/login-boot-sample/README.md) |
| 看权限注解怎么用 | [权限注解与权限编辑](auth/docs/annotations.md) |
| 看 RBAC 表、权限串、部门继承与扩展 | [RBAC 模型与扩展](auth/docs/rbac-and-extension.md) |
| 接登录 / 单点登录 | [login starter](boot/login-security-spring-boot-starter/README.md) 与 [SSO 总览](boot/login-security-spring-boot-starter/docs/sso-overview.md) |
| 对 OAuth2 / OIDC / SAML2 细节 | [OAuth2](boot/login-security-spring-boot-starter/docs/oauth2.md) / [OIDC](boot/login-security-spring-boot-starter/docs/oidc.md) / [SAML2](boot/login-security-spring-boot-starter/docs/saml2.md) |
| 查全量配置键 | [docs/application.yml](docs/application.yml) 与各 starter README |
| 找 SQL 脚本 | [relation/README.md](relation/README.md) |
| 看代码生成器 | [other/code-generator](other/code-generator/README.md) |
| 跑维护脚本 | [scripts/README.md](scripts/README.md) |
| 看历史决策与废弃原因 | [docs/decisions](docs/decisions/已废弃实现与决策记录.md) |
| 看运行时规格（维护者） | [openspec/specs](openspec/specs) |

## 参与贡献

1. Fork 本仓库，创建特性分支。
2. 修改后至少跑一遍：
   ```bash
   mvn clean install -DskipTests
   bash scripts/check-sql-compat.sh
   bash scripts/check-config-docs.sh   # 先 mvn -q -DskipTests compile
   ```
3. 提交信息沿用 `feat(scope): ...`、`fix(scope): ...`、`docs(scope): ...` 风格。
4. 提交 Pull Request，说明改动目的、影响面与验证方式。

Issue / PR 模板在 `.gitee/` 下。安全问题不要公开提交 Issue，先私下联系维护者。

## 许可证

本项目采用 MIT 许可证，见 [LICENSE](LICENSE)。

## 相关资源

- Gitee 仓库：<https://gitee.com/LiuKewenSc/kewen-framework>
- 前端模板：<https://gitee.com/LiuKewenSc/kewen-web-admin>
- 作者：kewen
