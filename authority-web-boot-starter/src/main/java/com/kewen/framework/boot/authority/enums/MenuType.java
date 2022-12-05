package com.kewen.framework.boot.authority.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * @descrpition 菜单类型
 * @author kewen
 * @since 2022-12-01 10:33
 */
public enum  MenuType implements IEnum<Integer> {

    /**
     * 菜单
     */
    MENU(1),
    /**
     * 按钮
     */
    BUTTON(2),
    /**
     * 外部链接
     */
    EXTERNAL(3),

    ;
    private final Integer value;

    MenuType(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
