package com.kewen.framework.sample.tenant.controller;


import com.kewen.framework.tenant.TenantContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author kewen
 * @since 2024-08-30
 */
@RestController
public class TenantController {

    @GetMapping("/hello")
    public Object hello(){
        Long tenantId = TenantContext.get();
        return "hello - "+ tenantId;
    }
}
