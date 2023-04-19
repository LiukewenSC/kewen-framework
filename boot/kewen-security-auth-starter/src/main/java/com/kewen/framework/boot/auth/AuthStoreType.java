package com.kewen.framework.boot.auth;
/**
 * 登录信息存储类型
 * @author kewen
 * @since 2023-04-19
 */
public enum AuthStoreType {
    TOKEN("token"),
    SESSION("session"),

    ;

    private final String value;

    AuthStoreType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
