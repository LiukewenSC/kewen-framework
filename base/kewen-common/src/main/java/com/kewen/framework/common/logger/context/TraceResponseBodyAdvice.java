package com.kewen.framework.common.logger.context;

import com.kewen.framework.common.core.model.Result;
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
        //坑爹的fastjson
        TraceResult result = new TraceResult();
        result.setCode(body.getCode());
        result.setMessage(body.getMessage());
        result.setSuccess(body.getSuccess());
        result.setData(body.getData());
        result.setTraceId(TraceContext.get());
        return result;
    }

    private static class TraceResult extends Result {
        private String traceId;

        public String getTraceId() {
            return traceId;
        }

        public void setTraceId(String traceId) {
            this.traceId = traceId;
        }
    }
}
