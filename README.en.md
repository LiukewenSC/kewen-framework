# Kewen Framework

English | [简体中文](README.md)

> A Spring Boot-based framework for enterprise authorization and login security: annotation-driven menu and data permissions, a default RBAC implementation, username/password plus OAuth2 / OIDC / SAML2 single sign-on, and reusable web, file storage and multi-tenancy capabilities.

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![JDK](https://img.shields.io/badge/JDK-21+-green.svg)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.2-brightgreen.svg)]()
[![Maven](https://img.shields.io/badge/Maven-3.6+-red.svg)]()

## What is this

Kewen Framework is a Java 21 + Spring Boot 4.1 multi-module framework focused on authorization and login in enterprise applications. It decomposes "can I enter this menu, which rows of a list can I see, can I modify this record, and how are permissions configured" into four composable capabilities, and ships a default user–department–role–menu implementation. You can adopt it as a whole, or use only the annotations and the seam layer to build your own permission model.

## Capabilities and status

| Capability | Description | Status |
| --- | --- | --- |
| Menu permission | `@AuthMenu` / `@AuthMenuDoc`; menu rows are registered at startup, request paths are matched exactly | Available |
| Data scope | `@AuthDataRange` is declared on mapper query methods; permission conditions are injected per statement, declarations are validated at startup | Available |
| Data operation | `@AuthDataOperation` collects the IDs exposed by `DataIdCarrier` and checks them one by one; the request is allowed only if all of them match | Available |
| Permission editing | `AuthDataAdaptor` imperatively edits / reads / deletes the permissions of a single record | Available; the old `@AuthDataAuthEdit` has been removed |
| Default RBAC | User–department–role–menu; `RELATIONDEPT` covers strict descendant departments only | Available |
| Username/password login | JSON login, session concurrency control, unified `SecurityUser` | Available; `remember-me` is not wired up yet |
| OAuth2 / OIDC | Authorization code flow; OIDC supports discovery / JWKS / ID token validation | Available; OIDC logout is not wired up |
| SAML2 | SP-initiated flow, metadata parsing, SP metadata endpoint, SLO | Available; only the SP-initiated flow is documented |
| Basic web | Unified `Result`, exception handling, traceId, request logging, MyBatis-Plus pagination, ServerChan notifications | Available |
| File storage | Qiniu Cloud upload / download / callback / file records | Available; Qiniu is hard-wired, `cu.storage.type` is not wired up |
| Multi-tenancy | `tenantId` request header context and Feign propagation | Available; no SQL-level isolation |
| Code generation | MyBatis-Plus boilerplate generation | Available; configuration is hard-coded in `Properties` |

## Tech stack

| Category | Technology | Version |
| --- | --- | --- |
| Language / build | Java / Maven | 21+ / 3.6+ |
| Core framework | Spring Boot / Spring Security | 4.1.2 / 7.1.x |
| Persistence | MyBatis-Plus / MySQL | 3.5.18 / 8.0+ |
| SQL parsing | JSQLParser | 4.9 (managed by MyBatis-Plus) |
| JSON | FastJSON2 / Jackson | 2.0.66 / 3.x |
| Utilities | Hutool / Lombok / Guava | From the project BOM |
| Session | spring-session-jdbc | From the Spring Boot BOM |
| Single sign-on | spring-security-oauth2-client / spring-security-saml2-service-provider | From the Spring Security BOM |

## Module map

Module-level READMEs are written in Chinese.

| Module | In one line | Entry |
| --- | --- | --- |
| `basic` | Unified response, exceptions, trace, request logging and utilities | [basic/README.md](basic/README.md) |
| `basic-support` | Request log persistence, ServerChan notifications, MyBatis-Plus pagination utilities | [basic-support/README.md](basic-support/README.md) |
| `boot/basic-spring-boot-starter` | Auto-configuration for the basic web capabilities | [README](boot/basic-spring-boot-starter/README.md) |
| `boot/login-security-spring-boot-starter` | Username/password, OAuth2 / OIDC / SAML2 login | [README](boot/login-security-spring-boot-starter/README.md) |
| `boot/storage-spring-boot-starter` | Qiniu Cloud file storage | [README](boot/storage-spring-boot-starter/README.md) |
| `boot/tenant-spring-boot-starter` | Multi-tenant context and Feign propagation | [README](boot/tenant-spring-boot-starter/README.md) |
| `auth/auth-core` | Annotations, permission model interfaces, data scope injection, menu interceptor | [README](auth/auth-core/README.md) |
| `auth/auth-rbac` | Default user–department–role–menu RBAC implementation | [README](auth/auth-rbac/README.md) |
| `auth/auth-core-spring-boot-starter` | Core auto-configuration | [README](auth/auth-core-spring-boot-starter/README.md) |
| `auth/auth-rbac-spring-boot-starter` | RBAC auto-configuration | [README](auth/auth-rbac-spring-boot-starter/README.md) |
| `auth/auth-rbac-security-spring-boot-starter` | Connects RBAC to the Spring Security login context | [README](auth/auth-rbac-security-spring-boot-starter/README.md) |
| `sample/auth-core-boot-sample` | Standalone test bench for the core seam layer | [README](sample/auth-core-boot-sample/README.md) |
| `sample/auth-rbac-boot-sample` | RBAC + meeting-room permission case | [README](sample/auth-rbac-boot-sample/README.md) |
| `sample/auth-rbac-security-boot-sample` | Login identity + RBAC integration test bench | [README](sample/auth-rbac-security-boot-sample/README.md) |
| `sample/login-boot-sample` | Username/password login and the three SSO entry points | [README](sample/login-boot-sample/README.md) |
| `sample/basic-boot-sample` | Basic web capabilities sample | [README](sample/basic-boot-sample/README.md) |
| `sample/storage-boot-sample` | Qiniu storage integration sample | [README](sample/storage-boot-sample/README.md) |
| `sample/tenant-boot-sample` | Multi-tenant context sample | [README](sample/tenant-boot-sample/README.md) |
| `other/code-generator` | MyBatis-Plus code generator | [README](other/code-generator/README.md) |

```text
kewen-framework/
+-- basic/                 Core library
+-- basic-support/         Persistence and notification extensions
+-- boot/                  4 Spring Boot starters
+-- auth/                  core, rbac and 3 starters
+-- sample/                7 runnable samples
+-- other/code-generator/  Code generator
+-- relation/              Framework DDL (sample DDL stays in sample/*/sql/)
+-- docs/                  Full configuration reference and decision records
+-- scripts/               SQL / config documentation validation scripts
+-- openspec/              Runtime specs and change records (maintainers)
```

## Quick start (5 minutes)

### Path 1: H2 sample, no external dependencies

Start with the RBAC and meeting-room permission case:

```bash
git clone https://gitee.com/LiuKewenSc/kewen-framework.git
cd kewen-framework
mvn clean install -DskipTests

cd sample/auth-rbac-boot-sample
java -jar target/auth-rbac-boot-sample-0.0.1-exec.jar
```

> Start this sample from the packaged exec jar. `mvn spring-boot:run` may fail to scan the permission entities in this project layout and report `权限标记错误: ROLE` (invalid permission marker).

In another terminal:

```bash
curl -H "X-Demo-User: admin" "http://localhost:8081/meetingRoom/page?page=1&size=10"
```

- In-memory H2 by default, port 8081; H2 console at <http://localhost:8081/h2-console>.
- The identity is declared by `X-Demo-User` and falls back to `admin` when absent; switch to `admin2` / `subordinate` / `outsider` to observe the differences in department inheritance.
- The RBAC sample has no login page; see `sample/login-boot-sample` for username/password and SSO, and `sample/auth-rbac-security-boot-sample` for login identity + RBAC integration.

Login and single sign-on sample:

```bash
cd sample/login-boot-sample
mvn spring-boot:run
```

Open <http://localhost:8080/login.html>; the accounts are `admin/admin123` and `demo/demo123`. The default configuration contains one OIDC client and contacts a local IDP (port 9010) at startup; comment out the `demo-oidc` entry if you don't have an IDP. Most samples use port 8080, so do not run several of them at the same time.

### Path 2: Integrate into your own MySQL project

The full RBAC + Spring Security login combination:

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

1. Run [relation/auth-rbac/auth-rbac.sql](relation/auth-rbac/auth-rbac.sql) to create the permission tables.
2. If you persist sessions in MySQL, run [relation/login-security/spring-session-mysql.sql](relation/login-security/spring-session-mysql.sql) and set `spring.session.jdbc.initialize-schema: never`.
3. Configure the data source and `cu.security.current-user-url` (defaults to `/currentUser`), and prepare user data such as `sys_user` / `sys_user_credential`.
4. After startup, log in with `POST /login` (JSON) and fetch the current user with `GET /currentUser`.

See the "5 分钟接入" (5-minute integration) section in [auth/README.md](auth/README.md) for finer-grained dependency combinations, required beans and verification steps.

## Documentation map

| I want to… | Read… |
| --- | --- |
| Run a sample first | [auth-rbac sample](sample/auth-rbac-boot-sample/README.md) / [login sample](sample/login-boot-sample/README.md) |
| Learn how to use the permission annotations | [Permission annotations and permission editing](auth/docs/annotations.md) |
| See the RBAC tables, permission strings, department inheritance and extension points | [RBAC model and extension](auth/docs/rbac-and-extension.md) |
| Add login / single sign-on | [login starter](boot/login-security-spring-boot-starter/README.md) and [SSO overview](boot/login-security-spring-boot-starter/docs/sso-overview.md) |
| Look up OAuth2 / OIDC / SAML2 details | [OAuth2](boot/login-security-spring-boot-starter/docs/oauth2.md) / [OIDC](boot/login-security-spring-boot-starter/docs/oidc.md) / [SAML2](boot/login-security-spring-boot-starter/docs/saml2.md) |
| Find every configuration key | [docs/application.yml](docs/application.yml) and the starter READMEs |
| Find SQL scripts | [relation/README.md](relation/README.md) |
| See the code generator | [other/code-generator](other/code-generator/README.md) |
| Run maintenance scripts | [scripts/README.md](scripts/README.md) |
| See past decisions and why implementations were abandoned | [docs/decisions](docs/decisions/已废弃实现与决策记录.md) |
| Read the runtime specs (maintainers) | [openspec/specs](openspec/specs) |

## Contributing

1. Fork this repository and create a feature branch.
2. Before submitting, run at least:
   ```bash
   mvn clean install -DskipTests
   bash scripts/check-sql-compat.sh
   bash scripts/check-config-docs.sh   # run `mvn -q -DskipTests compile` first
   ```
3. Keep commit messages in the `feat(scope): ...`, `fix(scope): ...`, `docs(scope): ...` style.
4. Open a pull request describing the purpose, the impact and how you verified the change.

Issue / PR templates live under `.gitee/`. Please do not report security problems in public issues; contact the maintainer privately.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).

## Related resources

- Gitee repository: <https://gitee.com/LiuKewenSc/kewen-framework>
- Frontend template: <https://gitee.com/LiuKewenSc/kewen-web-admin>
- Author: kewen
