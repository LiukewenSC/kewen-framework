package com.kewen.framework.boot.authority.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * @descrpition 菜单的权限类型
 * @author kewen
 * @since 2022-12-01 10:36
 */
public enum  MenuAuthType implements IEnum<Integer> {
    /**
     * 基于父菜单权限
     */
    PARENT(1),
    /**
     *  基于本身权限
     */
    SELF(2),

    ;
    private final Integer value;

    MenuAuthType(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
