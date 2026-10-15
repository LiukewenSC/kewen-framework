package com.cu.framework.sample.basic.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * /jackson 下各接口的日期绑定与序列化契约测试
 *
 * @author kewen
 * @since 2026-10-15
 */
public class JacksonSerializerControllerTest extends AbstractBasicSampleWebTest {

    private static final Pattern DATE_TIME = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");

    private static final Pattern DATE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    /**
     * 非 yyyy-MM-dd 形态的文本，用于触发绑定失败
     */
    private static final String MALFORMED_DATE = "20240101";

    private static final String LOCAL_DATE_TEXT = "2024-01-01";

    private static final String LOCAL_DATE_TIME_TEXT = "2024-01-01 12:30:45";

    @Test
    public void validDateParamsAreAccepted() throws Exception {
        JsonNode body = performJson(get("/jackson/paramSerializer")
                .param("localDate", LOCAL_DATE_TEXT)
                .param("localDateTime", LOCAL_DATE_TIME_TEXT), 200);

        assertEquals(200, body.get("code").asInt());
        assertTrue(body.get("success").asBoolean());
        // 绑定成功且按同一格式往返，回显值必须与提交值逐字相同
        assertEquals(LOCAL_DATE_TEXT, body.get("data").get("localDate").asText(), "响应体为 " + body);
        assertEquals(LOCAL_DATE_TIME_TEXT, body.get("data").get("localDateTime").asText(), "响应体为 " + body);
    }

    @Test
    public void omittedDateParamsAreAccepted() throws Exception {
        JsonNode body = performJson(get("/jackson/paramSerializer"), 200);

        assertEquals(200, body.get("code").asInt());
        assertTrue(body.get("success").asBoolean());
        // 参数缺省时两个键仍存在且为 null，说明未传参绑定为空值而不是报错
        JsonNode data = body.get("data");
        assertTrue(data.has("localDate") && data.has("localDateTime"), "缺省时两个键应仍存在，实际为 " + data);
        assertTrue(data.get("localDate").isNull() && data.get("localDateTime").isNull(),
                "缺省时两个参数应绑定为 null，实际为 " + data);
    }

    @Test
    public void malformedDateParamIsRejectedWithFailedEnvelope() throws Exception {
        JsonNode body = performJson(get("/jackson/paramSerializer").param("localDate", MALFORMED_DATE), 500);

        assertFalse(body.get("success").asBoolean());
        assertEquals(500, body.get("code").asInt());
        assertTrue(body.has("data") && body.has("traceId"),
                "失败响应也应保持五字段信封，而不是返回未包装的异常信息，实际为 " + body);
    }

    @Test
    public void boundObjectDateFieldsAreAccepted() throws Exception {
        JsonNode body = performJson(get("/jackson/paramSerializer2")
                .param("localDate", LOCAL_DATE_TEXT)
                .param("localDateTime", LOCAL_DATE_TIME_TEXT), 200);

        assertEquals(200, body.get("code").asInt());
        assertTrue(body.get("success").asBoolean());
        JsonNode data = body.get("data");
        assertEquals(LOCAL_DATE_TEXT, data.get("localDate").asText(),
                "对象属性绑定的回显值应与提交值逐字相同，实际为 " + data);
        assertEquals(LOCAL_DATE_TIME_TEXT, data.get("localDateTime").asText(),
                "对象属性绑定的回显值应与提交值逐字相同，实际为 " + data);
    }

    @Test
    public void omittedObjectDateFieldsAreAccepted() throws Exception {
        JsonNode body = performJson(get("/jackson/paramSerializer2"), 200);

        assertEquals(200, body.get("code").asInt());
        assertTrue(body.get("success").asBoolean());
        JsonNode data = body.get("data");
        assertTrue(data.get("localDate").isNull() && data.get("localDateTime").isNull(),
                "缺省时对象的两个字段应绑定为 null，实际为 " + data);
    }

    /**
     * 该接口以 @GetMapping 声明却带 @RequestBody，非标准用法，此处只验证 JSON 日期字段的反序列化
     */
    @Test
    public void jsonBodyDateFieldsAreAccepted() throws Exception {
        JsonNode body = performJson(get("/jackson/paramSerializer3")
                .contentType("application/json")
                .content("{\"localDate\":\"" + LOCAL_DATE_TEXT + "\",\"localDateTime\":\"" + LOCAL_DATE_TIME_TEXT + "\"}"),
                200);

        assertEquals(200, body.get("code").asInt());
        assertTrue(body.get("success").asBoolean());
        JsonNode data = body.get("data");
        assertEquals(LOCAL_DATE_TEXT, data.get("localDate").asText(),
                "JSON 请求体反序列化后的回显值应与提交值逐字相同，实际为 " + data);
        assertEquals(LOCAL_DATE_TIME_TEXT, data.get("localDateTime").asText(),
                "JSON 请求体反序列化后的回显值应与提交值逐字相同，实际为 " + data);
    }

    /**
     * 请求体解析失败同样被全局异常处理的 Throwable 分支兜住，不返回 Spring 默认的 400 错误页
     */
    @Test
    public void malformedJsonBodyDateFieldIsRejected() throws Exception {
        JsonNode body = performJson(get("/jackson/paramSerializer3")
                .contentType("application/json")
                .content("{\"localDate\":\"" + MALFORMED_DATE + "\"}"), 500);

        assertFalse(body.get("success").asBoolean());
        assertEquals(500, body.get("code").asInt());
        assertTrue(body.has("message") && body.has("data") && body.has("traceId"),
                "失败响应也应保持五字段信封，而不是返回未包装的异常信息，实际为 " + body);
    }

    @Test
    public void dateFieldsAreSerializedAsFormattedStrings() throws Exception {
        JsonNode data = performJson(get("/jackson/deSerializerDate"), 200).get("data");

        assertTrue(data.get("date").isTextual(), "Date 应输出为字符串而不是数字时间戳");
        assertTrue(data.get("localDate").isTextual(), "LocalDate 应输出为字符串而不是数字时间戳");
        assertTrue(data.get("localDateTime").isTextual(), "LocalDateTime 应输出为字符串而不是数字时间戳");

        assertTrue(DATE_TIME.matcher(data.get("date").asText()).matches(),
                "Date 实际输出 " + data.get("date").asText());
        assertTrue(DATE.matcher(data.get("localDate").asText()).matches(),
                "LocalDate 实际输出 " + data.get("localDate").asText());
        assertTrue(DATE_TIME.matcher(data.get("localDateTime").asText()).matches(),
                "LocalDateTime 实际输出 " + data.get("localDateTime").asText());
    }
}
