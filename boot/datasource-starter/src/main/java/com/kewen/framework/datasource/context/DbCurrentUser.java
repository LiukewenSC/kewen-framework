package com.kewen.framework.datasource.context;

/**
 * 当前登录人上下文  保存数据时自动填充createUserId和createUserName
 * {@link com.kewen.framework.datasource.plug.GlobalFillConfig #insertFill}
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
