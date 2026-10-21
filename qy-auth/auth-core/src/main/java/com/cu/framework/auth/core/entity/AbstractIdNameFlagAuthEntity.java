package com.cu.framework.auth.core.entity;

import com.cu.framework.auth.core.exception.AuthEntityException;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.GenericTypeResolver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 抽象权限对象，提供基础的数据，用于转换成BaseAuth权限对象
 * 自定义的权限类型最好继承此类，不能继承的需要约定好权限字符串格式
 * 其实现类需要保留无参构造，否则项目启动会抛异常
 *
 * 此处只支持 ID+Name的形式，若有其他规则如 某部门某岗位 DEPTPOSSION_${deptId}_${positionId}，请自行扩展 IAuthEntityProvider
 * @author kewen
 * @since 2023-12-26
 */
@Data
public abstract class AbstractIdNameFlagAuthEntity<ID> implements IFlagAuthEntity {

    private static final long serialVersionUID = 5347275476740629152L;

    /**
     * ID 类型转换器注册表，支持扩展新类型只需 put 即可
     */
    private static final Map<Class<?>, Function<String, ?>> ID_CONVERTERS;

    static {
        Map<Class<?>, Function<String, ?>> map = new HashMap<>();
        map.put(Integer.class, Integer::valueOf);
        map.put(Long.class, Long::valueOf);
        map.put(String.class, Function.identity());
        ID_CONVERTERS = Collections.unmodifiableMap(map);
    }

    /**
     * 缓存每个具体子类解析出的 ID 转换器，避免重复反射
     */
    private static final ConcurrentHashMap<Class<?>, Function<String, ?>> CONVERTER_MAP = new ConcurrentHashMap<>();

    /**
     * ID
     */
    protected ID id;

    /**
     * 名字
     */
    protected String name;

    @Override
    public Pair<String, String> serialize(int pos) {
        return Pair.of(id.toString(), name);
    }

    @Override
    public void deSerialize(int pos, String idStr, String descStr) {
        setId(getIdConverter().andThen(id -> (ID) id).apply(idStr));
        setName(descStr);
    }

    private Function<String, ?> getIdConverter() {
        return CONVERTER_MAP.computeIfAbsent(this.getClass(), clz -> {
            Class<?> actualT = GenericTypeResolver.resolveTypeArgument(clz, AbstractIdNameFlagAuthEntity.class);
            Function<String, ?> converter = ID_CONVERTERS.get(actualT);
            if (converter == null) {
                throw new AuthEntityException("ID的格式仅支持: " + ID_CONVERTERS.keySet());
            }
            return converter;
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractIdNameFlagAuthEntity)) return false;
        AbstractIdNameFlagAuthEntity<?> that = (AbstractIdNameFlagAuthEntity<?>) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
