package com.kewen.framework.datasource.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.kewen.framework.datasource.context.DbTenant;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @descrpition 
 * @author kewen
 * @since 2022-12-05 16:00
 */
@Configuration
@MapperScan(basePackages = "com.kewen.framework.boot.authority.mapper")
public class MybatisPlusConfig {

    @Autowired
    private DbTenant dbTenant;


    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        //分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        //租户插件
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                Long tenantId = dbTenant.getTenantId();
                return tenantId==null?new LongValue(100001):new LongValue(tenantId);
            }

            @Override
            public String getTenantIdColumn() {
                return dbTenant.getTenantIdColumn();
            }

            @Override
            public boolean ignoreTable(String tableName) {
                List<String> ignorePrefixTables = dbTenant.ignorePrefixTables();
                if (CollectionUtils.isEmpty(ignorePrefixTables)){
                    return false;
                }
                return ignorePrefixTables.stream().anyMatch(tableName::startsWith);
            }
        }));
        return interceptor;
    }
}
