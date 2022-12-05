package com.kewen.framework.base.common.utils;

import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author kewen
 * @descrpition
 * @since 2022-11-30 20:31
 */
public class BeanUtil {

    /**
     * 转换成 bean 用fastjson，不用再做字段类型匹配，快于spring和common-lang包
     * @param source 源
     * @param clazz 转换的类型
     * @param <T> 返回泛型
     * @return
     */
    public static <T> T toBean(Object source,Class<T> clazz){
        String s = JSONObject.toJSONString(source);
        return JSONObject.parseObject(s, clazz);
    }


    /**
     * 克隆Bean，复制Bean中的值，深度复制
     * @param source
     * @param <T>
     * @return
     */
    public static <T> T copy(T source){
        String jsonString = com.alibaba.fastjson.JSONObject.toJSONString(source);
        return (T) com.alibaba.fastjson.JSONObject.parseObject(jsonString, source.getClass());
    }

    /**
     * 克隆集合
     * @param sources
     * @param <T>
     * @return
     */
    public static <T> List<T> copyList(Collection<T> sources){
        ArrayList<T> lists = new ArrayList<>(sources.size());
        for (T source : sources) {
            lists.add(copy(source));
        }
        return lists;
    }


}
