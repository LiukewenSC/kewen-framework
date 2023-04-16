package com.kewen.framework.base.auth;

import com.kewen.framework.base.common.model.IUser;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *  用户信息，带权限
 *  继承hashmap的目的是为了更方便的设置属性值，此对象返回给前端也是有既定的结构的
 * @author kewen
 * @since 2023-04-10
 */
public class CommonAuthUserInfo extends HashMap<String,List<AuthEntity>> implements IUser , AuthUserInfo {

    /**
     * 用户id
     */
    protected Long id;
    /**
     * 用户名
     */
    protected String name;


    /**
     * 拥有的权限属性名集合，只能在此对应的key范围内，
     */
    protected List<String> propertyNames= Collections.emptyList();


    public void setAuth(String propertyName, List<AuthEntity> values){
        this.put(propertyName,values);
    }
    public List<AuthEntity> getAuth(String propertyName){
        return this.get(propertyName);
    }



    public List<String> getPropertyNames() {
        return propertyNames;
    }

    public void setPropertyNames(List<String> propertyNames) {
        this.propertyNames = propertyNames;
    }


    @Override
    public List<AuthEntity> getAuthorities() {
        return this.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }


    @Override
    public Long getUserId() {
        return id;
    }

    @Override
    public String getUserName() {
        return name;
    }

    private void validProperty(String propertyName){
        boolean contains = propertyNames.contains(propertyName);
        if (!contains){
            throw new RuntimeException("this property is not support");
        }
    }
    private void validProperties(Collection<String> propertyNames){
        boolean contains = propertyNames.containsAll(propertyNames);
        if (!contains){
            throw new RuntimeException("this property is not support");
        }
    }
    private void unSupportMethod(){
        throw new UnsupportedOperationException("un supported");
    }


    /*HashMap相关的处理*/
    public CommonAuthUserInfo(Map<? extends String, ? extends List<AuthEntity>> m) {
        unSupportMethod();
    }

    @Override
    public List<AuthEntity> put(String key, List<AuthEntity> value) {
        validProperty(key);
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends List<AuthEntity>> m) {
        unSupportMethod();
    }

    @Override
    public List<AuthEntity> putIfAbsent(String key, List<AuthEntity> value) {
        validProperty(key);
        return super.putIfAbsent(key, value);
    }

    @Override
    public List<AuthEntity> computeIfAbsent(String key, Function<? super String, ? extends List<AuthEntity>> mappingFunction) {
        validProperty(key);
        return super.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public List<AuthEntity> computeIfPresent(String key, BiFunction<? super String, ? super List<AuthEntity>, ? extends List<AuthEntity>> remappingFunction) {
        validProperty(key);
        return super.computeIfPresent(key, remappingFunction);
    }

    @Override
    public List<AuthEntity> compute(String key, BiFunction<? super String, ? super List<AuthEntity>, ? extends List<AuthEntity>> remappingFunction) {
        validProperty(key);
        return super.compute(key, remappingFunction);
    }


}
