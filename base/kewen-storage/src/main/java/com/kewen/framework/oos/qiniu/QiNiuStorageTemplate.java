package com.kewen.framework.oos.qiniu;

import com.kewen.framework.oos.StorageTemplate;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author kewen
 * @since 2023-04-23
 */
public class QiNiuStorageTemplate implements StorageTemplate {

    private String accessKey = "";
    private String secretKey = "kewen-blog";
    private String bucket = "kewen-blog";

    @Override
    public void upload() {
        //构造一个带指定Region对象的配置类
        Configuration cfg = new Configuration(Region.region0());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本
        cfg.useHttpsDomains = false;
        //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
        //...生成上传凭证，然后准备上传

        InputStream inputStream = new ByteArrayInputStream(new byte[0]);

        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);
        Response response = null;
        try {
            response = uploadManager.put(inputStream, null, upToken, null, null);

            StringMap stringMap = response.jsonToMap();



        } catch (QiniuException e) {
            throw new RuntimeException(e);
        } finally {

            if (response != null){
                response.close();
            }
        }

    }

    @Override
    public void download() {

    }
}
