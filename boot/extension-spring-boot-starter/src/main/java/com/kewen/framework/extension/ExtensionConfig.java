package com.kewen.framework.extension;


import com.kewen.framework.extension.request.DatabasePersistentListener;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * @author kewen
 * @since 2024-08-29
 */
@Configuration
@ComponentScan("com.kewen.framework.extension")
@MapperScan("com.kewen.framework.extension.**.mapper")
public class ExtensionConfig {


    @Bean(autowire = Autowire.BY_TYPE)
    @ConditionalOnProperty(name = "kewen.base.request.params-persistent",havingValue ="true")
    DatabasePersistentListener databasePersistentListener(){
        return new DatabasePersistentListener();
    }
}
