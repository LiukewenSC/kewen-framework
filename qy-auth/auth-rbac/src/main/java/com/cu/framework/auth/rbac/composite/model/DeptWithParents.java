package com.cu.framework.auth.rbac.composite.model;

import com.cu.framework.auth.core.entity.AbstractIdNameFlagAuthEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kewen
 * @descrpition
 * @since 2023-12-26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DeptWithParents extends Dept {

    private static final long serialVersionUID = 6638347959892346419L;

    private List<ParentDept> parents = new ArrayList<>();

    public DeptWithParents() {
        super();
    }

    public DeptWithParents(Long id , String name) {
        this.id=id;
        this.name=name;
    }


    /**
     * 权限标记
     * @return
     */
    @Override
    public String flag() {
        return "DEPT";
    }
}
