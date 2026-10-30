package com.cu.framework.basic.model;

import com.cu.framework.basic.exception.ErrorType;
import com.cu.framework.basic.logger.trace.TraceResult;
import tools.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

/**
 * 失败信封的错误分类字段：成功不输出，失败输出，TraceResult 包装后不丢失
 *
 * @author kewen
 * @since 2026-10-30
 */
public class ResultErrorTypeTest {

    @Test
    public void successResultOmitsErrorType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(Result.success());

        Assert.assertFalse("成功响应不得输出 errorType，实际为 " + json, json.contains("errorType"));
    }

    @Test
    public void failedResultCarriesErrorType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Result<Void> result = Result.failed(ErrorType.AUTHENTICATION, "用户名或密码错误");
        String json = mapper.writeValueAsString(result);

        Assert.assertEquals(Integer.valueOf(401), result.getCode());
        Assert.assertEquals("AUTHENTICATION", result.getErrorType());
        Assert.assertTrue("失败响应应输出分类，实际为 " + json, json.contains("\"errorType\":\"AUTHENTICATION\""));
    }

    @Test
    public void traceResultKeepsErrorType() {
        TraceResult<Void> traceResult = TraceResult.of(
                Result.failed(ErrorType.DATABASE, "数据库操作失败，请稍后重试"), "trace-1");

        Assert.assertEquals("DATABASE", traceResult.getErrorType());
        Assert.assertEquals(Integer.valueOf(500), traceResult.getCode());
    }
}
