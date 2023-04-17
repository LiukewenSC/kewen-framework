package com.kewen.framework.common.web.listener;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * @descrpition 
 * @author kewen
 * @since 2023-01-03 17:06
 */
public class TestApplicationListener implements ApplicationListener {

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {

        applicationEvent.toString();
    }
}
