package com.cu.framework.auth.core.data.edit;


import com.cu.framework.auth.core.AuthDataOperation;
import com.cu.framework.auth.core.data.AuthDataHandler;
import com.cu.framework.auth.core.AuthCurrentUserContextHolder;
import com.cu.framework.auth.core.entity.IAuthObject;
import com.cu.framework.auth.core.exception.AuthException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;

/**
 * @descrpition 校验业务权限切面，通过业务id和应用校验权限
 * @author kewen
 * @since 2022-11-25
 */
@Aspect
public class DataCheckAspect {

    private static final Logger log = LoggerFactory.getLogger(DataCheckAspect.class);

    /** 数据权限处理器，用于校验当前用户对数据的操作权限 */
    private AuthDataHandler annotationAuthHandler;
    private AuthCurrentUserContextHolder authCurrentUserContextHolder;



    /**
     * 切入标注了 {@link com.cu.framework.auth.core.AuthDataOperation} 注解的方法
     */
    @Pointcut("@annotation(com.cu.framework.auth.core.AuthDataOperation)")
    public void pointcut(){}

    /**
     * 目标方法执行前校验数据操作权限：
     * 从方法参数中找到 {@link IdDataEdit} 实现取其业务数据ID，
     * 结合注解声明的业务功能与操作类型，校验当前登录用户是否有权限
     * @param joinPoint 被拦截的方法
     * @param authAnn 方法上的 @AuthDataOperation 注解
     * @throws AuthException 参数中没有 IdDataEdit 实现或权限校验不通过时抛出
     */
    @Before(value = "pointcut() && @annotation(authAnn)", argNames = "joinPoint,authAnn")
    public void before(JoinPoint joinPoint, AuthDataOperation authAnn){
        log.info("校验用户权限，拦截方法:{}",joinPoint.toString());
        Object[] args = joinPoint.getArgs();
        if (args==null){
            throw new AuthException("参数不能为空");
        }
        Optional<Object> first = Arrays.stream(args).filter(a -> a instanceof IdDataEdit).findFirst();
        if (!first.isPresent()){
            throw new AuthException("参数没有找到接口 IdDataEdit 实现类");
        }
        IdDataEdit business = (IdDataEdit) first.get();
        IAuthObject authObject = authCurrentUserContextHolder.getAuthObject();
        boolean hasAuth = annotationAuthHandler.hasDataOperateAuths(authObject, authAnn.businessFunction(), business.getDataId(),authAnn.operate());
        if (!hasAuth){
            throw new AuthException("权限校验不通过");
        }
    }

    public void setAnnotationAuthHandler(AuthDataHandler annotationAuthHandler) {
        this.annotationAuthHandler = annotationAuthHandler;
    }
    public void setAuthCurrentUserContextHolder(AuthCurrentUserContextHolder authCurrentUserContextHolder) {
        this.authCurrentUserContextHolder = authCurrentUserContextHolder;
    }

}
