package com.kewen.framework.common.context;

import com.kewen.framework.common.context.container.UserContextContainer;
import com.kewen.framework.common.core.model.IUser;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

/**
 * @descrpition 用户上下文获取接口
 * @author kewen
 * @since 2022-12-05 16:24
 */
public class UserContext implements ApplicationContextAware {

    private static UserContextContainer container ;

    /**
     * 是否支持用户上下文
     * @return
     */
    public static boolean support(){
        return container != null;
    }

    /**
     * 获取用户上下文信息
     * @return
     */
    public static IUser get(){
        return container.getUser();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //不能直接 applicationContext 此处有为空则报错
        ObjectProvider<UserContextContainer> beanProvider = applicationContext.getBeanProvider(UserContextContainer.class);
        UserContext.container= beanProvider.getIfAvailable();
    }

}
