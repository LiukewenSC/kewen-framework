package com.cu.framework.auth.core.menu;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * @author kewen
 * @since 2024-08-09
 */
@Data
@Accessors(chain = true)
public class MenuApiEntity<T> {
    /** 菜单API主键ID，入库前由 MenuApiService#generateId 生成 */
    T id;
    /** 请求路径，菜单API的唯一标识，equals / hashCode 基于此字段 */
    String path;
    /** 菜单名称，方法级菜单为 控制器名>方法菜单名 形式 */
    String name;
    /** 上级菜单ID，根据 parentPath 对应的菜单取得，无上级时为根节点ID */
    T parentId;
    /** 上级菜单路径，主要用于根据此路径来寻找父id */
    String parentPath;

    /**
     * 创建不带上级路径的菜单API实体
     * @param path 请求路径
     * @param name 菜单名称
     * @return 菜单API实体
     */
    public static MenuApiEntity of(String path, String name) {
        return of(path, name, null);
    }

    /**
     * 创建菜单API实体
     * @param path 请求路径
     * @param name 菜单名称
     * @param parentPath 上级菜单路径，可为 null
     * @return 菜单API实体
     */
    public static MenuApiEntity of(String path, String name, String parentPath) {
        MenuApiEntity apiEntity = new MenuApiEntity();
        apiEntity.setPath(path);
        apiEntity.setName(name);
        apiEntity.setParentPath(parentPath);
        return apiEntity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuApiEntity apiEntity = (MenuApiEntity) o;
        return Objects.equals(path, apiEntity.path);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(path);
    }
}
