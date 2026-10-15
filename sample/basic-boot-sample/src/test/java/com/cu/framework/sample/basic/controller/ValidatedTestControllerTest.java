package com.cu.framework.sample.basic.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * /validated/hello 的声明式校验契约测试
 *
 * @author kewen
 * @since 2026-10-15
 */
public class ValidatedTestControllerTest extends AbstractBasicSampleWebTest {

    @Test
    public void validBodyIsAccepted() throws Exception {
        JsonNode body = performJson(post("/validated/hello")
                .contentType("application/json")
                .content("{\"id\":1,\"name\":\"张三\",\"age\":18,\"enable\":true,\"roles\":[\"admin\"]}"), 200);

        assertEquals(200, body.get("code").asInt());
        assertTrue(body.get("success").asBoolean());
    }

    @Test
    public void invalidBodyReturnsHttp200WithFailedEnvelope() throws Exception {
        JsonNode body = performJson(post("/validated/hello")
                .contentType("application/json")
                .content("{\"age\":18,\"enable\":true,\"roles\":[\"admin\"]}"), 200);

        assertEquals(500, body.get("code").asInt());
        assertFalse(body.get("success").asBoolean());
        String message = body.get("message").asText();
        assertTrue(message.contains("[id]") && message.contains("[name]"),
                "message 应同时带出两个失败字段的标记，实际为 " + message);
    }

    /**
     * 只让 age 单字段失败，使 message 不依赖校验默认提示的语言环境，从而能断出完整字符串。
     * 期望值刻意保留了 GlobalExceptionHandlerAdvice 中 substring(0, length() - 2) 的缺陷：
     * 拼接结果 "[age]年龄不能为空;" 被删掉了分号和其前面的"空"字。修复该缺陷时本断言应变红
     */
    @Test
    public void singleFieldFailureMessageIsTruncatedByTwoCharacters() throws Exception {
        JsonNode body = performJson(post("/validated/hello")
                .contentType("application/json")
                .content("{\"id\":1,\"name\":\"张三\",\"enable\":true,\"roles\":[\"admin\"]}"), 200);

        assertEquals("[age]年龄不能为", body.get("message").asText());
    }
}
