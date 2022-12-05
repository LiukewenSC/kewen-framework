package com.kewen.framework.cloud.eureka.client.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * eureka的客户端配置，统一配置在此处，不用再各地方单独配置
 * @author liukewen
 * @since 2022/9/2
 */

@Configuration
@PropertySource("classpath:application-eureka-client.properties")
@EnableDiscoveryClient
public class EurekaClientConfiguration {

}