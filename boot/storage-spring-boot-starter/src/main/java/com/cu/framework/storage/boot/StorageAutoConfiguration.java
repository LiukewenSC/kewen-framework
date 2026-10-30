package com.cu.framework.storage.boot;

import com.cu.framework.storage.core.StorageTemplate;
import com.cu.framework.storage.core.qiniu.QiNiuStorageTemplate;
import com.cu.framework.storage.web.FileResponseBodyAdvance;
import com.cu.framework.storage.web.FileStorageProcessor;
import com.cu.framework.storage.web.FileStorageEndpoint;
import com.cu.framework.storage.web.FileStorageCallbackEndpoint;
import com.cu.framework.storage.web.impl.DefaultFileStorageProcessor;
import com.qiniu.storage.Region;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * 
 * @author kewen
 * @since 2023-04-24
 */
@AutoConfiguration
@EnableConfigurationProperties({StorageProperties.class})
@MapperScan("com.cu.framework.storage.web.mp.mapper")
@ComponentScan("com.cu.framework.storage.web.mp")
public class StorageAutoConfiguration {

    @Autowired
    StorageProperties storageProperties;


    /**
     * 配置存储模板
     * @return 七牛云存储模板
     */
    @Bean
    StorageTemplate storageTemplate(){
        return new QiNiuStorageTemplate(
                storageProperties.getAccessKey(),
                storageProperties.getSecretKey(),
                resolveRegion(storageProperties.getRegion()),
                storageProperties.getBucket(),
                storageProperties.getRootPath(),
                storageProperties.getIsPublic(),
                storageProperties.getDownloadDomain(),
                storageProperties.getUploadCallbackUrl(),
                storageProperties.getPersistentNotifyUrl(),
                Boolean.TRUE.equals(storageProperties.getUseHttpsDownload())
        );
    }

    /**
     * 把配置中的区域标识解析为七牛 Region 实例
     * @param region cu.storage.region 配置值，接受 region0/region1/region2/region3/region4/auto
     *               及对应拼音别名 huadong/huabei/huanan/beimei/xinjiapo
     * @return 对应的七牛 Region
     * @throws IllegalArgumentException 区域标识无法识别时抛出，避免静默落到错误的机房
     */
    private static Region resolveRegion(String region) {
        String key = region == null ? "" : region.trim().toLowerCase();
        switch (key) {
            case "region0": case "huadong":
                return Region.region0();
            case "region1": case "huabei":
                return Region.region1();
            case "region2": case "huanan":
                return Region.region2();
            case "region3": case "beimei":
                return Region.beimei();
            case "region4": case "xinjiapo":
                return Region.xinjiapo();
            case "auto": case "autoregion":
                return Region.autoRegion();
            default:
                throw new IllegalArgumentException("无法识别的存储区域配置 cu.storage.region=" + region
                        + "，可选值：region0/huadong、region1/huabei、region2/huanan、"
                        + "region3/beimei、region4/xinjiapo、auto");
        }
    }

    @Bean
    FileStorageEndpoint storageController(){
        return new FileStorageEndpoint();
    }
    @Bean
    FileStorageCallbackEndpoint storageUploadCallback(){
        return new FileStorageCallbackEndpoint();
    }

    @Bean
    FileResponseBodyAdvance fileResponseBodyAdvance(){
        return new FileResponseBodyAdvance();
    }

    @Bean
    FileStorageProcessor storageService(){
        return new DefaultFileStorageProcessor();
    }
}
