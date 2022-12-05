package com.kewen.framework.boot.web.filter;


import lombok.extern.slf4j.Slf4j;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 请求进入的过滤器，只要到应用的URL就打印
 *
 * @author liukewen
 * @since 2022/9/3
 */
@Slf4j
public class RequestLogInterceptor implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String remoteAddr = request.getRemoteAddr();
        log.info("--------------------请求参数--------------------------");
        if (request instanceof HttpServletRequest){
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            StringBuffer requestURL = httpServletRequest.getRequestURL();
            log.info("请求路径url为：{}",requestURL.toString());
        } else {
            log.info("非Http请求，地址为：{}:{}",request.getRemoteAddr(),request.getServerPort());
        }
        log.info("-----------------------------------------------------");
        chain.doFilter(request, response);
    }
}