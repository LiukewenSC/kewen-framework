package com.kewen.framework.common.web.support;

import com.kewen.framework.common.context.TraceContext;
import com.kewen.framework.common.core.model.Result;
import com.kewen.framework.common.core.utils.BeanUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 交易流水号加入 ，最后加入，因此放在最后
 * @author kewen
 * @since 2023-04-28
 */
@ControllerAdvice
@Order(9000)
public class TrackingLogResponseBodyAdvice implements ResponseBodyAdvice<Result> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return Result.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Result beforeBodyWrite(Result body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        TraceResult result = BeanUtil.toBean(body, TraceResult.class);
        //TraceResult result = new TraceResult();

        result.setTraceId(TraceContext.get());
        return result;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    static class TraceResult extends Result {
        private String traceId;
    }
}
