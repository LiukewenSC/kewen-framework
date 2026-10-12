package com.cu.framework.sample.basic.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试异步controller
 *
 * @author kewen
 * @since 2023-07-11
 */
@Service
public class AsyncService {

    /**
     * 异步方法示例，通过 @Async 在异步线程中执行，打印当前线程名以验证未走调用方线程
     */
    @Async
    public void testAsync(){
        System.out.println("当前线程名："+Thread.currentThread().getName());
    }

}
