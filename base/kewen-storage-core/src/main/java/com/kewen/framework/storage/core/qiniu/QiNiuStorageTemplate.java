package com.kewen.framework.storage.core.qiniu;

import cn.hutool.crypto.digest.DigestUtil;
import com.kewen.framework.storage.core.StorageTemplate;
import com.kewen.framework.storage.core.model.PreUploadTokenBO;
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
    private final String callbackUrl ;
    UploadManager uploadManager;

    public QiNiuStorageTemplate(String accessKey, String secretKey, String bucket,String downloadDomain,String callbackUrl) {


        this.auth = Auth.create(accessKey, secretKey);

        //构造一个带指定Region对象的配置类
        Configuration cfg = new Configuration(Region.region0());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本
        cfg.useHttpsDomains = false;

        //...其他参数参考类注释
        this.uploadManager = new UploadManager(cfg);
        this.bucket = bucket;
        this.downloadDomain = downloadDomain;
        this.callbackUrl = callbackUrl;
    }

    @Override
    public UploadBO upload(InputStream stream, String fileName, String mimeType) {
        StringMap policy = new StringMap();
        policy.put("returnBody", "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"size\":$(fsize)}");
        //...生成上传凭证，然后准备上传
        String upToken = auth.uploadToken(bucket,null,20L,policy);


        Response response = null;
        try {
            response = uploadManager.put(stream, fileName, upToken, null, mimeType);

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

    @Override
    public PreUploadTokenBO createPreUploadToken(String moduleName, String fileName) {

        StringMap putPolicy = new StringMap();

        String key=moduleName+"/"+fileName;

        // https://developer.qiniu.com/kodo/1206/put-policy
        putPolicy.put("callbackUrl", callbackUrl);

        putPolicy.put("callbackBody", "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\",\"size\":$(fsize)," +
                "\"mimeType\":\"$(mimeType)\",\"fileId\":$(x:fileId)}"
        );
        putPolicy.put("callbackBodyType", "application/json");

        //putPolicy.put("forceSaveKey",true);
        //putPolicy.put("saveKey",);


        String token = auth.uploadToken(bucket, key, 20L, putPolicy);

        return new PreUploadTokenBO().setUploadToken(token).setKey(key);

    }
}
