package com.cu.framework.auth.core.entity;

/**
 * 权限常量
 * @author kewen
 * @descrpition
 * @since 2023-12-26
 */
public class AuthConstant {
    /**
     * 全局权限分隔符常量，可以修改，建议修改时在系统启动时处理好
     * 注意不要使用 string.split() 正则表达式中有歧义的分隔符，否则会在逆向得到 权限实体时错误
     */
    public static String AUTH_SPLIT="__";
    /**
     * 权限次级分隔符，用于分隔组合权限标记中的多个组成部分以及各位置的 ID、名字，
     * 如 DEPT_ROLE__1_1 中标记 DEPT_ROLE 与值 1_1 内部的 _
     */
    public static String AUTH_SUB_SPLIT="_";
}
