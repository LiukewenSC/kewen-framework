package com.cu.framework.boot.basic.config;


import com.cu.framework.basic.support.message.FangTangMessageClient;
import com.cu.framework.boot.basic.properties.FangTangProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 *  方糖消息通知
 * @author kewen
 * @since 2024-08-30
 */
@Configuration
@EnableConfigurationProperties(FangTangProperties.class)
public class FangTangMessageConfig {


    /**
     * 方糖客户端
     * @return
     */
    @Bean
    @ConditionalOnProperty(prefix = "cu.message.fang-tang", name = "enabled", havingValue = "true")
    FangTangMessageClient fangTangMessageClient(FangTangProperties fangTangProperties){

        if (!StringUtils.hasText(fangTangProperties.getKey())){
            throw new RuntimeException("方糖消息通知配置错误，key不能为空");
        }
        if (!StringUtils.hasText(fangTangProperties.getDomain())){
            throw new RuntimeException("方糖消息通知配置错误，domain不能为空");
        }

        FangTangMessageClient messageClient = new FangTangMessageClient();

        messageClient.setKey(fangTangProperties.getKey());
        messageClient.setDomain(fangTangProperties.getDomain());

        return messageClient;
    }
}
