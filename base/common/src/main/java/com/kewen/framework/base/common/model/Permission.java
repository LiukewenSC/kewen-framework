package com.kewen.framework.base.common.model;



import java.util.Objects;

/**
 * @descrpition 权限
 * @author kewen
 * @since 2022-11-23 10:32
 */
public class Permission {
    protected Integer id;
    protected String name;


    public Permission() {
    }

    public Permission(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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
        Permission that = (Permission) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
