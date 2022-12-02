package com.kewen.common.utils;

import com.alibaba.fastjson2.JSONObject;

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


}
