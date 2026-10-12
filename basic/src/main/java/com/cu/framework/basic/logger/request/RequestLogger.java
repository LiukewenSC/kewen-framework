package com.cu.framework.basic.logger.request;

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
    /** 请求路径（URI） */
    protected String url;
    /** 请求方法，如 GET、POST */
    protected String method;
    /** query 参数序列化后的 JSON 字符串，无参数时为 null */
    protected String params;
    /** 请求体 JSON 解析后的对象（一般为 Map），无 body 或不支持解析时为 null */
    protected Object body;
    /** 客户端 IP */
    protected String ip;
    /** 全部请求头，key 为请求头名称 */
    protected Map<String,String> headers;
    /**
     * 执行时间
     */
    protected int execMillisecond;
}