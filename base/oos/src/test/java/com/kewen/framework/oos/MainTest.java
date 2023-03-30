package com.kewen.framework.oos;

import com.kewen.framework.oos.key.KeyInfo;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class MainTest {

    public static void main(String[] args) {
        SpringApplication.run(MainTest.class,args);
    }

    public void t1() throws QiniuException {
        Configuration cfg = new Configuration();
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;
        UploadManager uploadManager = new UploadManager(cfg);
        Auth auth = Auth.create(KeyInfo.accessKey, KeyInfo.secretKey);
        String token = auth.uploadToken(KeyInfo.bucketName);
        //String key = "file save key";
        //Response r = uploadManager.put("hello world".getBytes(), key, token);

        uploadManager.put("D:\\ChinaUnicomDocment\\员工满意度调查.png","self/员工满意.png",token);

    }

    public void getDomains() throws QiniuException {
        Configuration cfg = new Configuration();
        Auth auth = Auth.create(KeyInfo.accessKey, KeyInfo.secretKey);
        BucketManager manager = new BucketManager(auth, cfg);
        Response response = manager.bucketsResponse();


    }


}