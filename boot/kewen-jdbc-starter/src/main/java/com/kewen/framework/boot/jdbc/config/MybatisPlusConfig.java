package com.kewen.framework.boot.jdbc.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.kewen.framework.common.context.TenantContext;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 *  mybatis plus 的扩展配置
 * @author kewen
 * @since 2022-12-05
 */
@Configuration
public class MybatisPlusConfig {

    private DbTenantProperties dbTenantProperties;


    public MybatisPlusConfig(ObjectProvider<DbTenantProperties> dbTenantObjectProvider) {
        DbTenantProperties ifAvailable = dbTenantObjectProvider.getIfAvailable();
        if (ifAvailable !=null){
            dbTenantProperties = ifAvailable;
        }
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        //分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());

        //租户插件
        if (TenantContext.support()){
            interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
                @Override
                public Expression getTenantId() {
                    Long tenantId = TenantContext.get();
                    return tenantId==null?new LongValue(100001):new LongValue(tenantId);
                }

                @Override
                public String getTenantIdColumn() {
                    return dbTenantProperties.getTenantIdColumn();
                }

                @Override
                public boolean ignoreTable(String tableName) {
                    List<String> ignorePrefixTables = dbTenantProperties.getIgnorePrefixTables();
                    if (CollectionUtils.isEmpty(ignorePrefixTables)){
                        return false;
                    }
                    return ignorePrefixTables.stream().anyMatch(tableName::startsWith);
                }
            }));
        }
        return interceptor;
    }
}
