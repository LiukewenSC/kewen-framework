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
public class FileServiceImplTest {

    @Autowired
    FileService fileService;
    @Test
    public void upload() throws QiniuException {
        fileService.upload("D:\\ChinaUnicomProjects\\log\\emergency.2023-03-23.0.log");
    }
    @Test
    public void getFilePath() throws QiniuException {
        String filePath = fileService.getFilePath("FrK_Ta6bOnh390PCRe5gdv-hs7Gc");

    }

}