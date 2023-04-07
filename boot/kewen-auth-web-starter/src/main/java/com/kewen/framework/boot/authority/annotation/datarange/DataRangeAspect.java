package com.kewen.framework.boot.authority.annotation.datarange;

import com.kewen.framework.base.authority.context.CurrentUserContext;
import com.kewen.framework.boot.authority.annotation.CheckDataRange;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @descrpition 切面拦截注解 {@link CheckDataRange} 将注解对应的信息写入权限范围上下文中，供mapper拦截器中使用
 * @author kewen
 * @since 2022-11-24 17:26
 */
@Component
@Aspect
public class DataRangeAspect {

    private static final String NONE_AUTH="NONE";

    @Pointcut("@annotation(com.kewen.framework.boot.authority.annotation.CheckDataRange)")
    public void jointPoint() {

    }

    @Around("jointPoint() && @annotation(checkDataRange)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint, CheckDataRange checkDataRange) throws Throwable {

        Collection<String> auths = CurrentUserContext.getCurrentUserAuths();
        DataRangeContext.AuthRange selectAuth = new DataRangeContext.AuthRange()
                .setModule(checkDataRange.module())
                .setOperate(checkDataRange.operate())
                .setTableAlias(checkDataRange.tableAlias())
                .setBusinessColumn(checkDataRange.businessColumn())
                .setAuthorities(auths);

        DataRangeContext.set(selectAuth);
        try {
            return proceedingJoinPoint.proceed();
        } finally {
            DataRangeContext.clear();
        }

    }

}
