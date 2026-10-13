package com.cu.framework.storage.core.qiniu;

import com.cu.framework.storage.core.StorageTemplate;
import com.cu.framework.storage.core.model.PreUploadTokenBO;
import com.cu.framework.storage.core.model.UploadBO;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author kewen
 * @since 2023-04-23
 */
@Slf4j
public class QiNiuStorageTemplate implements StorageTemplate {

    /**
     * 权限对象（估计要改，分片不是这样用的，要new出来传参）
     */
    //private String accessKey = "qYUhPA6F******************oEEFB";
    //private String secretKey = "eyihg0gLA******************zpjFpKso1";
    private final Auth auth;
    /**
     * 存储桶空间
     * "cu-blog"
     */
    private final String bucket;

    /** 存储根目录，生成预上传 key 时作为前缀 */
    private final String rootPath;

    /**
     * 是否是公开空间
     */
    private final boolean isPublic;
    /**
     * 下载的域名地址
     */
    // "rtk99wucl.hd-bkt.clouddn.com"
    private final String downloadDomain;
    /** 上传成功后七牛回调的地址，写入预上传凭证的 callbackUrl 策略 */
    private final String callbackUrl;
    /** 数据处理（转码等）的异步通知地址，为空时上传凭证不写入 persistentNotifyUrl 策略 */
    private final String persistentNotifyUrl;
    /** 生成的下载链接是否使用 https */
    private final boolean useHttpsDownload;
    /** 七牛上传管理器，使用 V2 分片上传、https 域名配置构建 */
    private final UploadManager uploadManager;

    /**
     * 构造七牛云存储模板：创建鉴权对象与上传管理器（V2 分片上传、使用 https 域名配置）
     * @param accessKey 七牛 accessKey
     * @param secretKey 七牛 secretKey
     * @param region 存储区域
     * @param bucket 存储桶空间名
     * @param rootPath 存储根目录
     * @param isPublic 是否公开空间
     * @param downloadDomain 下载域名
     * @param callbackUrl 上传成功回调地址
     * @param persistentNotifyUrl 数据处理异步通知地址，可为空
     * @param useHttpsDownload 生成的下载链接是否使用 https
     */
    public QiNiuStorageTemplate(String accessKey, String secretKey, Region region, String bucket, String rootPath,
                                boolean isPublic, String downloadDomain, String callbackUrl,
                                String persistentNotifyUrl, boolean useHttpsDownload) {


        this.auth = Auth.create(accessKey, secretKey);

        //构造一个带指定Region对象的配置类
        Configuration cfg = new Configuration(region);
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本
        cfg.useHttpsDomains = true;

        //...其他参数参考类注释
        this.uploadManager = new UploadManager(cfg);
        this.bucket = bucket;
        this.isPublic = isPublic;
        this.downloadDomain = downloadDomain;
        this.callbackUrl = callbackUrl;
        this.persistentNotifyUrl = persistentNotifyUrl;
        this.useHttpsDownload = useHttpsDownload;
        this.rootPath = rootPath;
    }

    /**
     * 服务端流式上传：按 {@link #resolveKey} 生成对象 key，以该 key 签发有效期 20 秒的上传凭证
     * （returnBody 返回 key/hash/size），再用同一个 key 调用七牛 UploadManager 上传输入流，
     * 并把响应解析为 UploadBO
     * @param stream 文件输入流
     * @param relativeDirectory 相对目录
     * @param fileName 文件名
     * @param mimeType 文件 MIME 类型
     * @return 上传结果（key、hash、size）
     */
    @Override
    public UploadBO upload(InputStream stream, String relativeDirectory, String fileName, String mimeType) {
        StringMap policy = new StringMap();
        policy.put("returnBody", "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"size\":$(fsize)}");
        String key = resolveKey(relativeDirectory, fileName);
        //...生成上传凭证，然后准备上传
        String upToken = auth.uploadToken(bucket, key, 20L, policy);


        Response response = null;
        try {
            response = uploadManager.put(stream, key, upToken, policy, mimeType);

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

    /**
     * 生成文件下载链接：公开空间直接构建 URL；私有空间用 auth 签名并附加 1 小时有效期
     * @param key 文件在七牛云存储的 key
     * @return 下载链接
     */
    @Override
    public String downloadUrl(String key) {
        // domain   下载 domain, eg: qiniu.com【必须】
        // useHttps 是否使用 https【必须】
        // key      下载资源在七牛云存储的 key【必须】
        DownloadUrl url = new DownloadUrl(downloadDomain, useHttpsDownload, key);
        // url.setAttname(attname) // 配置 attname
        //        .setFop(fop) // 配置 fop
        //        .setStyle(style, styleSeparator, styleParam) // 配置 style
        try {
            String urlString;
            if (isPublic) {
                urlString = url.buildURL();
            } else {
                // 带有效期
                long expireInSeconds = 3600;//1小时，可以自定义链接过期时间
                long deadline = System.currentTimeMillis()/1000 + expireInSeconds;
                urlString = url.buildURL(auth, deadline);
            }
            log.info("{} 下载链接 :{}", key, urlString);
            return urlString;
        } catch (QiniuException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 生成客户端直传凭证：key 由 {@link #resolveKey} 按与服务端直传相同的规则生成，策略中配置上传成功后的
     * callbackUrl、callbackBody（JSON 回调内容）等，凭证有效期 120 秒；
     * 仅在配置了 persistentNotifyUrl 时才写入该策略
     * @param relativeDirectory 相对目录
     * @param fileName 文件名
     * @return 预上传凭证（key 与 uploadToken）
     */
    @Override
    public PreUploadTokenBO createPreUploadToken(String relativeDirectory, String fileName) {

        StringMap putPolicy = new StringMap();

        String key = resolveKey(relativeDirectory, fileName);

        // https://developer.qiniu.com/kodo/1206/put-policy
        putPolicy.put("callbackUrl", callbackUrl);

        putPolicy.put("callbackBody", "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\",\"size\":$(fsize)," +
                "\"mimeType\":\"$(mimeType)\",\"fileId\":$(x:fileId)}"
        );
        putPolicy.put("callbackBodyType", "application/json");
        putPolicy.put("persistentType", "1");
        if (persistentNotifyUrl != null && !persistentNotifyUrl.trim().isEmpty()) {
            putPolicy.put("persistentNotifyUrl", persistentNotifyUrl);
        }

        //putPolicy.put("forceSaveKey",true);
        //putPolicy.put("saveKey",);


        String token = auth.uploadToken(bucket, key, 120L, putPolicy);

        return new PreUploadTokenBO().setUploadToken(token).setKey(key);

    }

    /**
     * 生成对象在七牛存储中的 key：把根目录、相对目录、文件名中有内容的段以 "/" 连接。
     * 空段会被跳过，每段首尾多余的 "/" 会先被去掉，因此不会产出前导、尾随或连续的 "/"。
     * 服务端直传与客户端直传共用本方法，保证同一文件在两条路径下落到同一个 key。
     * <p>包级可见以便对该拼接规则做直接断言。
     * @param relativeDirectory 相对目录，可为空
     * @param fileName 文件名
     * @return 对象 key
     */
    String resolveKey(String relativeDirectory, String fileName) {
        return Stream.of(rootPath, relativeDirectory, fileName)
                .filter(segment -> segment != null && !segment.trim().isEmpty())
                .map(segment -> segment.replaceAll("^/+", "").replaceAll("/+$", ""))
                .filter(segment -> !segment.isEmpty())
                .collect(Collectors.joining("/"));
    }
}
