package com.kewen.framework.common.core.utils;

/**
 * 
 * @author kewen
 * @since 2023-04-19
 */
public interface CopyObject<P> {

    default void copyParent(P p){
        BeanUtil.copy(p,this);
    }

}
