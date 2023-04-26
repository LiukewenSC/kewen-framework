package com.kewen.framework.common.web.filter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.kewen.framework.common.web.context.RequestInfo;
import com.kewen.framework.common.web.context.RequestInfoContext;
import com.kewen.framework.common.web.filter.model.BodyParsedHttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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
 * 
 * @author kewen
 * @since 2023-04-26
 */
@Order(-8000)
@Slf4j
public class RequestInfoFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        BodyParsedHttpServletRequest req = new BodyParsedHttpServletRequest(request);

        Map<String, String> headers = parseHeaders(req);

        String params = parsParams(req);

        Object body = parseBody(req);


        RequestInfo requestInfo = new RequestInfo()
                .setUrl(req.getRequestURI())
                .setMethod(req.getMethod())
                .setParams(params)
                .setBody(body)
                .setIp(req.getRemoteAddr())
                .setHeaders(headers)
                ;

        log.info("请求信息{}",JSONObject.toJSONString(requestInfo));

        RequestInfoContext.set(requestInfo);
        try {
            filterChain.doFilter(req,response);
        } finally {
            RequestInfoContext.clear();
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
    private Object parseBody(BodyParsedHttpServletRequest request){
        byte[] body = request.getBody();
        return body==null? null:JSON.parse(body);
    }
}