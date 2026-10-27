# login-boot-sample

## 演示什么

`login-security-spring-boot-starter` 的完整登录页示例：账密 JSON 登录、OAuth2 / OIDC / SAML 单点登录入口发现、登录结果回跳与会话保持。默认 H2 内存库，端口 8080。

示例用户是内存实现，不入库：

- `admin / admin123`（`ROLE_ADMIN`、`ROLE_USER`）
- `demo / demo123`（`ROLE_USER`）

## 零配置启动

```bash
cd sample/login-boot-sample
mvn spring-boot:run
```

打开 <http://localhost:8080/login.html>。

> 默认配置里有一条 `protocol-type: oidc` 客户端，应用启动时会访问 `http://localhost:9010/.well-known/openid-configuration`。本地 IDP（端口 9010）不在线时应用起不来；只跑账密登录时，先把 `demo-oidc` 那条客户端注释掉，OAuth2 / SAML 不会在启动期发网络请求。

## 打开什么

| 地址 | 用途 |
| --- | --- |
| `/login.html` | 登录页：账密表单 + 按配置渲染的单点登录按钮 |
| `/api/login/options` | 未认证可访问的登录方式清单 |
| `/api/me` | 当前会话用户与权限；未登录时返回未认证语义 |
| `/` 或 `/index.html` | 登录后的首页 |
| `/logout` | POST 登出 |

## 验证清单

```bash
# 1. 登录方式清单
curl http://localhost:8080/api/login/options

# 2. 账密登录
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' --cookie-jar cookie.txt

# 3. 当前用户
curl http://localhost:8080/api/me --cookie cookie.txt

# 4. 登出
curl -X POST http://localhost:8080/logout --cookie cookie.txt
```

- `Content-Type` 必须是精确的 `application/json`；带 charset 参数会被当成表单，登录失败。
- 登录成功后 `/api/me` 返回用户名、展示名与权限列表。
- `result-url: /login.html` 时，单点登录成功/失败都 302 回登录页并带 `loginState=success|failure`。
- 单点登录按钮来自配置，不在页面里写死；没有配置身份源时显示“当前没有配置单点登录身份源”。

## 不覆盖什么

- 不引入 RBAC：用户与权限是内存桩，`/currentUser` 没有消费方；当前用户用示例自己的 `/api/me`。
- `remember-me` 配置存在但当前未接入过滤器链，示例不演示“记住我”。
- OAuth2 / OIDC / SAML 的完整联调需要本地 IDP（端口 9010）；示例不包含 IDP 本身。
- 不演示分布式会话部署；MySQL 会话表脚本见 [relation/login-security/spring-session-mysql.sql](../../relation/login-security/spring-session-mysql.sql)。

## 相关文档

- [login-security-spring-boot-starter](../../boot/login-security-spring-boot-starter/README.md)
- [单点登录总览](../../boot/login-security-spring-boot-starter/docs/sso-overview.md)
- [OAuth2](../../boot/login-security-spring-boot-starter/docs/oauth2.md)
- [OIDC](../../boot/login-security-spring-boot-starter/docs/oidc.md)
- [SAML2](../../boot/login-security-spring-boot-starter/docs/saml2.md)
- [项目根 README](../../README.md)
