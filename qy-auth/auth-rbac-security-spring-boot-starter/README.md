# 说明

当前登录用户上下文由 `RbacSecurityConfig` 注册：`RbacSecurityAuthCurrentUserContextHolder` 从 Spring Security 的登录态取用户名，再按用户名实时装载权限集合。使用方自己声明 `AuthCurrentUserContextHolder` 或 `SecurityUserDetailsService` 时，对应的自动注册会让位。

`PasswordEncoder` 仍需自行注册，修改密码要用。
