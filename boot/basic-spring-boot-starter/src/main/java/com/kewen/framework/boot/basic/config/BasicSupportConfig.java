package com.kewen.framework.boot.basic.config;


import com.kewen.framework.basic.support.message.FangTangMessageClient;
import com.kewen.framework.basic.support.log.message.FangTangRequestMessageListener;
import com.kewen.framework.basic.support.log.persistent.DatabasePersistentListener;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author kewen
 * @since 2024-08-29
 */
@Configuration
@ComponentScan("com.kewen.framework.basic.support")
@MapperScan("com.kewen.framework.basic.support.**.mapper")
public class BasicSupportConfig {


    @Bean(autowire = Autowire.BY_TYPE)
    @ConditionalOnProperty(name = "kewen.request.persistent.database", havingValue = "true")
    DatabasePersistentListener databasePersistentListener() {
        return new DatabasePersistentListener();
    }


    /**
     * 是否开启请求消息方糖渠道通知
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(name = "kewen.request.message.fang-tang", havingValue = "true")
    FangTangRequestMessageListener fangTangRequestMessageListener(FangTangMessageClient fangTangMessageClient) {
        FangTangRequestMessageListener listener = new FangTangRequestMessageListener();
        listener.setFangTangMessageClient(fangTangMessageClient);
        return listener;
    }

}
