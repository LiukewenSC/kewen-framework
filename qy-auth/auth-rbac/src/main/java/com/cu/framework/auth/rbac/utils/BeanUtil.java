package com.cu.framework.auth.rbac.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

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
    public static <T> T toBean(Object source, Class<T> clazz){
        String s = JSONObject.toJSONString(source);
        return JSONObject.parseObject(s, clazz);
    }
    /**
     * 将列表转换成指定类型的列表，基于 fastjson 序列化再反序列化，结果为空时返回空列表
     * @param source 源列表
     * @param clazz 元素的目标类型
     * @param <T> 返回元素泛型
     * @return 转换后的列表，不会为 null
     */
    public static <T> List<T> toList(List source, Class<T> clazz){
        String s = JSON.toJSONString(source);
        List<T> array = JSON.parseArray(s, clazz);
        if (CollectionUtils.isEmpty(array)){
            return Collections.emptyList();
        }
        return array;
    }
    /**
     * 将列表转换成指定类型的列表，并逐个元素交给 consumer 处理
     * @param source 源列表
     * @param clazz 元素的目标类型
     * @param consumer 对每个转换后元素的处理器
     * @param <T> 返回元素泛型
     * @return 转换并处理后的列表
     */
    public static <T> List<T> toList(List source, Class<T> clazz, Consumer<T> consumer){
        List<T> convert = toList(source, clazz);
        for (T t : convert) {
            consumer.accept(t);
        }
        return convert;
    }

    /**
     * 将 JSON 字符串解析为 TypeReference 指定的泛型类型对象
     * @param json JSON 字符串
     * @param typeReference 目标类型引用
     * @param <T> 返回泛型
     * @return 解析后的对象
     */
    public static <T> T parseObject(String json, TypeReference<T> typeReference){
        return JSONObject.parseObject(json,typeReference);
    }

    /**
     * 将对象序列化为 JSON 字符串
     * @param bean 待序列化的对象
     * @return JSON 字符串
     */
    public static String toJsonString(Object bean){
        return JSONObject.toJSONString(bean);
    }

    /**
     * 克隆Bean，复制Bean中的值，深度复制
     * @param source
     * @param <T>
     * @return
     */
    public static <S,T>  void copy(S source,T target){
        BeanUtils.copyProperties(source,target);
    }
    /**
     * 克隆对象，通过 JSON 序列化再反序列化为原对象的类实现
     * @param source 源对象
     * @param <T> 对象类型
     * @return 克隆出的新对象
     */
    public static <T> T clone(T source){
        return (T) toBean(source, source.getClass());
    }


    /**
     * 克隆集合
     * @param sources
     * @param <T>
     * @return
     */
    public static <T> List<T> cloneList(Collection<T> sources){
        ArrayList<T> lists = new ArrayList<>(sources.size());
        for (T source : sources) {
            lists.add(clone(source));
        }
        return lists;
    }


}
