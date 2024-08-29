package com.kewen.framework.basic.logger;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.kewen.framework.basic.logger.request.BodyHttpServletRequest;
import com.kewen.framework.basic.logger.request.PersistentRequestEvent;
import com.kewen.framework.basic.logger.request.RequestInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 持久化日志过滤器
 * @author kewen
 * @since 2023-04-26
 */
@Order(2)
@Slf4j
public class PersistentRequestFilter extends OncePerRequestFilter {

    ApplicationEventPublisher applicationEventPublisher;

    public PersistentRequestFilter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        Map<String, String> headers = parseHeaders(request);

        String params = parsParams(request);

        BodyHttpServletRequest innerRequest = new BodyHttpServletRequest(request);

        Object body = parseBody(innerRequest);

        RequestInfo requestInfo = new RequestInfo()
                .setUrl(innerRequest.getRequestURI())
                .setMethod(innerRequest.getMethod())
                .setParams(params)
                .setBody(body)
                .setIp(innerRequest.getRemoteAddr())
                .setHeaders(headers)
                ;

        log.info("请求ip:{},请求路径:{},请求方法{},请求params参数:{},请求body参数:{}",
                requestInfo.getIp(),
                requestInfo.getUrl(),
                requestInfo.getMethod(),
                requestInfo.getParams(),
                requestInfo.getBody()
        );

        //记录请求和时长
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(innerRequest,response);
        } finally {
            long end = System.currentTimeMillis();
            int millisecond = (int) (end - start);
            requestInfo.setExecMillisecond(millisecond);

            //发布请求持久化事件
            applicationEventPublisher.publishEvent(new PersistentRequestEvent(requestInfo));
        }
    }

    /**
     * 解析请求头
     * @param request
     * @return
     */
    private Map<String, String> parseHeaders(HttpServletRequest request){
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String element = headerNames.nextElement();
            headers.put(element,request.getHeader(element));
        }
        return headers;
    }

    /**
     * 解析param，将所有的param数组解析成字符串
     * @param request
     * @return
     */
    private String parsParams(HttpServletRequest request){
        Map<String, String[]> parameterMaps = request.getParameterMap();
        HashMap<Object, Object> parameterMap = new HashMap<>(parameterMaps.size());
        for (Map.Entry<String, String[]> entry : parameterMaps.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            if (values.length==1){
                parameterMap.put(key,values[0]);
            } else {
                parameterMap.put(key,values);
            }
        }
        return parameterMap.isEmpty()?null:JSONObject.toJSONString(parameterMap);
    }

    /**
     * 将body参数解析成object， 一般为map
     * @param request
     * @return
     */
    private Object parseBody(BodyHttpServletRequest request){
        byte[] body = request.getBody();
        return body==null? null:JSON.parse(body);
    }
}