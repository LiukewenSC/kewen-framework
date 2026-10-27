package com.cu.framework.auth.rbac.security;

import com.cu.framework.auth.core.exception.AuthException;
import com.cu.framework.auth.core.exception.AuthServiceException;
import com.cu.framework.authentication.exception.NoLoginException;
import com.cu.framework.authentication.handler.AuthenticationExceptionAdviceHandler;
import com.cu.framework.basic.exception.BizException;
import com.cu.framework.basic.exception.GlobalExceptionHandlerAdvice;
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
        assertEquals(Integer.valueOf(403),
                new AuthExceptionHandler().authException(new AuthException("no menu auth")).getCode());
        assertEquals(Integer.valueOf(400),
                new AuthExceptionHandler().authServiceException(new AuthServiceException("bad call")).getCode());
        assertEquals(Integer.valueOf(401),
                new AuthenticationExceptionAdviceHandler()
                        .authenticationException(new NoLoginException("not login")).getCode());
        assertEquals(Integer.valueOf(401),
                new AuthenticationExceptionAdviceHandler()
                        .accessDeniedException(new AccessDeniedException("denied")).getCode());
        assertEquals(Integer.valueOf(500),
                new GlobalExceptionHandlerAdvice().bizException(new BizException("biz failed")).getCode());
    }

    private int orderOf(Class<?> adviceType) {
        Order order = adviceType.getAnnotation(Order.class);
        assertNotNull(adviceType.getName() + " 缺少 @Order", order);
        return order.value();
    }
}
