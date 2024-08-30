package com.kewen.framework.auth.handler;


import com.kewen.framework.auth.security.response.AuthenticationSuccessResultResolver;
import com.kewen.framework.basic.logger.trace.TraceContext;
import com.kewen.framework.basic.logger.trace.TraceResult;
import com.kewen.framework.basic.model.Result;
import org.springframework.lang.Nullable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 认证成功用户转化类，封装返回结构
 * @author kewen
 * @since 2024-07-05
 */
public class AuthResponseBodyResultResolver implements AuthenticationSuccessResultResolver {

    @Override
    public Object resolver(HttpServletRequest request, HttpServletResponse response,@Nullable Object data) {
        Result<Object> success = Result.success(data);
        return TraceResult.of(success, TraceContext.get());
    }
}
