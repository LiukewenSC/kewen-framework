package com.kewen.framework.jdbc.config;

import com.kewen.framework.common.context.UserContext;
import com.kewen.framework.jdbc.context.DbTenant;
import com.kewen.framework.jdbc.context.DefaultDbTenant;
import com.kewen.framework.jdbc.plug.AutoFillConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
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
    @ConditionalOnExpression("${kewen.base.tenant.open:true}")
    @ConditionalOnMissingBean(DbTenant.class)
    public DbTenant dbTenant(){
        return new DefaultDbTenant();
    }


    /**
     * 自动填充插件
     * @return
     */
    @Bean
    public AutoFillConfig globalFillConfig(){
        return new AutoFillConfig(UserContext.support());
    }
}
