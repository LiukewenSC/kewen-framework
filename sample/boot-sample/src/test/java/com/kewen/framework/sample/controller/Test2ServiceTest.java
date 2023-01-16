package com.kewen.framework.sample.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class Test2ServiceTest {

    @Autowired
    Test2Service test2Service;

    @Test
    public void t(){
        test2Service.hello();
    }

}