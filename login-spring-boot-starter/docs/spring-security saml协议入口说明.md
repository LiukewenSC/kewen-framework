
## SAML SSO 入口一览

`SamlConfig.customizer()` 里通过 `http.saml2Login()` 注册的过滤器链沿用了 Spring Security 5.3 的默认路径，只有 **ACS 回调地址** 被 `login-processing-url` 覆盖，其他都是固定的。

| 用途 | HTTP 方法 | 路径 | 说明 |
|---|---|---|---|
| **SP 发起登录（入口）** | GET | `/saml2/authenticate/{registrationId}` | Spring Security 默认，**你在找的就是这个**，浏览器访问后会 302 跳到 IdP 的 SSO URL |
| ACS 登录回调 | POST | `{login-processing-url}` 展开后的地址 | IdP 把 SAMLResponse POST 回来的地址，可在 `cu.security.login.saml.login-processing-url` 配置 |
| SP 元数据 | GET | `/saml2/service-provider-metadata/{registrationId}` | 由 `Saml2MetadataFilter` 提供，给 IdP 侧导入用 |
| GET 触发退出 | GET | `/saml2/logout` | `SamlLogoutController` 提供的辅助页，返回自动提交的 POST 表单 |
| 实际登出 | POST | `/logout` | Spring Security 标准登出，会串起 SAML2 SLO |
| SLO 请求/响应 | POST | `/logout/saml2/slo` | 由 `logout-request-url` / `logout-response-url` 配置 |


 `sample/login-boot-sample`（端口 8080，registration-id = `demo-saml`）

如配置：

```yaml
cu:
  security:
    login:
      saml:
        # SP 侧 ACS（登录回调）地址模板
        login-processing-url: /login/saml2/sso/{registrationId}
        logout-request-url: /logout/saml2/slo
        logout-response-url: /logout/saml2/slo
        # 同样无默认值且必须至少配一个：SamlConfig 里直接 registrations.stream()，留空会启动失败
        registrations:
          - registration-id: demo-saml            # 留空时 getter 回退返回 sp-entity-id
            sp-entity-id: cu-framework-local      # 需与 IDP 侧配置的 SP Audience 一致
            metadata-resource: "classpath:saml/idp-metadata.xml"   # 会解析出 entityId / SSO URL / 验签证书
            #登录入口：http://localhost:8080/saml2/authenticate/demo-saml
            #ACS 回调：http://localhost:8080/login/saml2/sso/demo-saml
            #SP 元数据：http://localhost:8080/saml2/service-provider-metadata/demo-saml
            #退出：http://localhost:8080/saml2/logout
        # SP 签名密钥库：不配则 AuthnRequest / LogoutRequest 不签名。
        # demo 的 idp-metadata.xml 里 WantAuthnRequestsSigned=false，所以留空即可跑通
```


- **登录入口**：http://localhost:8080/saml2/authenticate/demo-saml
- ACS 回调：http://localhost:8080/login/saml2/sso/demo-saml
- SP 元数据：http://localhost:8080/saml2/service-provider-metadata/demo-saml
- 退出：http://localhost:8080/saml2/logout

例如：http://localhost:8080/saml2/authenticate/cu-local-registration

## 提示
- 访问入口前确保 `login-boot-sample` 已启用 SAML（`application.yml` 里 `cu.security.login.saml.registrations` 至少一条），否则 `SamlConfig` 启动就会 NPE。
- demo 用的 `idp-metadata.xml` 里 SSO 地址是占位的 `http://localhost:9010/saml/sso`，要跑通完整流程记得把 IdP 起在 9010，或整份替换 metadata。