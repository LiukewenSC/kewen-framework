package com.cu.framework.sample.basic.listener;

import org.springframework.boot.SpringBootExceptionReporter;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;

/**
 * SpringBootExceptionReporter 扩展点示例，通过 spring.factories 注册，
 * 演示应用启动失败时自定义异常上报器的接入方式（本例不做任何处理）
 * @author kewen
 * @since 2024-08-29
 */
public class ExceptionReporter implements SpringBootExceptionReporter {


    ExceptionReporter(ConfigurableApplicationContext context) {
        this(context, null);
    }

    ExceptionReporter(ConfigurableApplicationContext context, ClassLoader classLoader) {
    }

    /**
     * 上报启动异常，本示例不做任何处理
     * @param failure 启动过程中抛出的异常
     * @return 固定返回 false，表示异常未被本上报器处理，Spring Boot 继续交给后续上报器
     */
    @Override
    public boolean reportException(Throwable failure) {

        return false;
    }
}
