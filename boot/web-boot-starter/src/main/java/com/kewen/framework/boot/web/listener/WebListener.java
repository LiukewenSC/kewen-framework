package com.kewen.framework.boot.web.listener;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * @descrpition 
 * @author kewen
 * @since 2023-01-03 17:06
 */
public class WebListener implements ApplicationListener {

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {

        applicationEvent.toString();
    }
}
