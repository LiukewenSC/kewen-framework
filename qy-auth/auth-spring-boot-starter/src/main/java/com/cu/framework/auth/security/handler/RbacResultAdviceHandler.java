package com.cu.framework.auth.security.handler;


import com.cu.framework.auth.rbac.model.RbacResult;
import com.cu.framework.basic.model.Result;
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
public class RbacResultAdviceHandler implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.getParameterType().isAssignableFrom(RbacResult.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        RbacResult rbacResult = (RbacResult) body;
        Result<Object> result = new Result<>();
        result.setCode(rbacResult.getCode());
        result.setMessage(rbacResult.getMessage());
        result.setData(rbacResult.getData());
        return result;
    }
}
