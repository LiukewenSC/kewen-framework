package com.cu.framework.auth.core.data.range;


import com.cu.framework.auth.core.AuthDataRange;
import com.cu.framework.auth.core.AuthCurrentUserContextHolder;
import com.cu.framework.auth.core.entity.BaseAuth;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

/**
 * @descrpition 切面拦截注解 {@link AuthDataRange} 将注解对应的信息写入权限范围上下文中，供mapper拦截器中使用
 * @author kewen
 * @since 2022-11-24
 */
@Aspect
public class DataRangeAspect {

    @Autowired
    private AuthCurrentUserContextHolder authCurrentUserContextHolder;

    /**
     * 切入标注了 {@link AuthDataRange} 注解的方法
     */
    @Pointcut("@annotation(com.cu.framework.auth.core.AuthDataRange)")
    public void jointPoint() {

    }

    /**
     * 将注解属性与当前登录用户的权限组装成 {@link AuthRange} 写入 {@link DataRangeContext}，
     * 供目标方法执行期间的 {@link MybatisDataRangeInterceptor} 做 SQL 增强，
     * 目标方法结束后（含异常）清理上下文
     * @param proceedingJoinPoint 被拦截的方法
     * @param checkAuthDataRange 方法上的 @AuthDataRange 注解
     * @return 目标方法的返回值
     * @throws Throwable 目标方法抛出的异常
     */
    @Around("jointPoint() && @annotation(checkAuthDataRange)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint, AuthDataRange checkAuthDataRange) throws Throwable {

        Collection<BaseAuth> auths = authCurrentUserContextHolder.getAuths();
        AuthRange authRange = new AuthRange()
                .setBusinessFunction(checkAuthDataRange.businessFunction())
                .setOperate(checkAuthDataRange.operate())
                .setTable(checkAuthDataRange.table())
                .setTableAlias(checkAuthDataRange.tableAlias())
                .setDataIdColumn(checkAuthDataRange.dataIdColumn())
                .setAuthorities(auths)
                .setMatchMethod(checkAuthDataRange.matchMethod())
                ;

        DataRangeContext.set(authRange);
        try {
            return proceedingJoinPoint.proceed();
        } finally {
            DataRangeContext.clear();
        }

    }

}
