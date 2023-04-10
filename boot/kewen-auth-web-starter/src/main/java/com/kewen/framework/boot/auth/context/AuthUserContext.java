package com.kewen.framework.boot.auth.context;

import com.kewen.framework.boot.auth.AuthEntity;
import com.kewen.framework.boot.auth.AuthUserInfo;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AuthUserContext implements ApplicationContextAware {

    private static AuthUserInfoContextContainer container ;

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
    public static <T extends AuthUserInfo> T get(){
        return (T)container.getAuthUserInfo();
    }

    /**
     * 获取权限字符串
     * @return
     */
    public static List<String> getAuthorities(){
        return get().authorities();
    }

    /**
     * 获取权限信息
     * @return
     */
    public static List<AuthEntity> getEntities(){
        return get().authEntities();
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        AuthUserContext.container=applicationContext.getBean(AuthUserInfoContextContainer.class);
    }
}
