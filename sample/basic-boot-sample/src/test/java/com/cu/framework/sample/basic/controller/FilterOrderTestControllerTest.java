package com.cu.framework.sample.basic.controller;

import com.cu.framework.basic.support.notify.fangtang.FangTangMessageClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * /filter/hello 的接口测试，用于固化统一响应信封与流水号回显契约
 *
 * @author kewen
 * @since 2026-10-15
 */
public class FilterOrderTestControllerTest extends AbstractBasicSampleWebTest {

    @Autowired
    private ApplicationContext context;

    /**
     * 基类关掉 cu.notify.fang-tang.enabled 后该客户端不再注册，保证跑测试时不会向 sctapi.ftqq.com 出站
     */
    @Test
    public void fangTangMessageClientNotRegistered() {
        assertEquals(0, this.context.getBeanNamesForType(FangTangMessageClient.class).length,
                "测试上下文不应注册 FangTangMessageClient，否则每个请求都会产生出站 HTTP");
    }

    @Test
    public void successResponseCarriesFullEnvelope() throws Exception {
        JsonNode body = performJson(get("/filter/hello"), 200);

        assertTrue(body.has("code") && body.has("success") && body.has("message")
                        && body.has("data") && body.has("traceId"),
                "响应应同时包含 code、success、message、data、traceId 五个字段，实际为 " + body);
        assertEquals(200, body.get("code").asInt());
        assertTrue(body.get("success").asBoolean());
        assertEquals("成功", body.get("message").asText());
        assertTrue(body.get("data").isNull(), "无业务数据时 data 应以 null 出现而不是被省略");
    }

    @Test
    public void generatedTraceIdIsNotEmpty() throws Exception {
        JsonNode body = performJson(get("/filter/hello"), 200);

        assertFalse(body.get("traceId").asText().isEmpty(),
                "请求头未携带流水号时应生成非空值，实际为 " + body.get("traceId").asText());
    }

    @Test
    public void traceIdHeaderIsEchoed() throws Exception {
        JsonNode body = performJson(get("/filter/hello").header("traceId", "sample-trace-0001"), 200);

        assertEquals("sample-trace-0001", body.get("traceId").asText(),
                "响应流水号应回显请求头携带的值");
    }
}
