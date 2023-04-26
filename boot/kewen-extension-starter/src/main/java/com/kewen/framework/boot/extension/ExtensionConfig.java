package com.kewen.framework.boot.extension;

import com.kewen.framework.extension.DatabaseRequestParamPersistent;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * @author kewen
 * @since 2023-04-26
 */
@Configuration
@ComponentScan("com.kewen.framework.extension.mp")
@MapperScan("com.kewen.framework.extension.mp.mapper")
public class ExtensionConfig {

    /**
     * 请求参数数据库持久化
     * @return
     */
    @Bean
    @ConditionalOnProperty(name = "kewen.base.request.params-persistent",havingValue ="true")
    DatabaseRequestParamPersistent databaseRequestParamPersistent(){
        return new DatabaseRequestParamPersistent();
    }

}
