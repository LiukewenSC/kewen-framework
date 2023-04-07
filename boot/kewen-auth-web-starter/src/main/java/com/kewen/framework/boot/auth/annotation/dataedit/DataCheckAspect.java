package com.kewen.framework.boot.auth.annotation.dataedit;

import com.kewen.framework.boot.auth.context.CurrentUserContext;
import com.kewen.framework.base.authority.support.SysMenuAuthComposite;
import com.kewen.framework.base.common.exception.AuthorizationException;
import com.kewen.framework.boot.auth.annotation.CheckDataEdit;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * @descrpition 校验业务权限切面，通过业务id和应用校验权限
 * @author kewen
 * @since 2022-11-25 9:28
 */
@Component
@Slf4j
@Aspect
public class DataCheckAspect {

    @Autowired
    private SysMenuAuthComposite menuAuthComposite;



    @Pointcut("@annotation(com.kewen.framework.boot.auth.annotation.CheckDataEdit)")
    public void pointcut(){}

    @Before(value = "pointcut() && @annotation(authAnn)", argNames = "joinPoint,authAnn")
    public void before(JoinPoint joinPoint, CheckDataEdit authAnn){
        log.info("校验用户权限，拦截方法:{}",joinPoint.toString());
        Object[] args = joinPoint.getArgs();
        if (args==null){
            throw new AuthorizationException("参数不能为空");
        }
        Optional<Object> first = Arrays.stream(args).filter(a -> a instanceof ApplicationBusiness).findFirst();
        if (!first.isPresent()){
            throw new AuthorizationException("参数没有找到接口ApplicationBusiness实现类");
        }
        ApplicationBusiness business = (ApplicationBusiness) first.get();
        Collection<String> auths = CurrentUserContext.getCurrentUserAuths();
        boolean hasAuth = menuAuthComposite.hasAuth(auths, authAnn.module(), authAnn.operate(), business.getBusinessId());
        if (!hasAuth){
            throw new AuthorizationException("权限校验不通过");
        }
    }

}
