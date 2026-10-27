# auth-rbac-security-spring-boot-starter

把 RBAC 接到 Spring Security 登录态，提供当前登录人上下文与 `UserDetailsService` 的默认实现。

## 自动装配

| 配置类 | 生效条件 | 产出 |
| --- | --- | --- |
| `RbacSecurityConfig` | 无 | `RbacSecurityUserDetailsService`（容器中已有 `SecurityUserDetailsService` 时让位）；`RbacSecurityAuthCurrentUserContextHolder`（无条件注册）；`RbacCurrentUserEndpoint`（容器中已有同类型端点时让位） |

## 配置项

无自有配置键。本 starter 读取 `login-spring-boot-starter` 的 `cu.security.*`，其中 `cu.security.current-user-url` 由 `RbacCurrentUserEndpoint` 读取（默认 `/currentUser`），返回当前用户身份信息且不包含密码。

## 启动前置条件与已知空转项

- 依赖 `auth-rbac-spring-boot-starter` 与 `login-spring-boot-starter`。
- `SecurityUserDetailsService`：使用方自己声明同类型 Bean 时，本 starter 的默认实现让位。
- `AuthCurrentUserContextHolder`：本 starter **无条件注册**。使用方再声明同类型 Bean 会出现两个候选，需要自行加 `@Primary`，或不引入本 starter。
- `PasswordEncoder`：`login-spring-boot-starter` 会提供默认的 `DelegatingPasswordEncoder`；若库内密码不是带 `{bcrypt}` 等前缀的编码格式，需自行声明匹配的编码器（示例工程使用 `NoOpPasswordEncoder`）。
- 当前用户与权限按用户名实时装载；`AuthCurrentUserContextHolder` 的权限集合来自 Spring Security 登录态，不额外查库两次。

## 完整示例

见 [docs/application.yml](../../docs/application.yml) 的 `login-spring-boot-starter` 分节。
