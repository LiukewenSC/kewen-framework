package com.cu.framework.authentication.response;

import com.cu.framework.authentication.exception.NoLoginException;
import com.cu.framework.authentication.model.SecurityUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * 认证成功处理器
 * @author kewen
 * @since 2024-07-04
 */
public class JsonAuthenticationSuccessHandler implements SecurityAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(JsonAuthenticationSuccessHandler.class);
    /** 写出 JSON 响应体所用的序列化器 */
    private ObjectMapper objectMapper;
    /** 认证成功结果解析器，将响应体包装为最终返回结构 */
    private AuthenticationSuccessResultResolver resultResolver;
    /** 认证结果交付姿态判定器，决定单点登录成功是否改为整页重定向 */
    private LoginDeliveryResolver loginDeliveryResolver;

    public JsonAuthenticationSuccessHandler(AuthenticationSuccessResultResolver resultResolver , ObjectMapper objectMapper,
                                            LoginDeliveryResolver loginDeliveryResolver) {
        this.objectMapper = objectMapper;
        this.resultResolver = resultResolver;
        this.loginDeliveryResolver = loginDeliveryResolver;
    }

    /**
     * 认证成功的数据处理，这里处理了之后就返回了
     * @param request        the request which caused the successful authentication
     * @param response       the response
     * @param authentication the <tt>Authentication</tt> object which was created during
     *                       the authentication process.
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 单点登录的回调由浏览器整页承载，此时没有任何脚本在等响应体，改为把浏览器送回前端。
        // 判定必须早于 resultResolver 调用，否则消费方自定义的解析器会在跳转场景被无谓执行一次。
        if (loginDeliveryResolver.shouldRedirect(request)) {
            loginDeliveryResolver.redirect(response, LoginDeliveryResolver.STATE_SUCCESS);
            return;
        }

        // 所有协议返回的都是SecurityUser，不是的需要自定义AuthenticationProvider，并返回SecurityUser
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof SecurityUser)) {
            throw new NoLoginException("认证主体不是统一认证实体："
                    + (principal == null ? "null" : principal.getClass().getName()));
        }
        // 同一 principal 会被写入 session，清空密码，避免哈希随会话持久化
        SecurityUser securityUser = (SecurityUser) principal;
        securityUser.setPassword("");
        LoginSuccessResult loginSuccessResult = LoginSuccessResult.of(securityUser);

        loginSuccessResult.setToken(request.getSession().getId());
        loginSuccessResult.setLoginTime(LocalDateTime.now());
        Object result =  resultResolver.resolver(request,response, loginSuccessResult);
        writeResponseBody(response, result);
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

    /**
     * 退出登录返回
     * @param request
     * @param response
     * @param authentication
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Object result = resultResolver.resolver(request,response,null);
        writeResponseBody(response, result);
    }
}
