package com.cu.framework.sample.basic.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * basic-boot-sample 的 web 层测试基类，集中承载上下文配置使各子类共用同一个缓存的 Spring 上下文
 *
 * @author kewen
 * @since 2026-10-15
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("h2")
@TestPropertySource(properties = {
        "cu.notify.fang-tang.enabled=false",
        "cu.request.message.fang-tang=false"
})
public abstract class AbstractBasicSampleWebTest {

    /** 独立于被测的框架 ObjectMapper，避免用被测对象来验证被测对象 */
    private static final ObjectMapper RESPONSE_MAPPER = new ObjectMapper();

    @Autowired
    protected MockMvc mockMvc;

    /**
     * 发出请求，断言 HTTP 状态码后返回解析后的响应体
     * @param builder 请求构造器
     * @param expectedStatus 期望的 HTTP 状态码
     * @return 响应体 JSON
     */
    protected JsonNode performJson(MockHttpServletRequestBuilder builder, int expectedStatus) throws Exception {
        MvcResult result = this.mockMvc.perform(builder)
                .andExpect(status().is(expectedStatus))
                .andReturn();
        return RESPONSE_MAPPER.readTree(result.getResponse().getContentAsByteArray());
    }
}
