package com.kewen.framework.boot.auth.token;

/**
 * @descrpition 用户tokenkey生成器
 * @author kewen
 * @since 2022-11-28 17:23
 */
public interface TokenKeyGenerator {
    String generateKey();
}
