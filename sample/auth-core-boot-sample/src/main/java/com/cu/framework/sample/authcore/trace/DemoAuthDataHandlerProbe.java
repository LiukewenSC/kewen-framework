package com.cu.framework.sample.authcore.trace;

import com.cu.framework.auth.core.data.AuthDataHandler;
import com.cu.framework.auth.core.entity.BaseAuth;

import java.util.Collection;

/**
 * 行级判定的观察探针：原样委托给框架的处理器，只额外记一笔"这次判定真的打了权限表"。
 * <p>
 * 规格里"超出上限直接失败，且系统不发起任何权限查询"这一半无法从响应码看出来，
 * 有了这个计数就能变成可断言的事实：101 条时探针没有记录，100 条时有一条
 *
 * @author kewen
 * @since 2026-10-20
 */
public class DemoAuthDataHandlerProbe implements AuthDataHandler {

    /** 被代理的框架处理器 */
    private final AuthDataHandler delegate;
    /** 观察台 */
    private final DemoSqlTrace demoSqlTrace;

    public DemoAuthDataHandlerProbe(AuthDataHandler delegate, DemoSqlTrace demoSqlTrace) {
        this.delegate = delegate;
        this.demoSqlTrace = demoSqlTrace;
    }

    /**
     * 委托执行判定，并记录本次真的去读了权限表
     */
    @Override
    public boolean hasDataOperateAuths(Collection<BaseAuth> auths, String businessFunction,
                                       Collection<Object> dataIds, String operate) {
        demoSqlTrace.recordJudge(businessFunction, operate, dataIds == null ? 0 : dataIds.size());
        return delegate.hasDataOperateAuths(auths, businessFunction, dataIds, operate);
    }

    @Override
    public void editDataAuths(String businessFunction, Object dataId, String operate, Collection<BaseAuth> auths) {
        demoSqlTrace.recordJudge(businessFunction, operate, auths == null ? 0 : auths.size());
        delegate.editDataAuths(businessFunction, dataId, operate, auths);
    }

    @Override
    public Collection<BaseAuth> getDataAuths(String businessFunction, Object dataId, String operate) {
        return delegate.getDataAuths(businessFunction, dataId, operate);
    }

    @Override
    public void deleteBusinessFunctionAuthByDataId(String businessFunction, Object dataId) {
        delegate.deleteBusinessFunctionAuthByDataId(businessFunction, dataId);
    }
}
