package com.kewen.framework.boot.web.interceptor.trace;

import org.slf4j.MDC;

/**
 * @descrpition 交易流水号上下文
 * @author kewen
 * @since 2022-12-05 14:42
 */
public class TraceContext {

    public static final String TRACE_ID="traceId";

    public static String get(){
        return MDC.get(TRACE_ID);
    }
    public static void set(String traceId){
        MDC.put(TRACE_ID,traceId);
    }
    public static void clear(){
        MDC.clear();
    }
}
