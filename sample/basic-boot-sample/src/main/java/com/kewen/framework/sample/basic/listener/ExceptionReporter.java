package com.kewen.framework.sample.basic.listener;

import org.springframework.boot.SpringBootExceptionReporter;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;

public class ExceptionReporter implements SpringBootExceptionReporter {


    ExceptionReporter(ConfigurableApplicationContext context) {
        this(context, null);
    }

    ExceptionReporter(ConfigurableApplicationContext context, ClassLoader classLoader) {
    }

    @Override
    public boolean reportException(Throwable failure) {

        return false;
    }
}
