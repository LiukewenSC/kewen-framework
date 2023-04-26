package com.kewen.framework.common.web.context;

/**
 *  请求信息上下文
 * @author kewen
 * @since 2023-04-26
 */
public class RequestInfoContext {

    private static final ThreadLocal<RequestInfo> context = new InheritableThreadLocal<>();

    public static void set(RequestInfo requestInfo){
        context.set(requestInfo);
    }
    public static RequestInfo get(){
        return context.get();
    }
    public static void clear(){
        context.remove();
    }

}