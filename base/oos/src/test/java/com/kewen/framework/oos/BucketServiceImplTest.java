package com.kewen.framework.oos;

import com.qiniu.common.QiniuException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
public class BucketServiceImplTest {

    @Autowired
    BucketService bucketService;

    @Test
    public void createBucket() throws QiniuException {
        bucketService.createBucket("dfasfasd");
    }
}