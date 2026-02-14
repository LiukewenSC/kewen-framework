package com.kewen.framework.auth.security.extension;

import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 组合 Session ID 解析器，支持 Header（token）和 Cookie 两种方式。
 * <p>
 * 解析优先级：Header > Cookie
 * <ul>
 *   <li>请求中携带了指定 Header（如 Authorization），则使用 Header 中的值作为 Session ID</li>
 *   <li>请求中没有携带 Header，则回退到 Cookie 方式解析 Session ID</li>
 * </ul>
 * <p>
 * 写入 Session ID 时，同时写入 Header 和 Cookie，确保两种客户端都能正常工作。
 *
 * @author kewen
 * @since 1.0
 */
public class HeaderCookieHttpSessionIdResolver implements HttpSessionIdResolver {

    private static final String SESSION_RESOLVE_MODE = HeaderCookieHttpSessionIdResolver.class.getName() + ".MODE";
    private static final String MODE_HEADER = "HEADER";

    private final HeaderHttpSessionIdResolver headerResolver;
    private final CookieHttpSessionIdResolver cookieResolver;

    public HeaderCookieHttpSessionIdResolver(String headerName) {
        this.headerResolver = new HeaderHttpSessionIdResolver(headerName);
        this.cookieResolver = new CookieHttpSessionIdResolver();
    }

    /**
     * 解析 Session ID，优先从 Header 中获取，没有则从 Cookie 中获取
     */
    @Override
    public List<String> resolveSessionIds(HttpServletRequest request) {
        List<String> headerSessionIds = headerResolver.resolveSessionIds(request);
        if (!headerSessionIds.isEmpty()) {
            request.setAttribute(SESSION_RESOLVE_MODE, MODE_HEADER);
            return headerSessionIds;
        }
        return cookieResolver.resolveSessionIds(request);
    }

    /**
     * 写入 Session ID。
     * 如果本次请求是通过 Header 方式解析的，则通过 Header 返回；否则通过 Cookie 返回。
     */
    @Override
    public void setSessionId(HttpServletRequest request, HttpServletResponse response, String sessionId) {
        if (MODE_HEADER.equals(request.getAttribute(SESSION_RESOLVE_MODE))) {
            headerResolver.setSessionId(request, response, sessionId);
        } else {
            cookieResolver.setSessionId(request, response, sessionId);
        }
    }

    /**
     * 过期 Session，同时清除 Header 和 Cookie
     */
    @Override
    public void expireSession(HttpServletRequest request, HttpServletResponse response) {
        headerResolver.expireSession(request, response);
        cookieResolver.expireSession(request, response);
    }
}
