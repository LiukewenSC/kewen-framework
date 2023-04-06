package com.kewen.framework.base.authority.context;

import com.kewen.framework.base.authority.utils.AuthorityConvertUtil;
import com.kewen.framework.base.authority.model.Authority;
import com.kewen.framework.base.common.model.UserDetail;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @descrpition 用上下文类
 * @author kewen
 * @since 2022-11-25 15:18
 */
public class CurrentUserContext  {

    private static final ThreadLocal<UserDetail> USER_CONTEXT = new InheritableThreadLocal<>();

    /**
     * 获取当前用户
     * @return
     */
    public static UserDetail getCurrentUser(){
        return USER_CONTEXT.get();
    }

    /**
     * 设置当前用户
     * @param userDetail
     */
    public static void setCurrentUser(UserDetail userDetail){
        USER_CONTEXT.set(userDetail);
    }

    /**
     * 清除当前用户
     */
    public static void clearCurrentUser(){
        USER_CONTEXT.remove();
    }

    /**
     * 获取当前用户权限
     * @return
     */
    public static Collection<Authority> getCurrentUserAuthorities() {
        UserDetail currentUser = USER_CONTEXT.get();
        return AuthorityConvertUtil.parseCurrentUser(currentUser);
    }

    /**
     * 获取当前用权限字符串
     * @return
     */
    public static Collection<String> getCurrentUserAuths(){
        Collection<Authority> authorities = getCurrentUserAuthorities();
        return authorities==null ? Collections.emptyList()
                : authorities.stream()
                .map(Authority::getAuthority)
                .collect(Collectors.toList());
    }
}
