package com.kewen.framework.basic.logger.request;


import org.springframework.context.ApplicationEvent;

/**
 * 持久化请求事件发布，用于执行事件监听
 * @author kewen
 * @since 2024-08-29
 */
public class RequestLoggerEvent extends ApplicationEvent {

    RequestLogger requestLogger;

    public RequestLoggerEvent(RequestLogger requestLogger) {
        super(requestLogger);
        this.requestLogger = requestLogger;
    }

    public RequestLogger getRequestLogger() {
        return requestLogger;
    }
}
