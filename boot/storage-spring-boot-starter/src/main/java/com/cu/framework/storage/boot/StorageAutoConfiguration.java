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
import org.springframework.context.annotation.Configuration;

/**
 * 
 * @author kewen
 * @since 2023-04-24
 */
@Configuration
@EnableConfigurationProperties({StorageProperties.class})
@MapperScan("com.cu.framework.storage.web.mp.mapper")
@ComponentScan("com.cu.framework.storage.web.mp")
public class StorageAutoConfiguration {

    @Autowired
    StorageProperties storageProperties;


    /**
     * 配置存储模板
     * @return
     */
    @Bean
    StorageTemplate storageTemplate(){
        return new QiNiuStorageTemplate(
                storageProperties.getAccessKey(),
                storageProperties.getSecretKey(),
                //华南机房
                Region.region2(),
                storageProperties.getBucket(),
                storageProperties.getRootPath(),
                storageProperties.getIsPublic(),
                storageProperties.getDownloadDomain(),
                storageProperties.getUploadCallbackUrl()
        );
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
