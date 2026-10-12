package com.cu.framework.basic.utils;

/**
 * 
 * @author kewen
 * @since 2023-04-19
 */
public interface CopyObject<P> {

    /**
     * 将参数对象的同名属性复制到当前对象（委托 BeanUtil.copy，即 Spring BeanUtils 属性复制）
     * @param p 属性来源对象
     */
    default void copyProperties(P p){
        BeanUtil.copy(p,this);
    }

}
