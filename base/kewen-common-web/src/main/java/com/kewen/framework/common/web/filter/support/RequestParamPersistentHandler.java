package com.kewen.framework.common.web.filter.support;

import com.kewen.framework.common.web.context.RequestInfo;

/**
 *
 * 持久化请求处理器
 * @author kewen
 * @since 2023-04-26
 */
public interface RequestParamPersistentHandler {
    void save(RequestInfo requestInfo,Integer millisecond);
}
