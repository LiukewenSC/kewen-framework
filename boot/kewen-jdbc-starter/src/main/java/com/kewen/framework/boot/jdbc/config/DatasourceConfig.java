package com.kewen.framework.boot.jdbc.config;

import com.kewen.framework.common.context.UserContext;
import com.kewen.framework.boot.jdbc.plug.AutoFillConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @descrpition 
 * @author kewen
 * @since 2022-12-05
 */
@Configuration
@PropertySource({"classpath:application-datasource.yml", "classpath:application-datasource-${spring.profiles.active}.yml"})
@EnableConfigurationProperties(DbTenantProperties.class)
public class DatasourceConfig {


    /**
     * 自动填充插件
     * @return
     */
    @Bean
    public AutoFillConfig globalFillConfig(){
        return new AutoFillConfig(UserContext.support());
    }
}
