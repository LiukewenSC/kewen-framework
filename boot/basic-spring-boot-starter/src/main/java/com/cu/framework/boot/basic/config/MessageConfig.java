package com.cu.framework.boot.basic.config;


import com.cu.framework.basic.support.message.FangTangMessageClient;
import com.cu.framework.boot.basic.properties.FangTangProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * @author kewen
 * @since 2024-08-30
 */
@Configuration
@EnableConfigurationProperties(FangTangProperties.class)
public class MessageConfig {


    /**
     * 方糖客户端
     * @return
     */
    @Bean
    FangTangMessageClient fangTangMessageClient(FangTangProperties fangTangProperties){

        FangTangMessageClient messageClient = new FangTangMessageClient();

        messageClient.setKey(fangTangProperties.getKey());
        messageClient.setDomain(fangTangProperties.getDomain());

        return messageClient;
    }
}
