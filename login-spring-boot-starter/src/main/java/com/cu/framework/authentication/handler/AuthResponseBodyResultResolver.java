package com.cu.framework.authentication.handler;


import com.cu.framework.authentication.response.AuthenticationSuccessResultResolver;
import com.cu.framework.basic.logger.trace.TraceContext;
import com.cu.framework.basic.logger.trace.TraceResult;
import com.cu.framework.basic.model.Result;
import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 认证成功用户转化类，封装返回结构
 * @author kewen
 * @since 2024-07-05
 */
public class AuthResponseBodyResultResolver implements AuthenticationSuccessResultResolver {

    /**
     * 将数据包装为 Result.success，并附带当前 TraceContext 链路信息后返回
     */
    @Override
    public Object resolver(HttpServletRequest request, HttpServletResponse response,@Nullable Object data) {
        Result<Object> success = Result.success(data);
        return TraceResult.of(success, TraceContext.get());
    }
}
