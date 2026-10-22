package com.cu.framework.auth.sample;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * auth-rbac-boot-sample 的 web 层测试基类，集中承载上下文配置使各子类共用同一个缓存的 Spring 上下文。
 * <p>
 * {@code addFilters = false} 是有意的：本示例的身份由请求头声明而不走 Spring Security 登录态，
 * 关掉过滤器链后剩下的正是菜单鉴权这一层，判据不会被登录状态干扰
 *
 * @author kewen
 * @since 2026-10-22
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AuthRbacBootSampleApp.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("h2")
public abstract class AbstractRbacSampleWebTest {

    /** 独立于被测的框架 ObjectMapper，避免用被测对象来验证被测对象 */
    private static final ObjectMapper RESPONSE_MAPPER = new ObjectMapper();

    @Autowired
    protected MockMvc mockMvc;

    /**
     * 发出请求并断言 HTTP 状态码为 200，返回解析后的响应体。
     * 权限拒绝是通过响应体的 code 交付的，HTTP 状态码不是 200 说明请求根本没走到权限判定
     *
     * @param builder 请求构造器
     * @return 响应体 JSON
     */
    protected JsonNode performJson(MockHttpServletRequestBuilder builder) throws Exception {
        MvcResult result = this.mockMvc.perform(builder)
                .andExpect(status().is(HttpStatus.OK.value()))
                .andReturn();
        return RESPONSE_MAPPER.readTree(result.getResponse().getContentAsByteArray());
    }
}
