package com.kewen.framework.boot.authority.advance.authedit;

import com.kewen.framework.base.authority.model.AuthorityObject;
import com.kewen.framework.base.authority.service.SysMenuAuthUnify;
import com.kewen.framework.base.common.exception.AuthorizationException;
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
    private SysMenuAuthUnify sysMenuAuthUnify;

    @Pointcut("@annotation(com.kewen.framework.boot.authority.advance.authedit.AuthDataEdit)")
    public void pointcut(){

    }

    @Around(value = "pointcut() && @annotation(authDataEdit)",argNames = "joinPoint,authDataEdit")
    @Transactional
    public Object around(ProceedingJoinPoint joinPoint, AuthDataEdit authDataEdit){
        log.info("编辑业务权限，拦截方法:{}",joinPoint.toString());
        Object[] args = joinPoint.getArgs();
        if (args==null){
            throw new AuthorizationException("参数不能为空");
        }
        Optional<Object> first = Arrays.stream(args).filter(a -> a instanceof AuthDataEditBusiness).findFirst();
        if (!first.isPresent()){
            throw new AuthorizationException("参数没有找到接口 AuthEditApplicationBusiness 实现类");
        }
        AuthDataEditBusiness authDataEditBusiness = (AuthDataEditBusiness) first.get();

        Integer businessId = authDataEditBusiness.getBusinessId();
        AuthorityObject authorityObject = authDataEditBusiness.getAuthorityObject();
        String module = authDataEdit.module();
        String operate = authDataEdit.operate();

        sysMenuAuthUnify.editBusinessAuthority(businessId,module,operate,authorityObject);

        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            log.error("执行编辑失败"+throwable.getMessage(),throwable);
            throw new RuntimeException(throwable.getMessage(),throwable);
        }
    }

}
