package com.kewen.framework.datasource.config;

import com.kewen.framework.datasource.context.DbCurrentUser;
import com.kewen.framework.datasource.context.DbTenant;
import com.kewen.framework.datasource.context.DefaultDbCurrentUser;
import com.kewen.framework.datasource.context.DefaultDbTenant;
import com.kewen.framework.datasource.plug.AutoFillConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @descrpition 
 * @author kewen
 * @since 2022-12-05 16:03
 */
@Configuration
public class DatasourceBeanLoadConfig {

    /**
     * 默认租户处理器
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(DbTenant.class)
    public DbTenant dbTenant(){
        return new DefaultDbTenant();
    }

    //todo @Bean 自动填充插件待测试，目前会与druid冲突
    @Bean
    public AutoFillConfig globalFillConfig(DbCurrentUser dbCurrentUser){
        return new AutoFillConfig(dbCurrentUser);
    }
    @Bean
    @ConditionalOnMissingBean(DbCurrentUser.class)
    public DbCurrentUser dbCurrentUser(){
        return new DefaultDbCurrentUser();
    }
}
