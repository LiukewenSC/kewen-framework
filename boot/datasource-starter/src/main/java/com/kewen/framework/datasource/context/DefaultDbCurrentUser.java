package com.kewen.framework.datasource.context;


import com.kewen.framework.base.common.context.UserContext;
import com.kewen.framework.base.common.model.IUser;

/**
 * 默认用户上下文,保存数据时自动填充createUserId和createUserName
 *
 * @author liukewen
 * @since 2022/9/2
 */
public class DefaultDbCurrentUser implements DbCurrentUser {

    @Override
    public Long getUserId() {
        IUser iUser = UserContext.get();
        return iUser == null ? null : iUser.getUserId();
    }

    @Override
    public String getUserName() {
        IUser iUser = UserContext.get();
        return iUser == null ? null : iUser.getUserName();
    }
}