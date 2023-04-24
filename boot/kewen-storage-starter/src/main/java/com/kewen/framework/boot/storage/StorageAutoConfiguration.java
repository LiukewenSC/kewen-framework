package com.kewen.framework.boot.storage;

import com.kewen.framework.storage.core.StorageTemplate;
import com.kewen.framework.storage.core.qiniu.QiNiuStorageTemplate;
import com.kewen.framework.storage.web.StorageController;
import com.kewen.framework.storage.web.service.StorageService;
import com.kewen.framework.storage.web.service.impl.NoneStorageService;
import com.kewen.framework.storage.web.service.impl.SysFileStorageService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@EnableConfigurationProperties(StorageProperties.class)
@Import(StorageAutoConfiguration.PersistentConfig.class)
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
                storageProperties.getBucket(),
                storageProperties.getDownloadDomain()
        );
    }

    @Bean
    StorageController storageController(){
        StorageController storageController = new StorageController();
        storageController.setDownloadDomain(storageProperties.getDownloadDomain());
        return storageController;
    }

    @Bean
    @ConditionalOnMissingBean(StorageService.class)
    StorageService storageService(){
        return new NoneStorageService();
    }

    @ConditionalOnProperty(name = "kewen.storage.open-persistent",havingValue = "true")
    @ComponentScan("com.kewen.framework.storage.web.mp")
    @MapperScan("com.kewen.framework.storage.web.mp.mapper")
    public static class PersistentConfig{

        @Bean
        SysFileStorageService storageService(){
            return new SysFileStorageService();
        }

    }

}
