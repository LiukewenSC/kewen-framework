package com.kewen.framework.boot.authority.advance.datarange;

import com.kewen.framework.base.authority.context.CurrentUserContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @descrpition 切面拦截注解 {@link DataRange} 将注解对应的信息写入权限范围上下文中，供mapper拦截器中使用
 * @author kewen
 * @since 2022-11-24 17:26
 */
@Component
@Aspect
public class DataRangeAspect {

    private static final String NONE_AUTH="NONE";

    @Pointcut("@annotation(com.kewen.framework.boot.authority.advance.datarange.DataRange)")
    public void jointPoint() {

    }

    @Around("jointPoint() && @annotation(dataRange)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint, DataRange dataRange) throws Throwable {

        Collection<String> auths = CurrentUserContext.getCurrentUserAuths();
        DataRangeContext.AuthRange selectAuth = new DataRangeContext.AuthRange()
                .setModule(dataRange.module())
                .setOperate(dataRange.operate())
                .setTableAlias(dataRange.tableAlias())
                .setBusinessColumn(dataRange.businessColumn())
                .setAuthorities(auths);

        DataRangeContext.set(selectAuth);
        try {
            return proceedingJoinPoint.proceed();
        } finally {
            DataRangeContext.clear();
        }

    }

}
