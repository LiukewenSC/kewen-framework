# SP 签名密钥库（sp-keystore.jks）说明

## 目前是否有这个文件？

**没有。** 仓库里不存在任何 `.jks` 文件，根目录 `.gitignore` 已将 `*.jks` / `*.p12` / `*.keystore` 列为敏感文件排除，**永远不会提交**。每个人需要在本地自行生成。

## 在什么情况下需要它？

SAML 中 SP 发起的 `AuthnRequest` / `LogoutRequest` 是否需要签名，由 **IdP 元数据里的 `WantAuthnRequestsSigned` 属性**决定（`SamlConfig` 中透传给 registration）：

| 场景 | 是否需要 keystore |
| --- | --- |
| `WantAuthnRequestsSigned="false"`（如 `login-boot-sample` 的 demo 元数据、idaas-idp4 各环境元数据） | 不需要，`keystore-resource` 留空即可跑通 |
| `WantAuthnRequestsSigned="true"`（如本 sample 的 `cu-local-metadata.xml`） | **需要**，否则 AuthnRequest 不签名，会被 IdP 拒绝 |

本 sample（`idaas-sp-boot-sample`）的 `application.yml` 中配置了：

```yaml
cu.security.login.saml.sp-signing-keystore:
  keystore-resource: classpath:saml/sp-keystore.jks
```

文件缺失时**不会启动报错**：`SamlConfig.loadSpSigningCredential()` 会打印警告 `SP 签名密钥库不存在: ...` 然后降级为不签名。只有访问 `WantAuthnRequestsSigned="true"` 的 IdP（例如 `cu-local`）时才会在登录环节失败。

> 注意：如果文件存在但密码错误/别名不对，会直接抛 `IllegalStateException` 导致启动失败。

## 怎么生成？

### 1. 生成 JKS 密钥库（私钥 + 自签证书）

别名、密码必须与 `application.yml` 中 `sp-signing-keystore` 段的配置一致（默认 `sp-signing` / `changeit`）：

```bash
keytool -genkeypair \
  -alias sp-signing \
  -keyalg RSA \
  -keysize 2048 \
  -validity 3650 \
  -keystore sp-keystore.jks \
  -storepass changeit \
  -keypass changeit
```

### 2. 导出证书给 IdP

```bash
keytool -exportcert \
  -alias sp-signing \
  -keystore sp-keystore.jks \
  -storepass changeit \
  -file sp-certificate.crt
```

把导出的 `sp-certificate.crt` 提供给 IdP 管理员，用于验证 SP 发起请求的签名。

### 3. 放置位置

生成的 `sp-keystore.jks` 放到本目录的**上一级** `src/main/resources/saml/` 下（与 yml 中 `classpath:saml/sp-keystore.jks` 对应），不要提交到 Git。

## 验证是否生效

启动时若不再出现 `SP 签名密钥库不存在` 警告，且访问 `/saml2/authenticate/{registrationId}` 重定向的 SAMLRequest 中带有 `<ds:Signature>` 节点，即签名生效。排查时可开启：

```yaml
logging:
  level:
    org.springframework.security.saml2: DEBUG
```
