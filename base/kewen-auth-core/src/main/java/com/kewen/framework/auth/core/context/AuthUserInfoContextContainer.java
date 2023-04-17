package com.kewen.framework.auth.core.context;

import com.kewen.framework.auth.core.model.AuthUserInfo;
import com.kewen.framework.common.core.model.IUser;
import com.kewen.framework.common.context.context.UserContextContainer;

/**
 * @descrpition  用户权限上下文容器，用户登录后会将用户信息写入容器中，可以从上下文中获取
 * @author kewen
 * @since 2022-11-25 15:49
 */
public interface AuthUserInfoContextContainer extends UserContextContainer {


    AuthUserInfo getAuthUserInfo();

    @Override
    default IUser getUser() {
        return getAuthUserInfo();
    }
}
