package com.cu.framework.auth.core.entity;

import cn.hutool.core.util.ClassUtil;
import com.cu.framework.auth.core.exception.AuthEntityException;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象的权限配置对象，此类主要定义了 BaseAuth 与  IFlagAuthEntity 的转换规则，
 * 子类主要关注定义的权限实体与AbstractAuthEntity之间的转换关系即可
 *
 * @author kewen
 * @since 2023-12-27
 */

public abstract class AbstractAuthObject implements IAuthObject {


    /**
     * 保存的 IAuthEntityProvider 与 权限标记的映射关系
     */
    protected static final Map<String, Class<? extends IFlagAuthEntity>> authEntityMap = new ConcurrentHashMap<>();

    //初始化类的操作，扫描所有的IAuthEntityProvider并保存到 map中
    static {
        try {
            Set<Class<?>> classes = ClassUtil.scanPackageBySuper(null, IFlagAuthEntity.class);
            try {
                for (Class<?> aClass : classes) {
                    if (ClassUtil.isAbstractOrInterface(aClass)) {
                        continue;
                    }
                    IFlagAuthEntity abstractAuthEntity = (IFlagAuthEntity) aClass.newInstance();
                    String flag = abstractAuthEntity.flag();
                    if (StringUtils.isEmpty(flag)) {
                        continue;
                    }
                    authEntityMap.put(flag, abstractAuthEntity.getClass());
                }
            } catch (InstantiationException | IllegalAccessException e) {
                throw new AuthEntityException("AbstractIdNameAuthEntity 子类需要保留无参构造", e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 将 BaseAuth 解析为对应的权限实体：
     * 取权限字符串中 AUTH_SPLIT 之前的部分作为权限标记 flag，
     * 从启动时扫描注册的 authEntityMap 中找到实体类，实例化并填充属性值
     * @param flatteningAuthority 基础权限
     * @return 解析出的权限实体
     * @throws AuthEntityException 权限标记未注册或实体实例化失败时抛出
     */
    public IFlagAuthEntity parseBaseAuth(FlatteningAuthority flatteningAuthority) {
        String[] strings = flatteningAuthority.getAuth().split(AuthConstant.AUTH_SPLIT);
        String flag = strings[0];
        Class<? extends IFlagAuthEntity> aClass = authEntityMap.get(flag);
        if (aClass == null) {
            throw new AuthEntityException("权限标记错误: "+flag);
        }
        IFlagAuthEntity abstractAuthEntity = null;
        try {
            abstractAuthEntity = aClass.newInstance();
        } catch (Exception e) {
            throw new AuthEntityException("实例化["+aClass.getName()+"]错误",e);
        }
        abstractAuthEntity.setProperties(flatteningAuthority);
        return abstractAuthEntity;
    }

    @Override
    public void setProperties(Collection<FlatteningAuthority> auths) {
        List<IFlagAuthEntity> abstractAuthEntities = new ArrayList<>();
        for (FlatteningAuthority auth : auths) {
            abstractAuthEntities.add(parseBaseAuth(auth));
        }
        setBaseAuth(abstractAuthEntities);
    }

    /**
     * 子类实现的回调钩子，将解析完成的权限实体列表设置到具体的权限集合体上
     * @param abstractAuthEntities 由 BaseAuth 解析出的权限实体列表
     */
    protected abstract void setBaseAuth(List<IFlagAuthEntity> abstractAuthEntities);
}
