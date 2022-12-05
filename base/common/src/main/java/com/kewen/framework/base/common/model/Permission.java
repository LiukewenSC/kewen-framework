package com.kewen.framework.base.common.model;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @descrpition 权限
 * @author kewen
 * @since 2022-11-23 10:32
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Permission {
    protected Long id;
    protected String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
