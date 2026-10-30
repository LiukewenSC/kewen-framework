package com.cu.framework.authentication.handler;

import com.cu.framework.authentication.exception.NoLoginException;
import com.cu.framework.basic.model.Result;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 登录安全异常出口的分类、文案与 HTTP 状态契约
 *
 * @author kewen
 * @since 2026-10-30
 */
public class AuthenticationExceptionAdviceHandlerTest {

    private final AuthenticationExceptionAdviceHandler advice = new AuthenticationExceptionAdviceHandler();

    @Test
    public void unauthenticatedRequestsAreAuthenticationFailures() {
        Result<Void> anonymous = advice.insufficientAuthenticationException(
                new InsufficientAuthenticationException("Full authentication is required to access this resource"), null);
        Result<Void> noLogin = advice.noLoginException(new NoLoginException("未登录"), null);

        Assert.assertEquals(Integer.valueOf(401), anonymous.getCode());
        Assert.assertEquals("AUTHENTICATION", anonymous.getErrorType());
        Assert.assertEquals("未登录或登录状态已失效，请重新登录", anonymous.getMessage());
        Assert.assertFalse(anonymous.getMessage().contains("权限不足"));
        Assert.assertEquals("AUTHENTICATION", noLogin.getErrorType());
        Assert.assertEquals("未登录或登录状态已失效，请重新登录", noLogin.getMessage());
    }

    @Test
    public void badCredentialsUseGenericMessage() {
        Result<Void> result = advice.authenticationException(new BadCredentialsException("Bad credentials"), null);

        Assert.assertEquals(Integer.valueOf(401), result.getCode());
        Assert.assertEquals("AUTHENTICATION", result.getErrorType());
        Assert.assertEquals("用户名或密码错误", result.getMessage());
    }

    @Test
    public void accountStateFailuresHaveDistinctMessages() {
        Assert.assertEquals("账号已被禁用", advice.authenticationException(new DisabledException("disabled"), null).getMessage());
        Assert.assertEquals("账号已被锁定", advice.authenticationException(new LockedException("locked"), null).getMessage());
        Assert.assertEquals("账号已过期", advice.authenticationException(new AccountExpiredException("expired"), null).getMessage());
        Assert.assertEquals("登录凭证已过期", advice.authenticationException(new CredentialsExpiredException("expired"), null).getMessage());
        Assert.assertEquals("认证失败，请重试", advice.authenticationException(new AuthenticationException("other") {
        }, null).getMessage());
    }

    @Test
    public void authenticationServiceUsageErrorMapsToUsage() {
        Result<Void> result = advice.authenticationServiceException(
                new AuthenticationServiceException("Authentication method not supported: GET"), null);

        Assert.assertEquals(Integer.valueOf(400), result.getCode());
        Assert.assertEquals("USAGE", result.getErrorType());
        Assert.assertEquals("Authentication method not supported: GET", result.getMessage());
    }

    @Test
    public void accessDeniedMapsToAuthorization() {
        Result<Void> result = advice.accessDeniedException(new AccessDeniedException("Access Denied"), null);

        Assert.assertEquals(Integer.valueOf(403), result.getCode());
        Assert.assertEquals("AUTHORIZATION", result.getErrorType());
        Assert.assertEquals("没有访问该资源的权限", result.getMessage());
    }

    @Test
    public void internalAuthenticationFailureSplitsDatabaseFromSystem() {
        Result<Void> database = advice.internalAuthenticationServiceException(
                new InternalAuthenticationServiceException("query failed", new DataAccessResourceFailureException("db down")), null);
        Result<Void> system = advice.internalAuthenticationServiceException(
                new InternalAuthenticationServiceException("principal broken", new IllegalStateException("boom")), null);

        Assert.assertEquals(Integer.valueOf(500), database.getCode());
        Assert.assertEquals("DATABASE", database.getErrorType());
        Assert.assertEquals("数据库操作失败，请稍后重试", database.getMessage());
        Assert.assertEquals(Integer.valueOf(500), system.getCode());
        Assert.assertEquals("SYSTEM", system.getErrorType());
        Assert.assertEquals("认证服务内部错误，请稍后重试", system.getMessage());
    }

    @Test
    public void handlersDeclareMatchingHttpStatus() throws Exception {
        assertStatus("noLoginException", HttpStatus.UNAUTHORIZED, NoLoginException.class);
        assertStatus("insufficientAuthenticationException", HttpStatus.UNAUTHORIZED, InsufficientAuthenticationException.class);
        assertStatus("authenticationException", HttpStatus.UNAUTHORIZED, AuthenticationException.class);
        assertStatus("authenticationServiceException", HttpStatus.BAD_REQUEST, AuthenticationServiceException.class);
        assertStatus("accessDeniedException", HttpStatus.FORBIDDEN, AccessDeniedException.class);
        assertStatus("internalAuthenticationServiceException", HttpStatus.INTERNAL_SERVER_ERROR,
                InternalAuthenticationServiceException.class);
    }

    private void assertStatus(String methodName, HttpStatus expected, Class<? extends Exception> exceptionType) throws Exception {
        Method method = AuthenticationExceptionAdviceHandler.class.getMethod(
                methodName, exceptionType, HttpServletRequest.class);
        ResponseStatus status = method.getAnnotation(ResponseStatus.class);
        Assert.assertNotNull(methodName + " 缺少 @ResponseStatus", status);
        Assert.assertEquals(methodName + " 的 HTTP 状态不符合分类映射", expected, status.value());
    }
}
