package com.kewen.framework.common.web.filter.support;

import org.springframework.lang.Nullable;

/**
 *
 * 持久化请求处理器
 * @author kewen
 * @since 2023-04-26
 */
public interface RequestParamPersistentHandler {
    void save(String url, String method, String remoteAddr, @Nullable String params, @Nullable String body, Integer millisecond);
}
