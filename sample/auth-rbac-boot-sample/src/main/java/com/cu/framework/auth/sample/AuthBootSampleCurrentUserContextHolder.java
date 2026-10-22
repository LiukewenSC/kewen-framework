package com.cu.framework.auth.sample;

import com.cu.framework.auth.core.AuthCurrentUserContextHolder;
import com.cu.framework.auth.core.entity.FlatteningAuthority;
import com.cu.framework.auth.core.entity.CurrentUser;
import com.cu.framework.auth.rbac.composite.SysUserComposite;
import com.cu.framework.auth.rbac.composite.model.UserAuthInfo;
import com.cu.framework.auth.rbac.model.UserAuthObject;
import lombok.Setter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.Collections;

/**
 * 示例用的用户上下文：本 sample 未引入 Spring Security 登录态，身份由请求头 {@code X-Demo-User} 声明，
 * 不带该头时回落到 admin；权限集合按该用户名从 sys_user 体系实时装载。
 * <p>
 * 头部名沿用 auth-core 示例已有的 {@code X-Demo-User}，为的是让"同一份权限配置、换一个身份"
 * 在两个示例里是同一个动作
 *
 * @author kewen
 * @since 2026-10-15
 */
@Setter
public class AuthBootSampleCurrentUserContextHolder implements AuthCurrentUserContextHolder {

    /** 声明本次请求以哪个用户身份装载权限 */
    private static final String USER_HEADER = "X-Demo-User";

    /** 不带身份头时的缺省用户 */
    private static final String DEFAULT_USERNAME = "admin";

    private SysUserComposite sysUserComposite;

    @Override
    public CurrentUser getUser() {
        return new CurrentUser().setUsername(getUsername());
    }

    @Override
    public Collection<FlatteningAuthority> getAuths() {
        UserAuthObject authObject = getUserAuthObjectByUsername(getUsername());
        if (authObject == null){
            return Collections.emptyList();
        }
        // 无凭证的用户装载不出权限体，这里给出空权限而不是让它解引用爆掉
        UserAuthInfo authInfo = authObject.getAuthInfo();
        if (authInfo == null) {
            return Collections.emptyList();
        }
        return authInfo.listBaseAuth();
    }

    public UserAuthObject getUserAuthObjectByUsername(String username){
        return sysUserComposite.getUserAuthObjectByUsername(username);
    }

    /**
     * 取当前请求声明的用户名。用 RequestContextHolder 而不是再配一个 Filter 加线程上下文：
     * 菜单鉴权发生在 DispatcherServlet 内部的拦截器里，那时请求上下文已经绑定
     * @return 请求头给出的用户名，缺省为 admin
     */
    private String getUsername() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return DEFAULT_USERNAME;
        }
        String username = attributes.getRequest().getHeader(USER_HEADER);
        if (username == null || username.trim().isEmpty()) {
            return DEFAULT_USERNAME;
        }
        return username.trim();
    }
}
