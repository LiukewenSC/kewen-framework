package com.kewen.framework.sample.basic.service;

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

    @Async
    public void testAsync(){
        System.out.println("当前线程名："+Thread.currentThread().getName());
    }

}
