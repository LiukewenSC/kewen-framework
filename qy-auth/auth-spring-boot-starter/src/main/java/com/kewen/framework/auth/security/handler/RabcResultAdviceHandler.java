package com.kewen.framework.auth.security.handler;


import com.kewen.framework.auth.rabc.model.RabcResult;
import com.kewen.framework.basic.model.Result;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 
 * @author kewen
 * @since 2024-08-29
 */
public class RabcResultAdviceHandler implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getParameterType().isAssignableFrom(RabcResult.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        RabcResult rabcResult = (RabcResult) body;
        Result<Object> result = new Result<>();
        result.setCode(rabcResult.getCode());
        result.setMessage(rabcResult.getMessage());
        result.setData(rabcResult.getData());
        return result;
    }
}
