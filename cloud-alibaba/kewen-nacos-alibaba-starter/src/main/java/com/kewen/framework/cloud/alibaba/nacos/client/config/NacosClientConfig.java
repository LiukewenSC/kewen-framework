package com.kewen.framework.cloud.alibaba.nacos.client.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@EnableDiscoveryClient
@Configuration
@PropertySource("classpath:application-nacos-client.yml")
public class NacosClientConfig {

}
