package com.kewen.framework.oos;

import com.kewen.framework.oos.key.DefaultKeyInfo;
import com.kewen.framework.oos.key.KeyInfo;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * @author kewen
 * @descrpition
 * @since 2023-03-30
 */
@Component
public class BucketServiceImpl implements BucketService{
    BucketManager bucketManager;

    public BucketServiceImpl(ObjectProvider<KeyInfo> keyInfoObjectProvider) {
        KeyInfo keyInfo = keyInfoObjectProvider.getIfAvailable();
        if (keyInfo ==null){
            keyInfo  = new DefaultKeyInfo();
        }
        Auth auth = Auth.create(keyInfo.getAccessKey(), keyInfo.getSecretKey());
        Configuration configuration = new Configuration();
        this.bucketManager = new BucketManager(auth, configuration);
    }

    @Override
    public void createBucket(String bucket) throws QiniuException {
        //String[] buckets = bucketManager.buckets();
        //for (String s : buckets) {
        //    if (s.equals(bucket)){
        //        return;
        //    }
        //}
        Response response = bucketManager.createBucket(bucket, "z2");
        response.close();
    }
}
