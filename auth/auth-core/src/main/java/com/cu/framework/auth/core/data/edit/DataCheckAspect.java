package com.cu.framework.auth.core.data.edit;


import com.cu.framework.auth.core.AuthCurrentUserContextHolder;
import com.cu.framework.auth.core.AuthDataOperation;
import com.cu.framework.auth.core.data.AuthDataHandler;
import com.cu.framework.auth.core.entity.FlatteningAuthority;
import com.cu.framework.auth.core.exception.AuthException;
import com.cu.framework.auth.core.exception.AuthServiceException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @descrpition 校验业务权限切面，通过业务id和应用校验权限
 * <p>
 * 判定单位是业务数据ID：切面收集实参中所有 {@link DataIdCarrier}（含一层数组与集合元素）交出的ID，
 * 去重后逐个判定，任一未命中即整体拒绝。
 * @author kewen
 * @since 2022-11-25
 */
@Aspect
public class DataCheckAspect {

    private static final Logger log = LoggerFactory.getLogger(DataCheckAspect.class);

    /**
     * 单次校验允许的业务数据ID数量上限（去重后）。
     * 超出即拒绝判定，由调用方自行切片，避免一次校验退化成不可控规模的查询
     */
    private static final int MAX_DATA_IDS = 100;

    /** 数据权限处理器，用于校验当前用户对数据的操作权限 */
    private final AuthDataHandler annotationAuthHandler;
    private final AuthCurrentUserContextHolder authCurrentUserContextHolder;

    public DataCheckAspect(AuthDataHandler annotationAuthHandler,
                           AuthCurrentUserContextHolder authCurrentUserContextHolder) {
        this.annotationAuthHandler = annotationAuthHandler;
        this.authCurrentUserContextHolder = authCurrentUserContextHolder;
    }

    /**
     * 切入标注了 {@link com.cu.framework.auth.core.AuthDataOperation} 注解的方法
     */
    @Pointcut("@annotation(com.cu.framework.auth.core.AuthDataOperation)")
    public void pointcut(){}

    /**
     * 目标方法执行前校验数据操作权限：
     * 收集方法参数中全部 {@link DataIdCarrier} 交出的业务数据ID，
     * 结合注解声明的业务功能与操作类型逐个判定，任一未命中即整体拒绝
     * @param joinPoint 被拦截的方法
     * @param authAnn 方法上的 @AuthDataOperation 注解
     * @throws AuthServiceException 参数中没有承载对象、ID集合为空、含空值或超出数量上限
     * @throws AuthException 权限判定不通过
     */
    @Before(value = "pointcut() && @annotation(authAnn)", argNames = "joinPoint,authAnn")
    public void before(JoinPoint joinPoint, AuthDataOperation authAnn){
        if (log.isDebugEnabled()) {
            log.debug("校验用户权限，拦截方法:{}", joinPoint);
        }
        checkDataOperationAuths(joinPoint.getArgs(), authAnn.businessFunction(), authAnn.operate());
    }

    /**
     * 收集并判定本次调用涉及的全部业务数据ID
     * @param args 被拦截方法的实参
     * @param businessFunction 注解声明的业务功能
     * @param operate 注解声明的操作类型
     * @throws AuthServiceException 无法判定（没有承载对象、ID为空或超上限）
     * @throws AuthException 权限判定不通过
     */
    void checkDataOperationAuths(Object[] args, String businessFunction, String operate) {
        Collection<Object> dataIds = collectDataIds(args);
        Collection<FlatteningAuthority> auths = authCurrentUserContextHolder.getAuths();
        boolean hasAuth = annotationAuthHandler.hasDataOperateAuths(auths, businessFunction, dataIds, operate);
        if (!hasAuth) {
            log.info("数据操作权限校验不通过，业务功能<{}>操作<{}>涉及数据ID<{}>条", businessFunction, operate, dataIds.size());
            throw new AuthException("权限校验不通过");
        }
    }

    /**
     * 汇总实参中所有承载对象交出的业务数据ID并按归一化值去重。
     * 承载对象的识别只到一层：实参本身、实参数组或集合的元素，更深的嵌套由承载对象自行摊平
     * @param args 被拦截方法的实参
     * @return 去重后的业务数据ID集合
     * @throws AuthServiceException 没有承载对象、ID集合为空、含空值或超出数量上限
     */
    private Collection<Object> collectDataIds(Object[] args) {
        List<DataIdCarrier<?>> carriers = new ArrayList<>();
        if (args == null) {
            throw new AuthServiceException("未提供业务数据ID");
        }
        for (Object arg : args) {
            addCarrier(carriers, arg);
            if (arg instanceof Collection) {
                for (Object item : (Collection<?>) arg) {
                    addCarrier(carriers, item);
                }
            } else if (arg instanceof Object[]) {
                for (Object item : (Object[]) arg) {
                    addCarrier(carriers, item);
                }
            }
        }
        if (carriers.isEmpty()) {
            throw new AuthServiceException("未提供业务数据ID");
        }

        Map<String, Object> distinct = new LinkedHashMap<>();
        for (DataIdCarrier<?> carrier : carriers) {
            Collection<?> ids = carrier.getDataIds();
            if (ids == null) {
                continue;
            }
            for (Object id : ids) {
                if (id == null) {
                    throw new AuthServiceException("业务数据ID不能为空");
                }
                distinct.put(String.valueOf(id), id);
            }
        }
        if (distinct.isEmpty()) {
            throw new AuthServiceException("未提供业务数据ID");
        }
        if (distinct.size() > MAX_DATA_IDS) {
            throw new AuthServiceException("业务数据ID数量超过上限" + MAX_DATA_IDS + "，请分批提交");
        }
        return distinct.values();
    }

    private void addCarrier(List<DataIdCarrier<?>> carriers, Object candidate) {
        if (candidate instanceof DataIdCarrier) {
            carriers.add((DataIdCarrier<?>) candidate);
        }
    }

}
