package com.cu.framework.authentication.response;

import com.alibaba.fastjson.JSONObject;
import com.cu.framework.basic.exception.ErrorLog;
import com.cu.framework.basic.exception.ErrorType;
import com.cu.framework.basic.model.Result;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * 安全异常返回处理，这里就直接加入SpringMVC的异常解析器了，子工程里使用解析器解析吧，统一了SpringSecurity的异常不能通用处理
 *
 * @author kewen
 * @since 2024-07-02
 */
public class SecurityAuthenticationExceptionResolverHandler implements AuthenticationEntryPoint, AuthenticationFailureHandler, AccessDeniedHandler {

    List<HandlerExceptionResolver> handlerExceptionResolvers;

    /**
     * 认证结果交付姿态判定器，为空时一律按 JSON 返回
     */
    private LoginDeliveryResolver loginDeliveryResolver;

    /**
     * AuthenticationEntryPoint 的实现
     * 处理未进入登录但后续需要验证登录的异常
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        resolver(request, response, exception);
    }

    /**
     * AccessDeniedHandler 的实现
     * 访问异常的处理
     * @throws ServletException
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException, ServletException {
        resolver(request, response, exception);
    }

    /**
     * 认证失败的处理
     * AuthenticationFailureHandler 的实现
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        resolver(request, response, exception);
    }

    /**
     * 具体解析器的处理逻辑
     */
    private void resolver(HttpServletRequest request, HttpServletResponse response, Exception exception) throws IOException {
        // 单点登录的失败同样由浏览器整页承载，与成功路径共用一条判定规则，避免把裸 JSON 甩在用户屏幕上。
        // 账号密码端点与非认证端点不在判定名单内，它们的失败仍由 JSON 错误出口按统一分类交付。
        if (loginDeliveryResolver.shouldRedirect(request)) {
            // 重定向不带响应体，浏览器侧也看不到原因，服务端日志是唯一的取证处；
            // 走 JSON 分支时由 HandlerExceptionResolver 负责记录，不在此重复
            ErrorLog.warn(ErrorType.AUTHENTICATION, request,
                    "单点登录认证失败，按失败姿态回跳：" + exception.getMessage());
            loginDeliveryResolver.redirect(response, LoginDeliveryResolver.STATE_FAILURE);
            return;
        }
        //拿到容器中配置的HandlerExceptionResolver，并调用其解析逻辑，这个类本身就会扫描`@ControllerAdvice`并加入HandlerExceptionResolver中
        for (HandlerExceptionResolver handlerExceptionResolver : handlerExceptionResolvers) {
            ModelAndView modelAndView = handlerExceptionResolver.resolveException(request, response, null, exception);
            if (modelAndView != null) {
                return;
            }
        }
        //没有解析到，没有解析到那就默认返回
        ErrorLog.error(ErrorType.SYSTEM, request, "安全异常没有可用的解析器", exception);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json;charset=utf-8");
        try {
            PrintWriter out = response.getWriter();
            out.write(JSONObject.toJSONString(Result.failed(ErrorType.SYSTEM, "系统内部错误，请稍后重试")));
            out.flush();
            out.close();
        } catch (IOException e) {
            throw new AuthenticationServiceException("SecurityExceptionResolverHandler 解析返回异常", e);
        }

    }

    public SecurityAuthenticationExceptionResolverHandler setHandlerExceptionResolvers(List<HandlerExceptionResolver> handlerExceptionResolvers) {
        this.handlerExceptionResolvers = handlerExceptionResolvers;
        return this;
    }

    public SecurityAuthenticationExceptionResolverHandler setLoginDeliveryResolver(LoginDeliveryResolver loginDeliveryResolver) {
        this.loginDeliveryResolver = loginDeliveryResolver;
        return this;
    }

}
