package com.kewen.framework.boot.magic.config;

import com.kewen.framework.basic.model.Result;
import org.springframework.context.annotation.Configuration;
import org.ssssssss.magicapi.core.context.RequestEntity;
import org.ssssssss.magicapi.core.interceptor.ResultProvider;

/**
 * @author kewen
 * @since 2023-05-06
 */

@Configuration
public class MagicResultConfig implements ResultProvider {
    @Override
    public Object buildResult(RequestEntity requestEntity, int code, String message, Object data) {
        Result<Object> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
}
