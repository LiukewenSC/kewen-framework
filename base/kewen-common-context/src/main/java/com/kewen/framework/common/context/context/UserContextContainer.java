package com.kewen.framework.common.context.context;


import com.kewen.framework.common.core.model.IUser;

/**
 *  用户上下文容器，定义用户的容器
 * @author kewen
 * @since 2023-04-07
 */
public interface UserContextContainer {

    /**
     * 获取用户上下文
     * @return
     */
    IUser getUser();

}
