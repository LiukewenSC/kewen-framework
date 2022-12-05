package com.kewen.framework.sample.service;

import com.kewen.framework.boot.web.interceptor.DefaultTenantInterceptor;
import com.kewen.framework.boot.web.interceptor.TenantInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @descrpition 
 * @author kewen
 * @since 2022-12-05 10:00
 */
@Component
public class TestService {

    @Autowired
    TenantInterceptor tenantInterceptor;

    public void hello(){
        System.out.println("hello");
    }

}
