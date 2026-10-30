# auth-rbac-security-spring-boot-starter

## 这是什么

把 RBAC 接到 Spring Security 登录态：登录成功后用 `RbacSecurityUserDetailsService` 装载用户与权限，把登录人放进 `SecurityUserContext`，并提供 `/currentUser` 端点。它把 `auth-rbac-spring-boot-starter` 与 `login-security-spring-boot-starter` 一起带上。

## 何时使用

- 需要账密登录或 OAuth2 / OIDC / SAML 单点登录，并接受默认 RBAC 表结构。
- 想用 Spring Security 的会话、并发控制、统一登录/登出处理，同时保留 RBAC 权限。
- 不适用：已有自己的登录态、只想用 RBAC 权限时，用 `auth-rbac-spring-boot-starter` 并自己提供 `AuthCurrentUserContextHolder`。

## 5 分钟接入

**依赖**：本 starter 传递 `auth-rbac-spring-boot-starter` 与 `login-security-spring-boot-starter`；再加 `basic-spring-boot-starter` 与数据库驱动。

```xml
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

**建表**：

- 权限表：[relation/auth-rbac/auth-rbac.sql](../../relation/auth-rbac/auth-rbac.sql)。
- MySQL 会话表：[relation/login-security/spring-session-mysql.sql](../../relation/login-security/spring-session-mysql.sql)；H2 用 `spring-session-jdbc` jar 内的 schema。

**准备登录用户**（仅本地演示，密码直接写 `{noop}` 前缀）：

```sql
INSERT INTO sys_user (id, name, nick_name, username, gender)
VALUES (1, '管理员', '管理员', 'admin', 1);

INSERT INTO sys_user_credential (user_id, password, enabled)
VALUES (1, '{noop}admin123', 1);
```

生产环境请使用 `{bcrypt}` 等强编码；默认 `PasswordEncoder` 是 `DelegatingPasswordEncoder`，库里的密码必须带编码前缀，或自行声明匹配的 `PasswordEncoder`。

**最小配置**：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  session:
    jdbc:
      initialize-schema: never   # 已手工执行会话表脚本；H2 用 embedded

cu:
  security:
    current-user-url: /currentUser
```

**验证**：

```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' --cookie-jar cookie.txt

curl http://localhost:8080/currentUser --cookie cookie.txt
```

登录成功返回 JSON 并带回会话 Cookie；`/currentUser` 返回当前用户，未登录时由安全异常 advice 返回未登录语义。

## 自动装配

| 配置类 | 生效条件 | 产出 |
| --- | --- | --- |
| `RbacSecurityConfig` | 无 | `RbacSecurityUserDetailsService`（使用方已有 `SecurityUserDetailsService` 时让位）；`RbacSecurityAuthCurrentUserContextHolder`（使用方已有 `AuthCurrentUserContextHolder` 时让位）；`RbacCurrentUserEndpoint`（使用方已有同类型端点时让位） |

三个默认实现都带 `@ConditionalOnMissingBean`：使用方声明同类型 Bean 时框架让位，不会出现两个候选。

## 配置项

无自有配置键。读取登录 starter 的 `cu.security.*`：

- `cu.security.current-user-url`：当前用户接口路径，默认 `/currentUser`。
- `cu.security.session.*`：最大会话数、是否阻止超限登录。
- `cu.security.login.password.*`：账密登录地址与参数名。
- `cu.security.login.oauth2.*` / `cu.security.login.saml.*`：需要单点登录时配置。

完整键表见 [login-security-spring-boot-starter/README.md](../../boot/login-security-spring-boot-starter/README.md) 与 [docs/application.yml](../../docs/application.yml)。

## 前置条件与已知空转项

- 依赖 `auth-rbac-spring-boot-starter` 与 `login-security-spring-boot-starter`。
- 需要 `SysUserComposite` 能按用户名返回 `UserAuthObject`；默认实现从 `sys_user*` 装载。
- 读取 `cu.security.current-user-url` 的 `RbacCurrentUserEndpoint` 只返回身份信息，不含密码。
- `RbacCurrentUserController`（来自 `auth-rbac`）另外提供 `/currentUser/routeMenus` 与 `/currentUser/updatePassword`。
- 当前用户与权限在登录时装载为会话快照；改权限或资料后需要重新登录才生效。
- 密码编码器由登录 starter 提供；库中密码不是带前缀的编码格式时自行声明 `PasswordEncoder`。
- 使用 MySQL 会话时，启动前执行 `relation/login-security/spring-session-mysql.sql`，并把 `spring.session.jdbc.initialize-schema` 设为 `never`。
- 融合示例见 [sample/auth-rbac-security-boot-sample](../../sample/auth-rbac-security-boot-sample/README.md)：登录身份由 RBAC 装载、`/currentUser` 由本 starter 提供，并带一张四身份 × 六端点的判权矩阵与 `verify.sh`。

## 相关文档

- [auth 总览](../README.md)
- [RBAC 模型与扩展](../docs/rbac-and-extension.md)
- [权限注解与权限编辑](../docs/annotations.md)
- [auth-rbac-spring-boot-starter](../auth-rbac-spring-boot-starter/README.md)
- [登录与单点登录](../../boot/login-security-spring-boot-starter/README.md)
- [全量配置参考](../../docs/application.yml)
