package com.cu.framework.authentication.config;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;

/**
 * 会话过期出口的分类与响应形状
 *
 * @author kewen
 * @since 2026-10-30
 */
public class SessionConfigTest {

    @Test
    public void expiredSessionResponseCarriesAuthenticationType() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new SessionConfig().writeExpiredSessionResponse(response);

        Assert.assertEquals(401, response.getStatus());
        String body = new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);
        Assert.assertTrue("会话过期必须带认证分类，实际为 " + body, body.contains("\"errorType\":\"AUTHENTICATION\""));
        Assert.assertTrue("会话过期提示不匹配，实际为 " + body, body.contains("登录状态已失效，请重新登录"));
        Assert.assertFalse("响应体不得包含异常堆栈文本", body.contains("Exception"));
    }
}
