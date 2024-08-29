package com.kewen.framework.basic.logger.request;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 
 * @author kewen
 * @since 2023-04-26
 */
@Data
@Accessors(chain = true)
public class RequestInfo {
    private String url;
    private String method;
    private String params;
    private Object body;
    private String ip;
    private Map<String,String> headers;
    /**
     * 执行时间
     */
    private int execMillisecond;
}