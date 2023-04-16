package com.kewen.framework.boot.common.context;

import com.kewen.framework.base.common.model.IUser;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

/**
 * @descrpition 用户上下文获取接口
 * @author kewen
 * @since 2022-12-05 16:24
 */
@Configuration
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
        UserContext.container= applicationContext.getBean(UserContextContainer.class);
    }

}
