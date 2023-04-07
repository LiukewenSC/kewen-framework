package com.kewen.framework.datasource.context;

import com.kewen.framework.datasource.plug.AutoFillConfig;

/**
 * 当前登录人上下文  保存数据时自动填充createUserId和createUserName
 * {@link AutoFillConfig #insertFill}
 * @author liukewen
 * @since 2022/9/1
 */
public interface DbCurrentUser {

    /**
     * 用户id
     * @return
     */
    Long getUserId();

    /**
     * 用户名
     * @return
     */
    String getUserName();

}
