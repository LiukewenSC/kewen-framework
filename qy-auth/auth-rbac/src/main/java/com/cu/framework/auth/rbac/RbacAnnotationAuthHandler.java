package com.cu.framework.auth.rbac;

import com.cu.framework.auth.core.entity.BaseAuth;
import com.cu.framework.auth.core.menu.AuthMenuHandler;
import com.cu.framework.auth.rbac.composite.SysAuthMenuComposite;

import java.util.Collection;

/**
 * 默认的注解权限处理器的实现，根据数据库来处理
 *
 * @author kewen
 * @since 2023-12-28
 */
public class RbacAnnotationAuthHandler implements AuthMenuHandler {

    private final SysAuthMenuComposite sysAuthMenuComposite;

    public RbacAnnotationAuthHandler(SysAuthMenuComposite sysAuthMenuComposite) {
        this.sysAuthMenuComposite = sysAuthMenuComposite;
    }

    @Override
    public boolean hasMenuAccessAuth(Collection<BaseAuth> auths, String path) {
        return sysAuthMenuComposite.hasMenuAuth(auths, path);
    }
}
