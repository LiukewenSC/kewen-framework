package com.kewen.framework.auth.core.annotation.authedit;


import com.kewen.framework.auth.core.AuthHandler;
import com.kewen.framework.auth.core.annotation.CheckDataAuthEdit;
import com.kewen.framework.common.core.exception.AuthorizationException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
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
    private AuthHandler authHandler;

    @Pointcut("@annotation(com.kewen.framework.auth.core.annotation.CheckDataAuthEdit)")
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

        authHandler.editBusinessAuthority(
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
