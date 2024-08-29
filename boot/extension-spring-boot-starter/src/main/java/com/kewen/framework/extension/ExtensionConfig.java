package com.kewen.framework.extension;


import com.kewen.framework.extension.request.message.FangTangRequestMessageListener;
import com.kewen.framework.extension.request.persistent.DatabasePersistentListener;
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
@ComponentScan("com.kewen.framework.extension")
@MapperScan("com.kewen.framework.extension.**.mapper")
public class ExtensionConfig {


    @Bean(autowire = Autowire.BY_TYPE)
    @ConditionalOnProperty(name = "kewen.extension.request.persistent.database", havingValue = "true")
    DatabasePersistentListener databasePersistentListener() {
        return new DatabasePersistentListener();
    }


    /**
     * 是否开启请求消息方糖渠道通知
     *
     * @return
     */
    @Bean(autowire = Autowire.BY_TYPE)
    @ConditionalOnProperty(name = "kewen.extension.request.message.fangtang", havingValue = "true")
    FangTangRequestMessageListener fangTangRequestMessageListener() {
        return new FangTangRequestMessageListener();
    }

}
