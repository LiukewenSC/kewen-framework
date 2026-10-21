package com.cu.framework.auth.core.entity;

import java.util.Objects;

/**
 * 扁平化权限字符串
 * 定义了基础的格式，所有的权限都按照此来转换
 * @author kewen
 * @descrpition
 * @since 2023-12-26
 */

public class FlatteningAuthority {
    /** 权限字符串，唯一标识一条权限，equals / hashCode 均基于此字段 */
    private String auth;
    /** 权限描述，与权限字符串格式对应的可读名称部分 */
    private String description;

    public FlatteningAuthority() {
    }

    public FlatteningAuthority(String auth, String description) {
        this.auth = auth;
        this.description = description;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FlatteningAuthority)) return false;
        FlatteningAuthority flatteningAuthority = (FlatteningAuthority) o;
        return Objects.equals(auth, flatteningAuthority.auth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(auth);
    }

    @Override
    public String toString() {
        return "BaseAuth{" +
                "auth='" + auth + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
