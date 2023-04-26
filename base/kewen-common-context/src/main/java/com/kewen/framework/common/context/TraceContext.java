package com.kewen.framework.common.context;

import org.slf4j.MDC;

/**
 * @descrpition 交易流水号上下文
 * @author kewen
 * @since 2022-12-05 14:42
 */
public class TraceContext {

    public static String get(){
        return MDC.get(ContextConstant.TRACE_ID_KEY);
    }
    public static void set(String traceId){
        MDC.put(ContextConstant.TRACE_ID_KEY,traceId);
    }
    public static void remove(String traceId){
        MDC.remove(traceId);
    }
}
