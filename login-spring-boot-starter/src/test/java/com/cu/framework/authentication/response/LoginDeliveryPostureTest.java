package com.cu.framework.authentication.response;

import com.cu.framework.authentication.model.SecurityUser;
import com.cu.framework.authentication.model.SecurityUserAuthority;
import com.cu.framework.authentication.oauth2.Oauth2OidcProperties;
import com.cu.framework.authentication.properties.SsoLoginProperties;
import com.cu.framework.authentication.saml.properties.SamlProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Collections;

/**
 * 认证结果交付姿态的分流正确性：三类单点登录按请求命中的处理端点改为重定向，
 * 账号密码端点与一切非认证端点保持原有 JSON 契约
 *
 * @author kewen
 * @since 2026-10-16
 */
public class LoginDeliveryPostureTest {

    /** OAuth2 / OIDC 回调地址，与 Oauth2OidcProperties 的默认 loginProcessingUrl 模板相匹配 */
    private static final String OAUTH2_CALLBACK = "/login/oauth2/code/demo-oauth2";

    /** SAML ACS 地址，与 SamlProperties 的默认 loginProcessingUrl 模板相匹配 */
    private static final String SAML_ACS = "/login/saml2/sso/demo-saml";

    private LoginDeliveryResolver resolverOf(String resultUrl) {
        SsoLoginProperties sso = new SsoLoginProperties();
        sso.setResultUrl(resultUrl);
        return new LoginDeliveryResolver(sso, new Oauth2OidcProperties(), new SamlProperties());
    }

    private MockHttpServletRequest requestOf(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRequestURI(uri);
        return request;
    }

    /**
     * 认证成功处理器收到的认证对象：账号密码与三种单点登录都交出 SecurityUser 主体，
     * 本测试只关心交付姿态，用它代表任意一种
     */
    private UsernamePasswordAuthenticationToken authenticatedToken() {
        SecurityUser securityUser = new SecurityUser()
                .setId(1L)
                .setUsername("admin")
                .setName("管理员")
                .setEnabled(true)
                .setUserAuthorities(Collections.singletonList(new SecurityUserAuthority("ROLE_ADMIN", null)));
        return new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
    }

    /**
     * 未配置结果页地址时，即使是单点登录的回调请求也不重定向
     */
    @Test
    public void noRedirectWhenTargetNotConfigured() {
        Assert.assertFalse(resolverOf(null).shouldRedirect(requestOf("GET", OAUTH2_CALLBACK)));
        Assert.assertFalse(resolverOf("   ").shouldRedirect(requestOf("POST", SAML_ACS)));
    }

    /**
     * 三类协议共用同一个结果页地址，oauth2 / oidc（同一条处理端点）与 saml 都命中
     */
    @Test
    public void singleSignOnProcessingEndpointsRedirect() {
        LoginDeliveryResolver resolver = resolverOf("/login.html");
        Assert.assertTrue(resolver.shouldRedirect(requestOf("GET", OAUTH2_CALLBACK)));
        Assert.assertTrue(resolver.shouldRedirect(requestOf("POST", SAML_ACS)));
        Assert.assertFalse("回调端点的前缀本身不该被当成 SSO 请求",
                resolver.shouldRedirect(requestOf("GET", "/login/oauth2/code")));
    }

    /**
     * 账号密码端点与业务请求恒为 JSON，这是"账密永远返回 JSON"的机制保障
     */
    @Test
    public void passwordEndpointAndBusinessRequestsStayJson() {
        LoginDeliveryResolver resolver = resolverOf("/login.html");
        Assert.assertFalse("账号密码 JSON 登录端点不得重定向",
                resolver.shouldRedirect(requestOf("POST", "/login")));
        Assert.assertFalse("GET 方式的同名端点也不重定向",
                resolver.shouldRedirect(requestOf("GET", "/login")));
        Assert.assertFalse("登录方式发现接口不受影响",
                resolver.shouldRedirect(requestOf("GET", "/api/login/options")));
        Assert.assertFalse("登出端点不重定向",
                resolver.shouldRedirect(requestOf("POST", "/logout")));
        Assert.assertFalse(resolver.shouldRedirect(requestOf("GET", "/")));
    }

    /**
     * 状态标记的参数名固定、取值二选一，且地址自带 query 时改用 & 追加
     */
    @Test
    public void stateMarkerAppendedWithCorrectSeparator() {
        Assert.assertEquals("/login.html?loginState=success",
                LoginDeliveryResolver.buildRedirectUrl("/login.html", LoginDeliveryResolver.STATE_SUCCESS));
        Assert.assertEquals("/login.html?loginState=failure",
                LoginDeliveryResolver.buildRedirectUrl("/login.html", LoginDeliveryResolver.STATE_FAILURE));
        Assert.assertEquals("/login.html?tab=sso&loginState=success",
                LoginDeliveryResolver.buildRedirectUrl("/login.html?tab=sso", LoginDeliveryResolver.STATE_SUCCESS));
        Assert.assertEquals("http://front.example.com/cb?loginState=failure",
                LoginDeliveryResolver.buildRedirectUrl("http://front.example.com/cb", LoginDeliveryResolver.STATE_FAILURE));
    }

    /**
     * 单点登录成功改为 302，响应体为空，且消费方的结果解析器不被调用
     */
    @Test
    public void successRedirectsAndSkipsResultResolver() throws Exception {
        CountingResultResolver resultResolver = new CountingResultResolver();
        JsonAuthenticationSuccessHandler handler = new JsonAuthenticationSuccessHandler(
                resultResolver, new ObjectMapper(), resolverOf("/login.html"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(requestOf("GET", OAUTH2_CALLBACK), response, authenticatedToken());

        Assert.assertEquals("/login.html?loginState=success", response.getRedirectedUrl());
        Assert.assertEquals(302, response.getStatus());
        Assert.assertEquals("重定向响应体不得携带业务数据", "", response.getContentAsString());
        Assert.assertEquals("跳转场景不应触发结果解析器", 0, resultResolver.invocations);
    }

    /**
     * 账号密码登录仍然写 JSON，不产生任何重定向
     */
    @Test
    public void passwordLoginStillWritesJson() throws Exception {
        CountingResultResolver resultResolver = new CountingResultResolver();
        JsonAuthenticationSuccessHandler handler = new JsonAuthenticationSuccessHandler(
                resultResolver, new ObjectMapper(), resolverOf("/login.html"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(requestOf("POST", "/login"), response, authenticatedToken());

        Assert.assertNull(response.getRedirectedUrl());
        Assert.assertEquals(1, resultResolver.invocations);
        Assert.assertTrue(response.getContentAsString().contains("loginResult"));
    }

    /**
     * 未配置回跳目标时，单点登录成功仍然写 JSON，行为与该属性引入前一致
     */
    @Test
    public void singleSignOnStaysJsonWhenTargetAbsent() throws Exception {
        JsonAuthenticationSuccessHandler handler = new JsonAuthenticationSuccessHandler(
                new CountingResultResolver(), new ObjectMapper(), resolverOf(null));

        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(requestOf("GET", OAUTH2_CALLBACK), response, authenticatedToken());

        Assert.assertNull(response.getRedirectedUrl());
        Assert.assertTrue(response.getContentAsString().contains("loginResult"));
    }

    /**
     * 单点登录回调上的认证失败改为 302 带失败标记，且不再进入 JSON 异常解析链
     */
    @Test
    public void failureOnSingleSignOnCallbackRedirects() throws Exception {
        RecordingHandlerExceptionResolver advice = new RecordingHandlerExceptionResolver();
        SecurityAuthenticationExceptionResolverHandler handler =
                new SecurityAuthenticationExceptionResolverHandler()
                        .setHandlerExceptionResolvers(Collections.singletonList(advice))
                        .setLoginDeliveryResolver(resolverOf("/login.html"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationFailure(requestOf("GET", OAUTH2_CALLBACK), response,
                new BadCredentialsException("invalid state"));

        Assert.assertEquals("/login.html?loginState=failure", response.getRedirectedUrl());
        Assert.assertEquals(302, response.getStatus());
        Assert.assertEquals("重定向时不应再走 JSON 异常解析链", 0, advice.invocations);
    }

    /**
     * 账号密码失败仍交给既有的异常解析器处理，未因姿态分流而改变
     */
    @Test
    public void passwordFailureKeepsExistingJsonContract() throws Exception {
        RecordingHandlerExceptionResolver advice = new RecordingHandlerExceptionResolver();
        SecurityAuthenticationExceptionResolverHandler handler =
                new SecurityAuthenticationExceptionResolverHandler()
                        .setHandlerExceptionResolvers(Collections.singletonList(advice))
                        .setLoginDeliveryResolver(resolverOf("/login.html"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationFailure(requestOf("POST", "/login"), response,
                new BadCredentialsException("bad credentials"));

        Assert.assertNull(response.getRedirectedUrl());
        Assert.assertEquals(1, advice.invocations);

        MockHttpServletResponse unauthenticatedProbe = new MockHttpServletResponse();
        handler.commence(requestOf("GET", "/"), unauthenticatedProbe,
                new BadCredentialsException("full authentication required"));
        Assert.assertNull("非认证端点的未登录请求不得被重定向", unauthenticatedProbe.getRedirectedUrl());
        Assert.assertEquals("未登录请求仍应交给既有的 JSON 异常解析链", 2, advice.invocations);
    }

    /**
     * 交出的结果对象，用于断言解析器是否被触发
     */
    private static class CountingResultResolver implements AuthenticationSuccessResultResolver {
        int invocations;

        @Override
        public Object resolver(javax.servlet.http.HttpServletRequest request,
                               javax.servlet.http.HttpServletResponse response,
                               Object data) {
            invocations++;
            return Collections.singletonMap("loginResult", "ok");
        }
    }

    /**
     * 记录被调用次数的异常解析器替身
     */
    private static class RecordingHandlerExceptionResolver implements HandlerExceptionResolver {
        int invocations;

        @Override
        public ModelAndView resolveException(javax.servlet.http.HttpServletRequest request,
                                             javax.servlet.http.HttpServletResponse response,
                                             Object handler, Exception ex) {
            invocations++;
            return new ModelAndView();
        }
    }
}
