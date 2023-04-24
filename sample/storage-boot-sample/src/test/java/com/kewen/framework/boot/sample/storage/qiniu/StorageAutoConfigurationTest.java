package com.kewen.framework.boot.sample.storage.qiniu;


import com.kewen.framework.storage.core.StorageTemplate;
import com.kewen.framework.storage.core.model.UploadBO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
public class StorageAutoConfigurationTest {

    @Autowired
    StorageTemplate storageTemplate;


    @Test
    public void storageTemplate() throws IOException {
        UploadBO upload = storageTemplate.upload(
                Files.newInputStream(Paths.get("E:\\download\\Adguard规则.txt")),
                "fadsfasdfasdfgafdasd.txt",
                "text/plain"
        );
        log.info("上传返回:{}",upload);
    }
    @Test
    public void download(){
        String url = storageTemplate.downloadUrl("FkslgNeYxYfdNsy-k0jcQNQHM3YX");

    }
}