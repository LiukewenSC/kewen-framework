package com.cu.framework.authentication.filter;

import com.alibaba.fastjson.JSONObject;
import com.cu.framework.basic.exception.ErrorLog;
import com.cu.framework.basic.exception.ErrorType;
import com.cu.framework.basic.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
/**
 * 全局异常处理过滤器，在SpringSecurity的过滤器链之前执行，
 * @author kewen
 * @since 2024-07-10
 */
@Order(-101)
public class WebSecurityGlobalExceptionHandlerFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(WebSecurityGlobalExceptionHandlerFilter.class);
    HandlerExceptionResolver handlerExceptionResolver;

    public WebSecurityGlobalExceptionHandlerFilter(HandlerExceptionResolver handlerExceptionResolver) {
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    /**
     * 拦截 /error 请求直接按“没有此页面”异常解析；执行后续过滤链时捕获所有异常
     * （一般是 Spring Security 过滤器抛出的），交给 HandlerExceptionResolver 解析，
     * 不再向外抛出，避免触发 Tomcat 的错误页重定向
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)  {
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/error")) {
            log.debug("又跑到了错误页面");
            resolver(request,response,new RuntimeException("没有此页面 /error"));
        }
        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex){
            log.debug("security前处理返回异常，一般说明SpringSecurity过滤器出异常了");
            //这里不能再往外抛了，会导致tomcat的重定向
            //throw ex;
            resolver(request,response,ex);
        }
    }
    /**
     * 用 HandlerExceptionResolver 解析异常；若无解析器处理（返回 null），则写出 SYSTEM 分类的统一信封
     */
    private void resolver(HttpServletRequest request, HttpServletResponse response, Exception ex){
        ModelAndView modelAndView = handlerExceptionResolver.resolveException(request, response, null, ex);
        if (modelAndView == null) {
            ErrorLog.error(ErrorType.SYSTEM, request, "安全过滤器异常没有可用的解析器", ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=utf-8");
            try {
                PrintWriter out = response.getWriter();
                out.write(JSONObject.toJSONString(Result.failed(ErrorType.SYSTEM, "系统内部错误，请稍后重试")));
                out.flush();
                out.close();
            } catch (IOException e) {
                throw new AuthenticationServiceException("BeforeSecurityFilter 解析返回异常", e);
            }
        }
    }

}
