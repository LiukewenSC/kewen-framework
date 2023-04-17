package com.kewen.framework.auth.sys.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @descrpition 部门
 * @author kewen
 * @since 2022-11-23 10:32
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dept {
    protected Long id;
    protected String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dept dept = (Dept) o;
        return Objects.equals(id, dept.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
