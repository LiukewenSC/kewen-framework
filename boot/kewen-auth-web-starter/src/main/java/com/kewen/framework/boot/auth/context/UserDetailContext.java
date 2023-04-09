package com.kewen.framework.boot.auth.context;

import com.kewen.framework.base.authority.model.UserDetail;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserDetailContext implements ApplicationContextAware {

    private static UserDetailContextContainer container ;

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
    public static UserDetail get(){
        return container.getUserDetail();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        UserDetailContext.container=applicationContext.getBean(UserDetailContextContainer.class);
    }
}
