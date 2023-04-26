package com.kewen.framework.common.web.context;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
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
}