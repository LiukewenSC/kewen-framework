package com.kewen.framework.basic.logger.trace;

import com.kewen.framework.basic.model.Result;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 交易流水号加入 ，最后加入，因此放在最后
 * @author kewen
 * @since 2023-04-28
 */
@RestControllerAdvice
@Order(9000)
public class TraceResponseBodyAdvice implements ResponseBodyAdvice<Result> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return Result.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Result beforeBodyWrite(Result body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        return TraceResult.of(body, TraceContext.get());
    }

}
