package com.kewen.framework.datasource.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @descrpition 
 * @author kewen
 * @since 2022-12-05
 */
@Configuration
@MapperScan("com.kewen.framework.**.mapper")
@PropertySource({"classpath:application-datasource.yml", "classpath:application-datasource-${spring.profiles.active}.yml"})
public class DatasourceConfig {

}
