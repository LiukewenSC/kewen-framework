package com.kewen.framework.boot.auth.web.token;

import java.util.UUID;

/**
 * @descrpition 
 * @author kewen
 * @since 2022-11-28 17:24
 */
public class DefaultTokenKeyGenerator implements TokenKeyGenerator {
    @Override
    public String generateKey() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
