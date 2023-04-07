package com.kewen.framework.boot.authority.annotation.authedit;

import com.kewen.framework.base.authority.model.AuthorityObject;
import com.kewen.framework.base.authority.support.SysMenuAuthComposite;
import com.kewen.framework.base.common.exception.AuthorizationException;
import com.kewen.framework.boot.authority.annotation.CheckDataAuthEdit;
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
    private SysMenuAuthComposite sysMenuAuthComposite;

    @Pointcut("@annotation(com.kewen.framework.boot.authority.annotation.CheckDataAuthEdit)")
    public void pointcut(){

    }

    @Around(value = "pointcut() && @annotation(checkDataAuthEdit)",argNames = "joinPoint,authDataEdit")
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

        Long businessId = authDataEditBusiness.getBusinessId();
        AuthorityObject authorityObject = authDataEditBusiness.getAuthorityObject();
        String module = checkDataAuthEdit.module();
        String operate = checkDataAuthEdit.operate();

        sysMenuAuthComposite.editBusinessAuthority(businessId,module,operate,authorityObject);

        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            log.error("执行编辑失败"+throwable.getMessage(),throwable);
            throw new RuntimeException(throwable.getMessage(),throwable);
        }
    }

}
