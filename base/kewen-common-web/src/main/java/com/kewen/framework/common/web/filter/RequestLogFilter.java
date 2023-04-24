package com.kewen.framework.common.web.filter;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 请求进入的过滤器，只要到应用的URL就打印
 *
 * @author liukewen
 * @since 2022/9/3
 */
@Slf4j
public class RequestLogFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String contentType = request.getContentType();
        // multipart/form-data; boundary=--------------------------806095705637677561579964
        if (StringUtils.isNotBlank(contentType) && org.springframework.util.StringUtils.startsWithIgnoreCase(contentType, "multipart/")) {
            return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String remoteAddr = request.getRemoteAddr();
        Map<String, String[]> parameterMap = request.getParameterMap();

        CacheRequestWrapper cacheRequestWrapper = new CacheRequestWrapper(request);
        byte[] bodyOnce = cacheRequestWrapper.getBodyOnce();
        //log.info("请求路径url为：{}，远程ip地址为{},param参数：{}，body参数：{}",
        JSON jsonObject = null;
        try {
            jsonObject = JSONObject.parseObject(bodyOnce);
        } catch (Exception e) {
            jsonObject = JSONObject.parseArray(new String(bodyOnce));
        }
        log.info("请求路径url为：{}，param参数：{}，body参数：{}",
                requestURI,
                //remoteAddr,
                JSON.toJSONString(parameterMap),
                jsonObject
        );
        filterChain.doFilter(cacheRequestWrapper, response);
    }
}