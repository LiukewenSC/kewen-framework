package com.kewen.framework.boot.storage;

import com.kewen.framework.storage.core.StorageTemplate;
import com.kewen.framework.storage.core.qiniu.QiNiuStorageTemplate;
import com.kewen.framework.storage.web.FileResponseBodyAdvance;
import com.kewen.framework.storage.web.controller.StorageController;
import com.kewen.framework.storage.web.controller.StorageUploadCallback;
import com.kewen.framework.storage.web.mp.service.SysStorageFileMpService;
import com.kewen.framework.storage.web.mp.service.SysStorageModuleMpService;
import com.kewen.framework.storage.web.service.StorageService;
import com.kewen.framework.storage.web.service.impl.DefaultStorageService;
import com.qiniu.storage.Region;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 
 * @author kewen
 * @since 2023-04-24
 */
@Configuration
@EnableConfigurationProperties({StorageProperties.class})
@Import(StorageAutoConfiguration.DefaultStorageServiceConfig.class)
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
                storageProperties.getIsPublish(),
                storageProperties.getDownloadDomain(),
                storageProperties.getUploadCallbackUrl()
        );
    }

    @Bean
    StorageController storageController(){
        return new StorageController();
    }
    @Bean
    StorageUploadCallback storageUploadCallback(){
        return new StorageUploadCallback();
    }

    @Bean
    FileResponseBodyAdvance fileResponseBodyAdvance(){
        return new FileResponseBodyAdvance();
    }


    /**
     * 默认的存储逻辑配置，模块信息存储在数据库中
     */
    @ConditionalOnMissingBean(StorageService.class)
    @MapperScan("com.kewen.framework.storage.web.mp.mapper")
    @ComponentScan("com.kewen.framework.storage.web.mp")
    public static class DefaultStorageServiceConfig{

        @Autowired
        StorageProperties storageProperties;

        @Bean
        StorageService storageService(StorageTemplate storageTemplate,SysStorageFileMpService storageFileMpService,
                                      SysStorageModuleMpService storageModuleMpService){
            DefaultStorageService service = new DefaultStorageService();
            service.setStorageModuleMpService(storageModuleMpService);
            service.setStorageTemplate(storageTemplate);
            service.setStorageFileMpService(storageFileMpService);

            return service;
        }

    }

}
