package com.cu.framework.boot.basic.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步线程池配置
 *
 * @author kewen
 * @since 2023-07-11
 */
@AutoConfiguration(before = TaskExecutionAutoConfiguration.class)
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * 延迟取用 applicationTaskExecutor，避免与 @EnableAsync 的早期初始化顺序耦合
     */
    @Autowired
    private ObjectProvider<ThreadPoolTaskExecutor> applicationTaskExecutor;

    /**
     * Spring Boot 4 在容器中已有 Executor 时会跳过自己的 TaskExecutor 与 AsyncConfigurer 包装层；
     * 这里显式提供 applicationTaskExecutor，既固定线程池参数，也避免出现两个 AsyncConfigurer
     * @return 异步线程池
     */
    @Bean(name = "applicationTaskExecutor")
    public ThreadPoolTaskExecutor applicationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程池数量，方法: 返回可用处理器的数量*2 + 1,，默认为io密集型。
        executor.setCorePoolSize(2 * Runtime.getRuntime().availableProcessors() + 1);
        //最大线程数量
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 5);
        //线程池的队列容量
        executor.setQueueCapacity(Runtime.getRuntime().availableProcessors() * 2);
        //线程名称的前缀
        executor.setThreadNamePrefix("this-excutor-");
        // setRejectedExecutionHandler：当pool已经达到max size的时候，如何处理新任务
        // CallerRunsPolicy：不在新线程中执行任务，而是由调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return applicationTaskExecutor.getObject();
    }

    /*异步任务中异常处理*/
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (Throwable ex, Method method, Object... params)->{
            System.out.println("class#method: " + method.getDeclaringClass().getName() + "#" + method.getName());
            System.out.println("type        : " + ex.getClass().getName());
            System.out.println("exception   : " + ex.getMessage());
        };
    }
}
