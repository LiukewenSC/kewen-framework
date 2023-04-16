package com.kewen.framework.base.auth.annotation.authedit;


import com.kewen.framework.base.auth.AuthEntity;
import com.kewen.framework.base.auth.AuthHandler;
import com.kewen.framework.base.common.exception.AuthorizationException;
import com.kewen.framework.base.auth.annotation.CheckDataAuthEdit;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author kewen
 * @descrpition 编辑权限切面
 * @since 2022-12-19 14:26
 */
@Slf4j
@Component
@Aspect
public class AuthDataEditAspect {

    @Autowired
    private AuthHandler sysMenuAuthComposite;

    @Pointcut("@annotation(com.kewen.framework.base.auth.annotation.CheckDataAuthEdit)")
    public void pointcut(){

    }

    @Around(value = "pointcut() && @annotation(checkDataAuthEdit)",argNames = "joinPoint,checkDataAuthEdit")
    @Transactional
    public Object around(ProceedingJoinPoint joinPoint, CheckDataAuthEdit checkDataAuthEdit){
        log.info("编辑业务权限，拦截方法:{}",joinPoint.toString());
        Object[] args = joinPoint.getArgs();
        if (args==null){
            throw new AuthorizationException("参数不能为空");
        }
        Optional<Object> first = Arrays.stream(args).filter(a -> a instanceof AuthDataEditBusiness).findFirst();
        if (!first.isPresent()){
            throw new AuthorizationException("参数没有找到接口 AuthDataEditBusiness 实现类");
        }
        AuthDataEditBusiness authDataEditBusiness = (AuthDataEditBusiness) first.get();

        sysMenuAuthComposite.editBusinessAuthority(
                authDataEditBusiness.getBusinessId(),
                checkDataAuthEdit.module(),
                checkDataAuthEdit.operate(),
                authDataEditBusiness.getAuthEntities());

        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            log.error("执行编辑失败"+throwable.getMessage(),throwable);
            throw new RuntimeException(throwable.getMessage(),throwable);
        }
    }

}
