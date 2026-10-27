package com.cu.framework.authentication;

import com.cu.framework.authentication.exception.NoLoginException;
import com.cu.framework.authentication.model.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 用户上下文
 * @author kewen
 * @since 2026-10-15
 */
public class SecurityUserContext {

    /**
     * 取当前登录用户。账号密码与三种单点登录写入安全上下文的主体都是
     * {@link SecurityUser}（单点登录的是其协议子类型），因此这里只做一次判定，
     * 不再按协议分支，也不再回查用户来源
     *
     * @return 当前登录用户
     * @throws NoLoginException 未登录、匿名访问，或主体不是本框架的统一认证实体
     */
    public static SecurityUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new NoLoginException("未登录");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof SecurityUser)) {
            // 认证主体不是统一认证实体：可能是升级前留下的旧会话，也可能是自行接入的认证方式
            // 没有产出 SecurityUser；按未登录处理，避免把一个身份不明的对象当成当前用户交出去
            throw new NoLoginException("认证主体不是统一认证实体："
                    + (principal == null ? "null" : principal.getClass().getName()));
        }
        return (SecurityUser) principal;
    }
}
