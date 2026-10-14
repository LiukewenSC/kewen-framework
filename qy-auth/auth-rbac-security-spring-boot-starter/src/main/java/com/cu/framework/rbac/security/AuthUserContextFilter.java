package com.cu.framework.rbac.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cu.framework.auth.core.AuthUserContext;
import com.cu.framework.authentication.model.SecurityUser;
import com.cu.framework.authentication.response.AuthenticationSuccessResultResolver;
import com.cu.framework.authentication.response.AuthenticationSuccessResultConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

/**
 * 用户权限上下文，过滤器，添加用户权限上下文，这个是在登录之后进行
 * 因为涉及到rememberMe，因此需要在remember之后进行处理，
 * 那这里就直接在{@link org.springframework.security.web.session.SessionManagementFilter}后面执行了
 *
 * @author kewen
 * @since 2024-07-02
 */
public class AuthUserContextFilter extends OncePerRequestFilter {

    /** 查询当前登录用户的请求 URI，命中该 URI 的请求由本过滤器直接返回，不再进入后续过滤链 */
    private String currentUserUrl;
    /** 写出 JSON 响应体所用的序列化器 */
    private ObjectMapper objectMapper;
    /** 认证成功结果解析器，将 SecurityUser 包装为最终响应结构 */
    private AuthenticationSuccessResultResolver resultResolver;
    /** Authentication 转 SecurityUser 的转换器提供者 */
    private ObjectProvider<AuthenticationSuccessResultConverter> jsonSuccessResultConverter;
    /** 用户详情服务，供转换器加载用户使用 */
    private UserDetailsService userDetailsService;

    /**
     * 构造过滤器
     * @param currentUserUrl 查询当前登录用户的请求 URI
     * @param resultResolver 认证成功结果解析器
     * @param objectMapper JSON 序列化器
     * @param jsonSuccessResultConverter 认证结果转换器提供者
     * @param userDetailsService 用户详情服务
     */
    public AuthUserContextFilter(String currentUserUrl, AuthenticationSuccessResultResolver resultResolver ,
                                 ObjectMapper objectMapper, ObjectProvider<AuthenticationSuccessResultConverter> jsonSuccessResultConverter,
                                 UserDetailsService userDetailsService) {
        this.currentUserUrl = currentUserUrl;
        this.resultResolver = resultResolver;
        this.objectMapper = objectMapper;
        this.jsonSuccessResultConverter = jsonSuccessResultConverter;
        this.userDetailsService = userDetailsService;
    }

    /**
     * 已认证时用支持的转换器将 Authentication 转为 SecurityUser 写入 AuthUserContext；
     * 若请求 URI 为 currentUserUrl，则解析当前用户并直接以 JSON 返回，不再走后续过滤链；否则放行
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Optional<Authentication> optionalAuthentication = Optional.of(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication);

        //设置用户权限上下文
        optionalAuthentication.ifPresent(authentication -> {
            for (AuthenticationSuccessResultConverter successResultConverter : jsonSuccessResultConverter) {
                if (successResultConverter.support(authentication)) {
                    SecurityUser securityUser = successResultConverter.convert(userDetailsService, authentication);
                    // todo 设置用户权限对象
                    RbacSecurityCurrentUser user = new RbacSecurityCurrentUser()
                            .setUsername(securityUser.getUsername())
                            .setAuthObject(null);
                    AuthUserContext.setCurrentUser(user);
                    break;
                }
            }
        });

        //如果是查询当前用户，则直接返回，没有查询到则报错需要登录
        if (currentUserUrl.equals(request.getRequestURI())) {
            SecurityUser securityUser = (SecurityUser)AuthUserContext.getCurrentUser();
            Object result = resultResolver.resolver(request, response, securityUser);
            writeResponseBody(response,result);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 将结果对象序列化为 JSON 并以 UTF-8 写入响应体
     */
    private void writeResponseBody(HttpServletResponse response,Object result) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter out = response.getWriter();
        out.write(objectMapper.writeValueAsString(result));
        out.flush();
        out.close();
    }
}
