package com.kewen.framework.common.logger;

import com.kewen.framework.common.logger.model.RequestInfo;

/**
 *
 * 持久化请求处理器
 * @author kewen
 * @since 2023-04-26
 */
public interface PersistentRequestStore {
    void save(RequestInfo requestInfo, Integer millisecond);
}
