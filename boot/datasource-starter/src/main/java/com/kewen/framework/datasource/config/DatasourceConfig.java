package com.kewen.framework.datasource.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @descrpition 
 * @author kewen
 * @since 2022-12-05 16:09
 */
@Configuration
@ConfigurationProperties("application-datasource.properties")
public class DatasourceConfig {

}
