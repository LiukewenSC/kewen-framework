package com.kewen.framework.quartz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * quartz相关的配置，主要配置schedule相关信息
 * @author kewen
 * @descrpition
 * @since 2022-11-15 9:23
 */
@Configuration
public class QuartzConfig {

    /**
     * quartz的scheduler定义bean
     * @return
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(){
        SchedulerFactoryBean factoryBean = new SchedulerFactoryBean();
        //覆盖现有的作业
        factoryBean.setOverwriteExistingJobs(true);
        //延时启动
        //factoryBean.setStartupDelay(3);
        //自动启动
        factoryBean.setAutoStartup(true);
        factoryBean.setApplicationContextSchedulerContextKey("applicationContext");
        //配置文件路径
        factoryBean.setConfigLocation(new ClassPathResource("quartz.properties"));
        // 需要存数据库时配置
        // factoryBean.setDataSource();
        return factoryBean;
    }

}
