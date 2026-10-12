package com.cu.framework.boot.magic.config;

import com.cu.framework.basic.model.Result;
import org.springframework.context.annotation.Configuration;
import org.ssssssss.magicapi.core.context.RequestEntity;
import org.ssssssss.magicapi.core.interceptor.ResultProvider;

/**
 * @author kewen
 * @since 2023-05-06
 */

@Configuration
public class MagicResultConfig implements ResultProvider {
    /**
     * 构建 magic-api 接口的统一返回结果，把状态码、消息、数据封装为本框架的 Result 对象
     * @param requestEntity 当前请求实体
     * @param code 状态码
     * @param message 提示信息
     * @param data 响应数据
     * @return 封装后的 Result 对象
     */
    @Override
    public Object buildResult(RequestEntity requestEntity, int code, String message, Object data) {
        Result<Object> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
}
