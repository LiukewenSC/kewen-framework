package com.kewen.framework.common.web.filter;


import com.alibaba.fastjson2.JSON;
import com.kewen.framework.common.core.utils.StringUtils;
import com.kewen.framework.common.web.filter.model.BodyParsedHttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 请求日志处理器，文件上传不执行此过滤器
 *
 * @author liukewen
 * @since 2022/9/3
 */
@Slf4j
@Order(-9998)
public class RequestPrintFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        BodyParsedHttpServletRequest handlerRequest = new BodyParsedHttpServletRequest(request);

        String url = request.getRequestURI();
        String method = request.getMethod();
        String ip = request.getRemoteAddr();
        Map<String, String[]> parameterMap = request.getParameterMap();
        //log.info("请求路径url为：{}，远程ip地址为{},param参数：{}，body参数：{}",
        // StringUtils.toEncodedString()
        log.info("请求参数-> url{},method:{},ip:{},param:{}，body:{}",
                url,
                method,
                ip,
                JSON.toJSONString(parameterMap),
                StringUtils.strCompact(handlerRequest.getBody())
        );
        filterChain.doFilter(handlerRequest, response);
    }
}