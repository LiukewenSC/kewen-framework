package com.cu.framework.sample.authcore.config;

import com.cu.framework.auth.core.AuthDataAdaptor;
import com.cu.framework.auth.core.data.AuthDataHandler;
import com.cu.framework.auth.core.data.JdbcAuthDataHandler;
import com.cu.framework.auth.core.data.JdbcAuthDataPersistent;
import com.cu.framework.sample.authcore.context.DemoAuthContextHolder;
import com.cu.framework.sample.authcore.context.DemoMenuAuthHandler;
import com.cu.framework.sample.authcore.mapper.DemoMenuApiMapper;
import com.cu.framework.sample.authcore.mapper.DemoMenuAuthMapper;
import com.cu.framework.sample.authcore.mapper.DemoUserAuthMapper;
import com.cu.framework.sample.authcore.store.DemoMenuApiStore;
import com.cu.framework.sample.authcore.trace.DemoAuthDataHandlerProbe;
import com.cu.framework.sample.authcore.trace.DemoSqlTrace;
import com.cu.framework.sample.authcore.trace.DemoSqlTraceInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 示例自己交付的扩展点与观察组件。
 * <p>
 * auth-core 只给接缝，不给实现：
 * <ul>
 *   <li>{@link DemoAuthContextHolder} —— 强制，缺了启动就失败（当前用户上下文）</li>
 *   <li>{@link DemoMenuAuthHandler} —— 强制，缺了启动就失败（菜单权限规则）</li>
 *   <li>{@link DemoMenuApiStore} —— 可选，缺了只记一条"菜单注册被跳过"的日志</li>
 *   <li>{@link AuthDataAdaptor} —— 由本示例声明；core 的自动配置里没有它，
 *       它是 core 自己的类却只在权限模型层被装配，纯 core 接入要自己注册</li>
 * </ul>
 * 三个扩展点全部指向示例自有的表，不引用任何权限模型层的代码或数据表
 *
 * @author kewen
 * @since 2026-10-20
 */
@Configuration
public class DemoAuthConfig {

    /**
     * 扩展点一：当前用户的权限从哪来
     */
    @Bean
    DemoAuthContextHolder authCurrentUserContextHolder(DemoUserAuthMapper demoUserAuthMapper) {
        return new DemoAuthContextHolder(demoUserAuthMapper);
    }

    /**
     * 扩展点二：某个请求路径需要哪种权限，由示例定规则
     */
    @Bean
    DemoMenuAuthHandler authMenuHandler(DemoMenuAuthMapper demoMenuAuthMapper) {
        return new DemoMenuAuthHandler(demoMenuAuthMapper);
    }

    /**
     * 扩展点三：菜单行存到哪、主键怎么来
     */
    @Bean
    DemoMenuApiStore menuApiService(DemoMenuApiMapper demoMenuApiMapper) {
        return new DemoMenuApiStore(demoMenuApiMapper);
    }

    /**
     * Service 层直接操作数据权限的入口（不依赖注解的那条路）
     */
    @Bean
    AuthDataAdaptor authDataAdaptor(AuthDataHandler authDataHandler) {
        return new AuthDataAdaptor(authDataHandler);
    }

    /**
     * 观察台：按追踪标识记录实际下发的语句与真实发生的判定
     */
    @Bean
    DemoSqlTrace demoSqlTrace() {
        return new DemoSqlTrace();
    }

    /**
     * 包住框架处理器，只为把"判定确实读了权限表"变成可断言的事实；判定逻辑本身一行不改。
     * <p>
     * 框架的 {@code jdbcAuthDataHandler} 带 {@code @ConditionalOnMissingBean(AuthDataHandler.class)}，
     * 而本探针就是一个 {@code AuthDataHandler}，所以框架默认实现会主动让位——
     * 被代理的对象因此由本方法用持久化组件自己构造，而不是从容器里取。
     * 这条让位规则本身就是 auth/starter-assembly 要演的行为
     */
    @Bean
    @Primary
    DemoAuthDataHandlerProbe demoAuthDataHandlerProbe(JdbcAuthDataPersistent jdbcAuthDataPersistent,
                                                      DemoSqlTrace demoSqlTrace) {
        return new DemoAuthDataHandlerProbe(new JdbcAuthDataHandler(jdbcAuthDataPersistent), demoSqlTrace);
    }

    /**
     * 语句观察插件。与鉴权无关，只负责把改写后的最终语句留档；
     * 它同时证明多插件共存时框架的解包逻辑仍然识别得出语句身份
     */
    @Bean
    DemoSqlTraceInterceptor demoSqlTraceInterceptor(DemoSqlTrace demoSqlTrace) {
        DemoSqlTraceInterceptor interceptor = new DemoSqlTraceInterceptor();
        interceptor.setDemoSqlTrace(demoSqlTrace);
        return interceptor;
    }
}
