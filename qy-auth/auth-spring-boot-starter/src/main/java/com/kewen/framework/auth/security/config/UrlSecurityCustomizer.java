package com.kewen.framework.auth.security.config;


import java.util.List;

/**
 * URL链接自定义
 * @author kewen
 * @since 2026-05-18
 */
public interface UrlSecurityCustomizer {
    /**
     * 自定义放行策略
     */
    List<String> permitAll();

}
