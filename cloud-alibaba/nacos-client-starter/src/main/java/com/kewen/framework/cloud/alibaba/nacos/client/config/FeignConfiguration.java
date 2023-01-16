package com.kewen.framework.cloud.alibaba.nacos.client.config;

import com.kewen.framework.cloud.alibaba.nacos.client.exception.FeignExceptionAdvance;
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
@EnableFeignClients(basePackages = {"com.kewen"})
@ComponentScan(basePackageClasses = FeignExceptionAdvance.class)
//@EnableCircuitBreaker
@Slf4j
public class FeignConfiguration {

}