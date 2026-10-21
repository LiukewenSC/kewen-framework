package com.cu.framework.auth.core.data;

import com.cu.framework.auth.core.entity.BaseAuth;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 实现了 AuthData配置相关的数据处理
 * @author kewen
 * @since 2024-08-14
 */

public class JdbcAuthDataHandler implements AuthDataHandler {

    /** 数据权限持久化组件，执行权限表的增删查 */

    private final JdbcAuthDataPersistent jdbcAuthDataPersistent;

    public JdbcAuthDataHandler(JdbcAuthDataPersistent jdbcAuthDataPersistent) {
        this.jdbcAuthDataPersistent = jdbcAuthDataPersistent;
    }

    @Override
    public void editDataAuths(String businessFunction, Object dataId, String operate, Collection<BaseAuth> baseAuths) {
        //这里为了保证baseAuths不为空，需要为空的时候设置为emptyList，但是直接赋值 lambda中会报错
        Collection<BaseAuth> baseAuthsTemplate = baseAuths==null?Collections.emptyList():baseAuths;

        List<AuthDataDO> dbList = jdbcAuthDataPersistent.listAuthData(businessFunction, Collections.singletonList(dataId), operate);
        //需要添加的
        List<AuthDataDO> adds = baseAuthsTemplate.stream()
                .filter(
                        as -> dbList.stream().noneMatch(db -> db.getAuthority().equals(as.getAuth()))
                ).map(as -> new AuthDataDO(
                        businessFunction,
                        dataId,
                        operate,
                        as.getAuth(),
                        as.getDescription()
                        )
                ).collect(Collectors.toList());
        //需要移除的
        List<Object> removes = dbList.stream()
                .filter(db -> !baseAuthsTemplate.contains(new BaseAuth(db.getAuthority(), db.getDescription())))
                .map(AuthDataDO::getId)
                .collect(Collectors.toList());

        //移除原有的
        if (!CollectionUtils.isEmpty(removes)) {
            int i = jdbcAuthDataPersistent.deleteBatchAuthData(removes);
        }
        //批量插入新的
        if (!CollectionUtils.isEmpty(adds)) {
            boolean b = jdbcAuthDataPersistent.insertBatchAuthData(adds);
        }
    }

    /**
     * 逐个判定这些业务数据在指定槽位下是否都有操作权限，任一未命中即返回 false。
     * <p>
     * 权限串的比对留在内存：用户权限集合的规模不可控，不放进查询条件。
     * 数据ID按字符串归一化后比对，避免同一主键的不同类型形态被当成两条数据。
     * @param auths 用户权限
     * @param businessFunction 模块
     * @param dataIds 业务id集合
     * @param operate 操作
     * @return 是否全部数据都有权限
     */
    @Override
    public boolean hasDataOperateAuths(Collection<BaseAuth> auths, String businessFunction, Collection<Object> dataIds, String operate) {
        if (CollectionUtils.isEmpty(auths) || CollectionUtils.isEmpty(dataIds)) {
            return false;
        }
        List<AuthDataDO> authDataList = jdbcAuthDataPersistent.listAuthData(businessFunction, dataIds, operate);

        Set<String> userAuths = auths.stream()
                .map(BaseAuth::getAuth)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, Set<String>> authoritiesByDataId = new HashMap<>();
        for (AuthDataDO authData : authDataList) {
            authoritiesByDataId.computeIfAbsent(normalize(authData.getDataId()), key -> new HashSet<>())
                    .add(authData.getAuthority());
        }

        for (Object dataId : dataIds) {
            Set<String> authorities = authoritiesByDataId.get(normalize(dataId));
            if (authorities == null || Collections.disjoint(authorities, userAuths)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Collection<BaseAuth> getDataAuths(String businessFunction, Object dataId, String operate) {
        List<AuthDataDO> authDataDOS = jdbcAuthDataPersistent.listAuthData(businessFunction, Collections.singletonList(dataId), operate);
        return authDataDOS.stream().map(
                a -> new BaseAuth(a.getAuthority(), a.getDescription())
        ).collect(Collectors.toList());
    }

    @Override
    public void deleteBusinessFunctionAuthByDataId(String businessFunction, Object dataId) {
        int i = jdbcAuthDataPersistent.deleteBusinessFunctionDataIdAuth(businessFunction, dataId);
    }

    private static String normalize(Object dataId) {
        return String.valueOf(dataId);
    }
}
