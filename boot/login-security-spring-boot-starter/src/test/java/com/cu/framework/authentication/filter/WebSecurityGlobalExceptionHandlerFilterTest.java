package com.cu.framework.authentication.filter;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * 安全过滤器在无解析器时的兜底输出
 *
 * @author kewen
 * @since 2026-10-30
 */
public class WebSecurityGlobalExceptionHandlerFilterTest {

    @Test
    public void missingResolverWritesSystemEnvelope() throws Exception {
        HandlerExceptionResolver noResolver = (request, response, handler, ex) -> null;
        WebSecurityGlobalExceptionHandlerFilter filter = new WebSecurityGlobalExceptionHandlerFilter(noResolver);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/currentUser");
        request.setRequestURI("/currentUser");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            throw new IllegalStateException("filter boom");
        });

        Assert.assertEquals(500, response.getStatus());
        String body = response.getContentAsString();
        Assert.assertTrue("兜底也必须输出统一信封，实际为 " + body, body.contains("\"errorType\":\"SYSTEM\""));
        Assert.assertFalse("异常原文不得进入响应体", body.contains("filter boom"));
    }
}
