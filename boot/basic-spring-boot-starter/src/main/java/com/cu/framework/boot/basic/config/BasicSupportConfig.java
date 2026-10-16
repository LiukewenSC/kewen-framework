package com.cu.framework.boot.basic.config;


import com.cu.framework.basic.support.notify.fangtang.FangTangMessageClient;
import com.cu.framework.basic.support.log.RequestLogFangTangMessageListener;
import com.cu.framework.basic.support.log.RequestLogDatabasePersistentListener;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author kewen
 * @since 2024-08-29
 */
@Configuration
@ComponentScan("com.cu.framework.basic.support")
@MapperScan("com.cu.framework.basic.support.**.mapper")
public class BasicSupportConfig {


    @Bean
    @ConditionalOnProperty(name = "cu.request.persistent.database", havingValue = "true")
    RequestLogDatabasePersistentListener databasePersistentListener() {
        return new RequestLogDatabasePersistentListener();
    }


    /**
     * 是否开启请求消息方糖渠道通知
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(name = {"cu.request.message.fang-tang", "cu.notify.fang-tang.enabled"}, havingValue = "true")
    @ConditionalOnBean(FangTangMessageClient.class)
    RequestLogFangTangMessageListener fangTangRequestMessageListener(FangTangMessageClient fangTangMessageClient) {
        RequestLogFangTangMessageListener listener = new RequestLogFangTangMessageListener();
        listener.setFangTangMessageClient(fangTangMessageClient);
        return listener;
    }

}
