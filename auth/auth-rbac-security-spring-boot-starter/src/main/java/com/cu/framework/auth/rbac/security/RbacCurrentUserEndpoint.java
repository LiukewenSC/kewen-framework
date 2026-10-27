package com.cu.framework.auth.rbac.security;

import com.cu.framework.authentication.SecurityUserContext;
import com.cu.framework.basic.model.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 当前登录用户接口，路径取自 {@code cu.security.current-user-url}（默认 /currentUser）。
 * 未登录时由 SecurityUserContext 抛出 NoLoginException，交给安全异常 advice 返回 401。
 *
 * @author kewen
 * @since 2026-10-27
 */
@RestController
public class RbacCurrentUserEndpoint {

    /**
     * 返回当前登录用户的身份信息，不包含密码
     *
     * @return 当前用户信息
     */
    @GetMapping("${cu.security.current-user-url:/currentUser}")
    public Result<RbacCurrentUserResp> currentUser() {
        return Result.success(RbacCurrentUserResp.of(SecurityUserContext.getCurrentUser()));
    }
}
