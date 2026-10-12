package com.cu.framework.basic.logger.trace;

import com.cu.framework.basic.model.Result;
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
    /**
     * 仅拦截返回值类型为 Result 及其子类的接口
     * @param returnType 控制器方法返回类型
     * @param converterType 消息转换器类型
     * @return 返回类型可赋值给 Result 时为 true
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return Result.class.isAssignableFrom(returnType.getParameterType());
    }

    /**
     * 响应体写出前，将 Result 包装为携带当前 TraceContext 流水号的 TraceResult
     * @param body 控制器返回的原始 Result
     * @return 附带 traceId 的 TraceResult
     */
    @Override
    public Result beforeBodyWrite(Result body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        return TraceResult.of(body, TraceContext.get());
    }

}
