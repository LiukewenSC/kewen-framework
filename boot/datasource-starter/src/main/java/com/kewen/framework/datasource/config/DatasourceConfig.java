package com.kewen.framework.datasource.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @descrpition 
 * @author kewen
 * @since 2022-12-05 16:09
 */
@Configuration
@PropertySource({"classpath:application-datasource.properties","classpath:application-datasource-${spring.profiles.active}.properties"})
public class DatasourceConfig {

}
