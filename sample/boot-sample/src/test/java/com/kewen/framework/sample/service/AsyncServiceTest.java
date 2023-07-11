package com.kewen.framework.sample.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class AsyncServiceTest {

    @Autowired
    AsyncService asyncService;

    @Test
    public void testAsync() {
        asyncService.testAsync();
        asyncService.testAsync();
        asyncService.testAsync();
        asyncService.testAsync();
        System.out.println("主线程完成");
    }
}