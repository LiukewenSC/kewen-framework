
package com.cu.framework.auth.sample.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.cu.framework.auth.rbac.composite.SysUserComposite;
import com.cu.framework.auth.sample.AuthBootSampleCurrentUserContextHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthBootSampleConfig {

    @Bean
    PasswordEncoder passwordEncoder(){
       return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public AuthBootSampleCurrentUserContextHolder authBootSampleCurrentUserContextHolder(SysUserComposite sysUserComposite){
        AuthBootSampleCurrentUserContextHolder holder = new AuthBootSampleCurrentUserContextHolder();
        holder.setSysUserComposite(sysUserComposite);
        return holder;
    }
    /**
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.H2)); // 如果配置多个插件, 切记分页最后添加
        // 如果有多数据源可以不配具体类型, 否则都建议配上具体的 DbType
        return interceptor;
    }
}
