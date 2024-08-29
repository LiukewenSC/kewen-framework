package com.kewen.framework.boot.basic.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
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
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * 配置了此线程池后spring就不会每次异步创建线程，
     * 若此处没有配置，spring则会先在容器中找线程池，
     *  若找到只有一个，则使用，
     *  若有多个，则必须在@Async中指定线程池的名字，否则报错
     *  若没有找到，则创建一个线程
     * @return
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setBeanName("asyncExecutor");
        //核心线程池数量，方法: 返回可用处理器的数量*2 + 1,，默认为io密集型。
        executor.setCorePoolSize(2 * Runtime.getRuntime().availableProcessors()+1);
        //最大线程数量
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors()*5);
        //线程池的队列容量
        executor.setQueueCapacity(Runtime.getRuntime().availableProcessors()*2);
        //线程名称的前缀
        executor.setThreadNamePrefix("this-excutor-");
        // setRejectedExecutionHandler：当pool已经达到max size的时候，如何处理新任务
        // CallerRunsPolicy：不在新线程中执行任务，而是由调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //初始化，好像也可以不要
        executor.initialize();
        return executor;
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
