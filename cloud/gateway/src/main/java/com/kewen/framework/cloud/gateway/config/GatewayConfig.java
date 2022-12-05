package com.kewen.framework.cloud.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author liukewen
 * @since 2022/9/4
 */
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator uucsRoueLocator(RouteLocatorBuilder builder) {
        RouteLocator build = builder.routes().route(
                "route-id-1",
                r -> r.path("/user-service/**")
                        .uri("bl:user-service")
        ).build();
        return build;
    }

}