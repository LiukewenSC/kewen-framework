package com.kewen.framework.cloud.eureka.client.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * feign的调用
 * @author liukewen
 * @since 2022/9/2
 */
@Configuration
@EnableFeignClients(basePackages = {"com.kewen.framework.cloud.eureka.client"})
@EnableCircuitBreaker
@ComponentScan(basePackages = "com.kewen.framework.cloud.eureka.client")
@Slf4j
public class FeignConfiguration{

}