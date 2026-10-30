package com.cu.framework.auth.rbac.security;

import com.cu.framework.auth.core.exception.AuthException;
import com.cu.framework.auth.core.exception.AuthServiceException;
import com.cu.framework.authentication.exception.NoLoginException;
import com.cu.framework.authentication.handler.AuthenticationExceptionAdviceHandler;
import com.cu.framework.basic.exception.BizException;
import com.cu.framework.basic.exception.GlobalExceptionHandlerAdvice;
import com.cu.framework.basic.model.Result;
import com.cu.framework.boot.auth.core.AuthExceptionHandler;
import org.junit.Test;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 认证/权限异常 advice 的优先级与返回码契约：
 * 具体异常必须先于通用 Throwable 命中，权限、认证、基础异常各自保留 403/401/500 语义。
 *
 * @author kewen
 * @since 2026-10-27
 */
public class AdviceOrderTest {

    @Test
    public void specificAdviceRunsBeforeGenericAdvice() {
        int authOrder = orderOf(AuthExceptionHandler.class);
        int loginOrder = orderOf(AuthenticationExceptionAdviceHandler.class);
        int basicOrder = orderOf(GlobalExceptionHandlerAdvice.class);

        assertTrue("权限异常 advice 必须早于登录异常 advice", authOrder < loginOrder);
        assertTrue("登录异常 advice 必须早于基础通用 advice", loginOrder < basicOrder);
        assertEquals(Ordered.LOWEST_PRECEDENCE, basicOrder);
    }

    @Test
    public void loginAdviceDoesNotFallbackToThrowable() {
        for (Method method : AuthenticationExceptionAdviceHandler.class.getDeclaredMethods()) {
            ExceptionHandler handler = method.getAnnotation(ExceptionHandler.class);
            if (handler == null) {
                continue;
            }
            for (Class<? extends Throwable> type : handler.value()) {
                assertFalse("登录异常 advice 不应处理通用 Throwable：" + method.getName(),
                        type == Throwable.class || type == Exception.class);
            }
        }
    }

    @Test
    public void adviceKeepsSpecificBusinessCodes() {
        Result<Void> authorization = new AuthExceptionHandler().authException(new AuthException("no menu auth"), null);
        assertEquals(Integer.valueOf(403), authorization.getCode());
        assertEquals("AUTHORIZATION", authorization.getErrorType());

        Result<Void> usage = new AuthExceptionHandler().authServiceException(new AuthServiceException("bad call"), null);
        assertEquals(Integer.valueOf(400), usage.getCode());
        assertEquals("USAGE", usage.getErrorType());
        assertFalse("授权拒绝与用法错误必须使用不同分类", usage.getErrorType().equals(authorization.getErrorType()));

        Result<Void> noLogin = new AuthenticationExceptionAdviceHandler()
                .noLoginException(new NoLoginException("not login"), null);
        assertEquals(Integer.valueOf(401), noLogin.getCode());
        assertEquals("AUTHENTICATION", noLogin.getErrorType());

        Result<Void> accessDenied = new AuthenticationExceptionAdviceHandler()
                .accessDeniedException(new AccessDeniedException("denied"), null);
        assertEquals(Integer.valueOf(403), accessDenied.getCode());
        assertEquals("AUTHORIZATION", accessDenied.getErrorType());

        Result<Void> biz = new GlobalExceptionHandlerAdvice().bizException(new BizException("biz failed"), null);
        assertEquals(Integer.valueOf(400), biz.getCode());
        assertEquals("BUSINESS", biz.getErrorType());
    }

    private int orderOf(Class<?> adviceType) {
        Order order = adviceType.getAnnotation(Order.class);
        assertNotNull(adviceType.getName() + " 缺少 @Order", order);
        return order.value();
    }
}
