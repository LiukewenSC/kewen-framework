package com.kewen.framework.base.common.model;

import java.util.Objects;

/**
 * @descrpition 
 * @author kewen
 * @since 2022-11-29 9:12
 */
public class DeptPrimary extends Dept {

    protected Boolean isPrimary;

    public DeptPrimary() {
    }

    public DeptPrimary(Integer id, String name, Boolean isPrimary) {
        super(id, name);
        this.isPrimary = isPrimary;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DeptPrimary that = (DeptPrimary) o;
        return Objects.equals(isPrimary, that.isPrimary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isPrimary);
    }
}
