package com.kewen.framework.auth.sys.constant;

/**
 * @author kewen
 * @descrpition 菜单相关枚举类型
 * @since 2023-04-07
 */
public interface MenuTypeConstant {

    /**
     * 菜单类型
     */
    interface TYPE {
        // 菜单
        Integer MENU_TYPE_MENU = 1;
        // 按钮
        Integer MENU_TYPE_BUTTON = 2;
        // 外部链接
        Integer MENU_TYPE_EXTERNAL = 3;
    }

    /**
     * 菜单权限类型
     */
    interface AUTH_TYPE {
        // 基于父菜单权限
        Integer PARENT = 1;

        // 基于自身权限
        Integer SELF = 2;
    }

}
