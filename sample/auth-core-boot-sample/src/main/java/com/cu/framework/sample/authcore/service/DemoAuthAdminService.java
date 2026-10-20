package com.cu.framework.sample.authcore.service;

import com.cu.framework.auth.core.AuthDataAdaptor;
import com.cu.framework.auth.core.entity.BaseAuth;
import com.cu.framework.sample.authcore.authobject.DemoAuthObject;
import com.cu.framework.sample.authcore.authobject.RawAuthObject;
import com.cu.framework.sample.authcore.entity.AuthDataRow;
import com.cu.framework.sample.authcore.mapper.AuthDataViewMapper;
import com.cu.framework.sample.authcore.mapper.ExecutionLogMapper;
import com.cu.framework.sample.authcore.model.DemoRequests.GrantReq;
import com.cu.framework.sample.authcore.model.DemoRequests.RemoveReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 不经注解、直接在 Service 层调用 {@link AuthDataAdaptor} 维护数据权限。
 * <p>
 * 这三个方法构成了行级判定的闭环：授权后同一个调用从拒绝变放行，清空后又回到拒绝，
 * 填充则把某个槽位上已配置的权限读回来。演示用探针模块 probe，不碰真实业务数据
 *
 * @author kewen
 * @since 2026-10-20
 */
@Service
public class DemoAuthAdminService {

    @Autowired
    private AuthDataAdaptor authDataAdaptor;
    @Autowired
    private AuthDataViewMapper authDataViewMapper;
    @Autowired
    private ExecutionLogMapper executionLogMapper;

    /**
     * 把目标数据在指定槽位上的权限整槽设为给定的那一条。
     * <p>
     * 框架按 diff 落库：给定的没有、库里有的会被删掉，库里没有的会被插入。
     * 权限串与描述都原样作为数据绑定，含引号也不会改变语句本身
     *
     * @param req 授权请求
     * @return 写入后该槽位的实际内容
     */
    public Map<String, Object> grant(GrantReq req) {
        RawAuthObject target = new RawAuthObject(
                Collections.singletonList(new BaseAuth(req.getAuthStr(), req.getAuthDesc())));
        authDataAdaptor.editDataAuths(req.getBizModule(), req.getBizDataId(), req.getAction(), target);
        executionLogMapper.insert("authData.grant", String.valueOf(req.getBizDataId()), req.getBizModule());
        return slotView(req.getBizModule(), req.getAction(), req.getBizDataId());
    }

    /**
     * 清空该条数据在所有槽位上的权限——业务里删除这条数据时要做的事
     *
     * @param req 清空请求
     * @return 清空后该业务功能下的剩余行数
     */
    public Map<String, Object> remove(RemoveReq req) {
        authDataAdaptor.deleteBusinessFunctionAuthByDataId(req.getBizModule(), req.getBizDataId());
        executionLogMapper.insert("authData.remove", String.valueOf(req.getBizDataId()), req.getBizModule());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("module", req.getBizModule());
        result.put("dataId", req.getBizDataId());
        result.put("remainingRowsOfModule", authDataViewMapper.listByModule(req.getBizModule()).size());
        return result;
    }

    /**
     * 把某个槽位上已配置的权限读进一个权限集合体
     *
     * @param bizModule 业务功能
     * @param action    操作类型
     * @param bizDataId 业务数据ID
     * @return 槽位视图，含读回的权限串
     */
    public Map<String, Object> fill(String bizModule, String action, Long bizDataId) {
        DemoAuthObject authObject = new DemoAuthObject();
        authDataAdaptor.fillDataAuths(bizModule, bizDataId, action, authObject);
        Map<String, Object> result = slotView(bizModule, action, bizDataId);
        Collection<BaseAuth> filled = authObject.listBaseAuth();
        result.put("filledIntoAuthObject", filled);
        return result;
    }

    /**
     * 读回某个槽位的内容，供调用后立即核对
     *
     * @param bizModule 业务功能
     * @param action    操作类型
     * @param bizDataId 业务数据ID
     * @return 槽位视图
     */
    public Map<String, Object> slotView(String bizModule, String action, Long bizDataId) {
        List<AuthDataRow> rows = authDataViewMapper.listSlot(bizModule, action, bizDataId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("module", bizModule);
        result.put("action", action);
        result.put("dataId", bizDataId);
        result.put("rows", rows);
        result.put("rowCount", rows.size());
        return result;
    }
}
