package com.kewen.framework.boot.common.context;


import com.kewen.framework.base.common.model.IUser;

/**
 *  用户上下文容器，设定从哪里取出User
 * @author kewen
 * @since 2023-04-07
 */
public interface UserContextContainer {

    /**
     * 获取用户上下文
     * todo  以后再改把，肯定还需要比较细的属性
     * @return
     */
    IUser getUser();

}
