package com.kewen.framework.base.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
    public static <T> List<T> toList(List source,Class<T> clazz){
        String s = JSON.toJSONString(source);
        return JSON.parseArray(s, clazz);
    }


    /**
     * 克隆Bean，复制Bean中的值，深度复制
     * @param source
     * @param <T>
     * @return
     */
    public static <T> T copy(T source){
        return (T)toBean(source, source.getClass());
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

    public static String toJsonString(Object bean){
        return JSONObject.toJSONString(bean);
    }

    /**
     * 设置 常量的值，有点剑走偏锋，尽量不要使用，否则程序里你可能看不懂
     */
    public static void setFinalField(Object source,String fieldName,Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = source.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        int nonFinal = field.getModifiers() & (~Modifier.FINAL); // 位操作去掉 final
        Field modifiers = Field.class.getDeclaredField("modifiers"); // 用反射拿到变量的修饰符
        modifiers.setAccessible(true);
        modifiers.setInt(field, nonFinal);
        field.set(source,value);
    }


}
