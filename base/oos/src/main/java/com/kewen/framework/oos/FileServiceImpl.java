package com.kewen.framework.oos;

import com.kewen.framework.oos.key.DefaultKeyInfo;
import com.kewen.framework.oos.key.KeyInfo;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.DownloadPrivateCloudUrl;
import com.qiniu.storage.DownloadUrl;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * @author kewen
 * @descrpition
 * @since 2023-03-30
 */
@Component
public class FileServiceImpl implements FileService {

    UploadManager uploadManager;
    Configuration configuration;
    Auth auth;

    public FileServiceImpl(ObjectProvider<KeyInfo> keyInfoObjectProvider) {
        KeyInfo keyInfo = keyInfoObjectProvider.getIfAvailable();
        if (keyInfo ==null){
            keyInfo  = new DefaultKeyInfo();
        }
        this.auth = Auth.create(keyInfo.getAccessKey(), keyInfo.getSecretKey());
        this.configuration = new Configuration();
        configuration.useHttpsDomains=false;
        this.uploadManager = new UploadManager(configuration);
    }

    @Override
    public String upload(String filePath) throws QiniuException {
        Response response = uploadManager.put(filePath, null, auth.uploadToken(KeyInfo.bucketName));
        try {
            StringMap stringMap = response.jsonToMap();
            return (String) stringMap.get("key");
        } finally {
            response.close();
        }
    }

    @Override
    public String getFilePath(String key) throws QiniuException {
        DownloadPrivateCloudUrl cloudUrl = new DownloadPrivateCloudUrl(configuration, KeyInfo.bucketName, key, KeyInfo.accessKey);
        String s = cloudUrl.buildURL();
        return s;
    }
}
