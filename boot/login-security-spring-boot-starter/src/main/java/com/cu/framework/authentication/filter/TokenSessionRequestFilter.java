package com.cu.framework.authentication.filter;

import org.apache.catalina.session.StandardSessionFacade;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;


/**
 * token过滤器，构造token的请求体。
 * 这里不要去处理 上下文相关的，上下文相关的应该在SecurityContextHolder.getContext()中处理,因为SpringSecurity会默认将session中的用户解析到Authentication中
 * 就这样实现是有bug，后续查阅spring-session-redis的实现方案，发现spring-session本身就已经实现了基于header的sessionId传入方式，因此此类废弃，剥离session的实现方案
 *
 * 使用 spring-session-jdbc ，spring-session-redis  spring-session-mongodb均可，
 *  <dependency>
 *      <groupId>org.springframework.session</groupId>
 *      <artifactId>spring-session-jdbc</artifactId>
 *  </dependency>
 *  <dependency>
 *      <groupId>org.springframework.session</groupId>
 *      <artifactId>spring-session-data-redis</artifactId>
 *  </dependency>
 *
 *
 *  配置成 Header就可以了
 *  @Bean
 *  HttpSessionIdResolver sessionIdResolver() {
 *      return new HeaderHttpSessionIdResolver("token");
 *  }
 *
 * @author kewen
 * @since 2024-07-02
 */
@Deprecated
public class TokenSessionRequestFilter extends OncePerRequestFilter {


    /** 承载 token 的请求头名称 */
    String tokenParameter;

    /**
     * 构造过滤器
     * @param tokenParameter 承载 token 的请求头名称
     */
    public TokenSessionRequestFilter(String tokenParameter) {
        this.tokenParameter = tokenParameter;
    }

    /**
     * 将原始请求包装为 TokenSessionHttpServletRequest 后传给过滤链，使请求头中的 token 可作为 session ID 使用
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //封装 token的session
        TokenSessionHttpServletRequest tokenSessionHttpServletRequest = new TokenSessionHttpServletRequest(request,tokenParameter);

        filterChain.doFilter(tokenSessionHttpServletRequest, response);
    }

    /**
     * @author kewen
     * @since 2024-05-17
     */
    private static class TokenSessionHttpServletRequest extends HttpServletRequestWrapper {

        /** 当前线程请求头中携带的 token，构造包装请求时写入 */
        private static final ThreadLocal<String> tokenThreadLocal = new ThreadLocal<>();

        /** token 到 session 的本地缓存 */
        private static final ConcurrentHashMap<String, HttpSession> tokenMap = new ConcurrentHashMap<>();

        /** 本次请求是否从请求头携带了 token */
        private final boolean isToken;

        /** 承载 token 的请求头名称 */
        String tokenParameter;

        /**
         * Constructs a request object wrapping the given request.
         *
         * @param request The request to wrap
         * @throws IllegalArgumentException if the request is null
         */
        public TokenSessionHttpServletRequest(HttpServletRequest request, String tokenParameter) {
            super(request);
            String token = request.getHeader(tokenParameter);
            tokenThreadLocal.set(token);
            isToken = token != null;
        }

        /**
         * 请求头携带了非空 token 时以该 token 作为 session ID 返回，否则回退到容器原始解析结果
         */
        @Override
        public String getRequestedSessionId() {
            if (!isToken) {
                return super.getRequestedSessionId();
            }
            if (StringUtils.hasText(getToken())) {
                return getToken();
            } else {
                return super.getRequestedSessionId();
            }
        }

        /**
         * 没有token，说明没有从前端传入，可能是登录请求
         *
         * @return
         */
        private String getToken() {
            return tokenThreadLocal.get();
        }

        @Override
        public HttpSession getSession() {
            return getSession(true);
        }

        @Override
        public Object getAttribute(String name) {
            return super.getAttribute(name);
        }

        @Override
        public void setAttribute(String name, Object o) {
            super.setAttribute(name, o);
        }

        /**
         * 返回经 TokenSession 门面包装的 session，使会话失效时抛出 CredentialsExpiredException
         */
        @Override
        public HttpSession getSession(boolean create) {
            HttpSession session = super.getSession(create);
            return new TokenSession(session);
        }
        /**
         * 按 token 从本地缓存 tokenMap 中查找 session；无 token 时创建新 session 并以 session ID 作为 token 缓存；
         * 缓存未命中且 create 为 true 时同样创建并缓存
         * @param create 缓存未命中时是否创建新 session
         * @return 命中或新建的 session，未命中且不允许创建时返回 null
         */
        public HttpSession getSessionInternal(boolean create) {
            String token = getToken();
            //没有token则生成一个
            if (token == null) {
                HttpSession session = super.getSession(create);
                if (session != null) {
                    token = session.getId();
                    tokenThreadLocal.set(token);
                    tokenMap.put(token, session);
                }
                return session;
            }

            HttpSession session = tokenMap.get(getToken());
            if (session != null) {

                return session;
            }

            //不创建则就直接返回了
            if (!create) {
                return session;
            }
            //创建则创建session，并加入到threadLocal中
            session = super.getSession(true);
            token = session.getId();

            tokenMap.put(token, session);
            tokenThreadLocal.set(token);

            return session;
        }
    }
    /**
     * session 门面，将读取属性时 session 已失效的 IllegalStateException 转为 CredentialsExpiredException
     * @author kewen
     * @since 2026-02-12
     */
    static class TokenSession extends StandardSessionFacade {

        public TokenSession(HttpSession session) {
            super(session);
        }

        /**
         * 读取 session 属性，session 已失效时抛出“登录已过期”的 CredentialsExpiredException
         */
        @Override
        public Object getAttribute(String name) {
            try {
                return super.getAttribute(name);
            } catch (IllegalStateException e) {
                //java.lang.IllegalStateException: getAttribute: Session already invalidated
                throw new CredentialsExpiredException("登录已过期", e);
            }
        }
    }
}
