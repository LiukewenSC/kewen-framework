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
public class RequestLogger {
    protected String url;
    protected String method;
    protected String params;
    protected Object body;
    protected String ip;
    protected Map<String,String> headers;
    /**
     * 执行时间
     */
    protected int execMillisecond;
}