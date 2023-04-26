package com.kewen.framework.common.web.filter;

import com.alibaba.fastjson2.JSON;
import com.kewen.framework.common.core.utils.StringUtils;
import com.kewen.framework.common.web.filter.model.BodyParsedHttpServletRequest;
import com.kewen.framework.common.web.filter.support.RequestParamPersistentHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 请求持久化过滤器，要保证在security之后，才能取到上下文
 * @author kewen
 * @since 2023-04-26
 */
@Order(1)
@Slf4j
public class RequestPersistanceFilter extends OncePerRequestFilter {
    List<RequestParamPersistentHandler> persistenceList;
    public RequestPersistanceFilter(ObjectProvider<RequestParamPersistentHandler> objectProvider) {
        persistenceList = objectProvider.orderedStream().collect(Collectors.toList());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        String url = request.getRequestURI();
        String remoteAddr = request.getRemoteAddr();
        String method = request.getMethod();
        String params = JSON.toJSONString(request.getParameterMap());
        String body = null;
        if (request instanceof BodyParsedHttpServletRequest){
            byte[] bytes = ((BodyParsedHttpServletRequest) request).getBody();
            body = StringUtils.strCompact(bytes);
        }

        try {
            filterChain.doFilter(request,response);
        } finally {
            long end = System.currentTimeMillis();
            int millisecond = (int) (end - start);
            for (RequestParamPersistentHandler persistent : persistenceList) {
                try {
                    persistent.save(url,method,remoteAddr,params,body,millisecond);
                } catch (Throwable e) {
                    log.error(persistent.getClass().getName()+"持久化日志失败",e);
                }
            }
        }



    }
}
