package com.kewen.framework.base.common.model;

import java.util.Objects;

/**
 * @descrpition 部门
 * @author kewen
 * @since 2022-11-23 10:32
 */
public class Dept {
    protected Long id;
    protected String name;

    public Dept() {
    }

    public Dept(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
