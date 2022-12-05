package com.kewen.framework.boot.authority.advance.authrange;

import com.kewen.framework.boot.authority.currentuser.CurrentUserContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;

/**
 * @descrpition 切面拦截注解 {@link AuthRange} 将注解对应的信息写入权限范围上下文中，供mapper拦截器中使用
 * @author kewen
 * @since 2022-11-24 17:26
 */
@Component
@Aspect
public class AuthRangeAspect {

    private static final String NONE_AUTH="NONE";

    @Pointcut("@annotation(com.kewen.framework.boot.authority.advance.authrange.AuthRange)")
    public void jointPoint() {

    }

    @Around("jointPoint() && @annotation(authRange)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint, AuthRange authRange) throws Throwable {

        Collection<String> auths = CurrentUserContext.getCurrentUserAuths();
        //从@AuthRange来的请求，但是用户没有权限，没有权限不进行验证
        if (CollectionUtils.isEmpty(auths)){
            //没有权限就设置一个默认权限NONE
            auths= Collections.singletonList(NONE_AUTH);
            //throw new AuthorizationException("用户没有权限，范围查询为空值");
        }
        AuthRangeContext.AuthRange selectAuth = new AuthRangeContext.AuthRange()
                .setModule(authRange.module())
                .setOperate(authRange.operate())
                .setTableAlias(authRange.tableAlias())
                .setBusinessColumn(authRange.businessColumn())
                .setAuthorities(auths);

        AuthRangeContext.set(selectAuth);
        try {
            return proceedingJoinPoint.proceed();
        } finally {
            AuthRangeContext.clear();
        }

    }

}
