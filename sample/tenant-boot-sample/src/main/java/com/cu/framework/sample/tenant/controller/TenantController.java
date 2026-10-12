package com.cu.framework.sample.tenant.controller;


import com.cu.framework.tenant.TenantContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author kewen
 * @since 2024-08-30
 */
@RestController
public class TenantController {

    /**
     * 多租户示例接口，从 TenantContext 线程上下文取出当前租户 ID 并拼入问候语返回，
     * 用于验证租户 ID 已随请求正确写入上下文
     * @return 含当前租户 ID 的问候字符串
     */
    @GetMapping("/hello")
    public Object hello(){
        Long tenantId = TenantContext.get();
        return "hello - "+ tenantId;
    }
}
