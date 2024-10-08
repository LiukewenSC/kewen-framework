package com.kewen.framework.auth.sys.model;

import com.kewen.framework.auth.core.model.AuthEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @descrpition 权限字符串结构
 * @author kewen
 * @since 2022-11-23 10:32
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysAuthority implements AuthEntity {
    /**
     * 权限字符串
     */
    protected String authority;
    /**
     * 权限字符串描述
     */
    protected String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SysAuthority sysAuthority1 = (SysAuthority) o;
        return Objects.equals(authority, sysAuthority1.authority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authority);
    }

    @Override
    public String getAuth() {
        return authority;
    }
}
