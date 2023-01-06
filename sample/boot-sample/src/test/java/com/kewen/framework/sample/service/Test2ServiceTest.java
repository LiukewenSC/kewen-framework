package com.kewen.framework.sample.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class Test2ServiceTest {

    Test2Service test2Service;

    @Test
    public void t(){
        test2Service.hello();
    }

}