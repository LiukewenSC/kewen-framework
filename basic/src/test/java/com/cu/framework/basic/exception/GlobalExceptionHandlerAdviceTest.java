package com.cu.framework.basic.exception;

import com.cu.framework.basic.model.Result;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.Method;
import java.time.LocalDate;

/**
 * 全局异常出口的分类、文案与 HTTP 状态契约
 *
 * @author kewen
 * @since 2026-10-30
 */
public class GlobalExceptionHandlerAdviceTest {

    private final GlobalExceptionHandlerAdvice advice = new GlobalExceptionHandlerAdvice();

    @Test
    public void bizExceptionMapsToBusiness() {
        Result<Void> result = advice.bizException(new BizException("余额不足"), null);

        Assert.assertEquals(Integer.valueOf(400), result.getCode());
        Assert.assertEquals("BUSINESS", result.getErrorType());
        Assert.assertEquals("余额不足", result.getMessage());
    }

    @Test
    public void validationMessageIsComplete() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "req");
        bindingResult.addError(new FieldError("req", "age", null, false, null, null, "年龄不能为空"));
        BindException exception = new BindException(bindingResult);

        Result<Void> result = advice.bindException(exception, null);

        Assert.assertEquals(Integer.valueOf(400), result.getCode());
        Assert.assertEquals("VALIDATION", result.getErrorType());
        Assert.assertEquals("[age]年龄不能为空", result.getMessage());
    }

    @Test
    public void requestFormatFailuresMapToValidation() {
        Result<Void> notReadable = advice.httpMessageNotReadableException(
                new HttpMessageNotReadableException("bad json", (org.springframework.http.HttpInputMessage) null), null);
        Result<Void> typeMismatch = advice.methodArgumentTypeMismatchException(
                new MethodArgumentTypeMismatchException("abc", LocalDate.class, "localDate", null, null), null);
        Result<Void> missingParameter = advice.missingServletRequestParameterException(
                new MissingServletRequestParameterException("name", "String"), null);

        Assert.assertEquals("VALIDATION", notReadable.getErrorType());
        Assert.assertEquals("VALIDATION", typeMismatch.getErrorType());
        Assert.assertEquals("参数[localDate]格式不正确", typeMismatch.getMessage());
        Assert.assertEquals("VALIDATION", missingParameter.getErrorType());
        Assert.assertEquals("缺少必需参数[name]", missingParameter.getMessage());
    }

    @Test
    public void databaseFailureHidesInternalDetail() {
        Result<Void> result = advice.dataAccessException(
                new DataAccessResourceFailureException("select * from secret_table"), null);

        Assert.assertEquals(Integer.valueOf(500), result.getCode());
        Assert.assertEquals("DATABASE", result.getErrorType());
        Assert.assertEquals("数据库操作失败，请稍后重试", result.getMessage());
        Assert.assertFalse(result.getMessage().contains("secret_table"));
    }

    @Test
    public void wrappedDatabaseFailureStillMapsToDatabase() {
        Result<Void> result = advice.throwable(
                new IllegalStateException("wrapper", new DataAccessResourceFailureException("db down")), null);

        Assert.assertEquals("DATABASE", result.getErrorType());
        Assert.assertEquals("数据库操作失败，请稍后重试", result.getMessage());
    }

    @Test
    public void unexpectedAndNullPointerFailuresMapToSystem() {
        Result<Void> unexpected = advice.throwable(new IllegalStateException("boom"), null);
        Result<Void> nullPointer = advice.throwable(new NullPointerException("npe"), null);

        Assert.assertEquals("SYSTEM", unexpected.getErrorType());
        Assert.assertEquals("SYSTEM", nullPointer.getErrorType());
        Assert.assertEquals("系统内部错误，请稍后重试", nullPointer.getMessage());
    }

    @Test
    public void handlersDeclareMatchingHttpStatus() throws Exception {
        assertStatus("bizException", HttpStatus.BAD_REQUEST, BizException.class, jakarta.servlet.http.HttpServletRequest.class);
        assertStatus("bindException", HttpStatus.BAD_REQUEST, BindException.class, jakarta.servlet.http.HttpServletRequest.class);
        assertStatus("httpMessageNotReadableException", HttpStatus.BAD_REQUEST,
                HttpMessageNotReadableException.class, jakarta.servlet.http.HttpServletRequest.class);
        assertStatus("methodArgumentTypeMismatchException", HttpStatus.BAD_REQUEST,
                MethodArgumentTypeMismatchException.class, jakarta.servlet.http.HttpServletRequest.class);
        assertStatus("missingServletRequestParameterException", HttpStatus.BAD_REQUEST,
                MissingServletRequestParameterException.class, jakarta.servlet.http.HttpServletRequest.class);
        assertStatus("dataAccessException", HttpStatus.INTERNAL_SERVER_ERROR,
                org.springframework.dao.DataAccessException.class, jakarta.servlet.http.HttpServletRequest.class);
        assertStatus("throwable", HttpStatus.INTERNAL_SERVER_ERROR,
                Throwable.class, jakarta.servlet.http.HttpServletRequest.class);
    }

    private void assertStatus(String methodName, HttpStatus expected, Class<?>... parameterTypes) throws Exception {
        Method method = GlobalExceptionHandlerAdvice.class.getMethod(methodName, parameterTypes);
        ResponseStatus status = method.getAnnotation(ResponseStatus.class);
        Assert.assertNotNull(methodName + " 缺少 @ResponseStatus", status);
        Assert.assertEquals(methodName + " 的 HTTP 状态不符合分类映射", expected, status.value());
    }
}
