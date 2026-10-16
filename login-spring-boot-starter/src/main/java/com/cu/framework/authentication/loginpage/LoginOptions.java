package com.cu.framework.authentication.loginpage;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 登录方式清单，供前端登录页按条渲染登录入口。
 * <p>
 * 只包含发起登录所需的最小信息：协议类别、注册标识、展示名、发起地址。
 * 本对象会被未经认证的请求读到，因此绝不承载客户端密钥、密钥库口令、证书或资源路径等机密。
 *
 * @author kewen
 * @since 2026-10-16
 */
@Data
public class LoginOptions {

    /**
     * 账号密码登录所需的提交信息，取自 cu.security.login.password
     */
    private Password password = new Password();

    /**
     * 单点登录入口列表，条数等于可正常发起登录的身份源配置条数；没有身份源时为空列表
     */
    private List<Sso> sso = new ArrayList<>();

    /**
     * 账号密码登录的提交地址与字段名
     *
     * @author kewen
     * @since 2026-10-16
     */
    @Data
    public static class Password {
        /**
         * 提交地址，取自 cu.security.login.password.login-url
         */
        private String loginUrl;
        /**
         * 用户名的参数名，取自 cu.security.login.password.username-parameter
         */
        private String usernameParameter;
        /**
         * 密码的参数名，取自 cu.security.login.password.password-parameter
         */
        private String passwordParameter;
    }

    /**
     * 一条单点登录入口
     *
     * @author kewen
     * @since 2026-10-16
     */
    @Data
    public static class Sso {
        /**
         * 协议类别，取值 oauth2 | oidc | saml，由配置直接决定而非推断
         */
        private String protocol;
        /**
         * 身份源注册标识
         */
        private String registrationId;
        /**
         * 前端展示名，恒不为 null 与空串；未配置展示用途名称时等于 registrationId
         */
        private String displayName;
        /**
         * 发起登录的地址，前端直接用于顶层导航，无需自行拼接注册标识
         */
        private String loginUrl;
    }
}
