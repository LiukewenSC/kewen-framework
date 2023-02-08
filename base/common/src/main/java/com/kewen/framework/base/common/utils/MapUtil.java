package com.kewen.framework.base.common.utils;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @descrpition map工具
 * @author kewen
 * @since 2022-11-30 16:20
 */
public class MapUtil {

    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    /**
     * 获取Map中的值
     * @param map 需要取出值的map
     * @param name 取药取出的key 例如 spring.datasource  返回为 Map或Bean结构
     *             例如 spring.datasource.username 返回为 String结构
     * @param clazz
     * @param <T>
     *     {
     *      "spring":
     *          {
     *              "datasource":
     *                  {
     *                      "url":"jdbc:mysql:localhost:3306"
     *                      "username":"mysql"
     *                      "password":"123456"
     *                  }
     *          }
     *      }
     * @return
     */
    public static <T> T getValue(Map map,String name,Class<T> clazz){
        EvaluationContext ctx = mapEvaluationContext(map);
        String spel = convertSpel(name);
        Object value = PARSER.parseExpression("#map" + spel).getValue(ctx);
        if (value instanceof String){
            if (clazz != String.class){
                throw new ClassCastException();
            }
            return (T)value;
        }
        return BeanUtil.convert(value, clazz);
    }

    /**
     * map中设置值 ，必须为map嵌套map的情况
     * @param map
     * @param name
     * @param value
     */
    public static void setValue(Map map,String name,Object value){
        EvaluationContext context = mapEvaluationContext(map);
        String spel = convertSpel(name);
        PARSER.parseExpression("#map" + spel).setValue(context, value);
    }
    private static EvaluationContext mapEvaluationContext(Map map){
        EvaluationContext ctx = new StandardEvaluationContext();
        ctx.setVariable("map",map);
        return ctx;
    }
    private static String convertSpel(String name){
        String[] split = name.split("\\.");
        StringBuilder builder = new StringBuilder("");
        for (String s : split) {
            builder.append("['").append(s).append("']");
        }
        return builder.toString();
    }


    /**
     * 快速构建Map
     * @param keyClass
     * @param valueCLass
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Builder<K, V> builder(Class<K> keyClass,Class<V> valueCLass){
        return new Builder<K, V>(keyClass,valueCLass);
    }
    public static <K, V> Builder<K, V> builder(){
        return new Builder<K, V>();
    }

    public static class Builder<K,V> {
        private final Map<K, V> map;
        private Builder(Class<K> keyClass,Class<V> valueCLass) {
            map = new HashMap<K,V>();
        }
        private Builder() {
            map = new HashMap<K,V>();
        }
        public Builder<K,V> put(K k,V v){
            map.put(k, v);
            return this;
        }
        public Map<K,V> build(){
            return map;
        }
    }



}
