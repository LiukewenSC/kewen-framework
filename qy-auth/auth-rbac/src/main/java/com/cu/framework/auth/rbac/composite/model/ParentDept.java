package com.cu.framework.auth.rbac.composite.model;

import com.cu.framework.auth.core.entity.AbstractIdNameFlagAuthEntity;
import com.cu.framework.auth.core.entity.AuthConstant;

public class ParentDept extends AbstractIdNameFlagAuthEntity<Long> {
    private static final long serialVersionUID = 1L;

    private int level;

    public ParentDept() {
        super();
    }

    public ParentDept(Long id , String name,int level) {
        super();
        this.id=id;
        this.name=name;
        this.level=level;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String flag() {
        return "RELATIONDEPT";
    }
}
