package com.kewen.framework.boot.web.interceptor.trace;

import com.kewen.common.utils.UUIDUtil;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

/**
 * @descrpition 交易流水号拦截器
 * @author kewen
 * @since 2022-12-05 10:29
 */
public class TraceInterceptor implements WebRequestInterceptor {

    @Override
    public void preHandle(WebRequest request) throws Exception{
        MDC.put("trace", UUIDUtil.generate());
    }

    @Override
    public void postHandle(WebRequest request, ModelMap model) throws Exception{

    }

    @Override
    public void afterCompletion(WebRequest request, Exception ex) throws Exception{
        MDC.clear();
    }
}