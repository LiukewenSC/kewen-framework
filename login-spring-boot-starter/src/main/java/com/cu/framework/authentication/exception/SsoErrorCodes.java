package com.cu.framework.authentication.exception;

/**
 * 单点登录失败原因码，写入认证异常携带的 error code，使调用方与日志能把这类失败
 * 和凭据错误、协议校验失败等其他认证失败区分开
 *
 * @author kewen
 * @since 2026-10-18
 */
public final class SsoErrorCodes {

    /**
     * 身份源认证通过，但本系统不存在与之对应的账号
     */
    public static final String LOCAL_USER_NOT_FOUND = "sso_local_user_not_found";

    private SsoErrorCodes() {
    }
}
