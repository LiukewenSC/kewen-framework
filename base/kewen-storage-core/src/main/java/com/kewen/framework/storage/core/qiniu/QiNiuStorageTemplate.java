package com.kewen.framework.storage.core.qiniu;

import com.kewen.framework.storage.core.StorageTemplate;
import com.kewen.framework.storage.core.model.UploadBO;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.DownloadUrl;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * @author kewen
 * @since 2023-04-23
 */
@Slf4j
public class QiNiuStorageTemplate implements StorageTemplate {

    /**
     * 权限对象（估计要改，分片不是这样用的，要new出来传参）
     */
    //private String accessKey = "qYUhPA6FvZ29dQ4pz1BgLwHaTcV2vt_rD-GoEEFB";
    //private String secretKey = "eyihg0gLAyHd2j08geDCCWdI0UHOfhxzpjFpKso1";
    Auth auth;
    /**
     * 存储桶空间
     *   "kewen-blog"
     */
    private final String bucket ;
    /**
     * 下载的域名地址
     */
    // "rtk99wucl.hd-bkt.clouddn.com"
    private final String downloadDomain ;
    UploadManager uploadManager;

    public QiNiuStorageTemplate(String accessKey, String secretKey, String bucket,String downloadDomain) {


        this.auth = Auth.create(accessKey, secretKey);

        //构造一个带指定Region对象的配置类
        Configuration cfg = new Configuration(Region.region0());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本
        cfg.useHttpsDomains = false;

        //...其他参数参考类注释
        this.uploadManager = new UploadManager(cfg);
        this.bucket = bucket;
        this.downloadDomain = downloadDomain;
    }
    private StringMap policy(){
        StringMap policy = new StringMap();

        // returnBody 可配置返回结构
        // 魔法值对照 https://developer.qiniu.com/kodo/1235/vars#magicvar
        // 自定义变量 https://developer.qiniu.com/kodo/1235/vars#xvar
        policy.put("returnBody", "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"size\":$(fsize)}");
        return policy;
    }

    @Override
    public UploadBO upload(InputStream stream, String storageName, String mediumType) {

        //...生成上传凭证，然后准备上传
        String upToken = auth.uploadToken(bucket,null,20L,policy());


        Response response = null;
        try {
            response = uploadManager.put(stream, storageName, upToken, null, mediumType);

            UploadBO uploadBO = response.jsonToObject(UploadBO.class);

            log.info("uploadBO={}", uploadBO);
            return uploadBO;
        } catch (QiniuException e) {
            throw new RuntimeException(e);
        } finally {
            if (response != null) {
                response.close();
            }
        }

    }

    @Override
    public String downloadUrl(String key) {
        // domain   下载 domain, eg: qiniu.com【必须】
        // useHttps 是否使用 https【必须】
        // key      下载资源在七牛云存储的 key【必须】
        DownloadUrl url = new DownloadUrl(downloadDomain, false, key);
        //url.setAttname(attname) // 配置 attname
        //        .setFop(fop) // 配置 fop
        //        .setStyle(style, styleSeparator, styleParam) // 配置 style
        try {
            String urlString = url.buildURL();
           log.info("{} 下载链接 :{}",key,urlString);
            return urlString;
        } catch (QiniuException e) {
            throw new RuntimeException(e);
        }

    }
}
