package com.cu.framework.auth.core.entity;

import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Collection;

/**
 * 权限集合体接口，存放权限集合体的结构就在这里
 * 如 users roles depts positions 等
 * @author kewen
 * @since 2023-12-27
 */
public interface IAuthObject extends Serializable {

    /**
     * 判断权限集合体中是否没有任何基础权限
     * @return listBaseAuth 为空时返回 true
     */
    default boolean isEmpty(){
        return CollectionUtils.isEmpty(listBaseAuth());
    }
    /**
     * 获取得到基本权限
     * @return
     */
    Collection<FlatteningAuthority> listBaseAuth();

    /**
     * 通过BaseAuth设置权限体的权限属性值
     * @param flatteningAuthorities
     */
    void setProperties(Collection<FlatteningAuthority> flatteningAuthorities);

}
