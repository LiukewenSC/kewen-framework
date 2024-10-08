package com.kewen.framework.common.web.interceptor;

import com.kewen.framework.common.context.TraceContext;
import com.kewen.framework.common.core.utils.UUIDUtil;
import org.springframework.beans.factory.annotation.Value;
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
        TraceContext.set(UUIDUtil.generate());
    }

    @Override
    public void postHandle(WebRequest request, ModelMap model) throws Exception{

    }

    @Override
    public void afterCompletion(WebRequest request, Exception ex) throws Exception{
        TraceContext.clear();
    }
}
