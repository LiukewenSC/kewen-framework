package com.cu.framework.sample;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 示例自己的配置。
 * <p>
 * 这里刻意不声明 {@code SecurityUserDetailsService}、{@code AuthCurrentUserContextHolder} 或
 * {@code PasswordEncoder}：三者一旦成为容器 Bean，starter 的默认实现就会按
 * {@code @ConditionalOnMissingBean} 让位，登录身份便不再参与 RBAC 判权。
 *
 * @author kewen
 * @since 2026-10-15
 */
@Configuration
public class AuthRbacSecurityBootSampleConfig {

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
