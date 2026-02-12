package com.kewen.framework.auth.sample.response;

import com.kewen.framework.basic.model.Result;;
import com.kewen.framework.auth.security.response.AuthenticationSuccessResultResolver;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 认证成功用户转化类，封装返回结构
 * @author kewen
 * @since 2024-07-05
 */
@Component
public class SampleResponseBodyResultResolver implements AuthenticationSuccessResultResolver {

    @Override
    public Object resolver(HttpServletRequest request, HttpServletResponse response,@Nullable Object data) {
        return Result.success(data);
    }
}
