package com.cu.framework.boot.auth.rbac.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 *  impl 实现相关
 *  有了子包才有此处配置
 * @author kewen
 * @since 2023-12-28
 */
@AutoConfiguration
@MapperScan({
        "com.cu.framework.auth.rbac.**.mapper"
})
@ComponentScan({
        "com.cu.framework.auth.rbac.mp.service.impl",
        "com.cu.framework.auth.rbac.composite",
        "com.cu.framework.auth.rbac.controller"
})
public class AuthRbacScanConfig {

}
