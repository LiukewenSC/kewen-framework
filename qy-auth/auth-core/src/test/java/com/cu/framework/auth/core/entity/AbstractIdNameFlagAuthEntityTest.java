package com.cu.framework.auth.core.entity;

import org.junit.Test;
import org.springframework.core.GenericTypeResolver;

import static org.junit.Assert.*;

public class AbstractIdNameFlagAuthEntityTest {



    @Test
    public void genericTypeResolver() {
        Class<?> longType = GenericTypeResolver.resolveTypeArgument(LongEntity.class, AbstractIdNameFlagAuthEntity.class);
        Class<?> intType = GenericTypeResolver.resolveTypeArgument(IntegerEntity.class, AbstractIdNameFlagAuthEntity.class);
        Class<?> strType = GenericTypeResolver.resolveTypeArgument(StringEntity.class, AbstractIdNameFlagAuthEntity.class);

        assertEquals(Long.class, longType);
        assertEquals(Integer.class, intType);
        assertEquals(String.class, strType);

        Class<?> nullType = GenericTypeResolver.resolveTypeArgument(this.getClass(), AbstractIdNameFlagAuthEntity.class);
        assertNull(nullType);
    }

    @Test
    public void deSerializeLongId() {
        LongEntity entity = new LongEntity();
        entity.deSerialize(0, "12345", "测试名称");
        assertEquals(Long.valueOf(12345L), entity.getId());
        assertEquals("测试名称", entity.getName());
    }

    @Test
    public void deSerializeIntegerId() {
        IntegerEntity entity = new IntegerEntity();
        entity.deSerialize(0, "999", "整型测试");
        assertEquals(Integer.valueOf(999), entity.getId());
        assertEquals("整型测试", entity.getName());
    }

    @Test
    public void deSerializeStringId() {
        StringEntity entity = new StringEntity();
        entity.deSerialize(0, "abc-id", "字符串测试");
        assertEquals("abc-id", entity.getId());
        assertEquals("字符串测试", entity.getName());
    }
}

class LongEntity extends AbstractIdNameFlagAuthEntity<Long> {
    @Override
    public String flag() {
        return "LONG_TEST";
    }
}

class IntegerEntity extends AbstractIdNameFlagAuthEntity<Integer> {
    @Override
    public String flag() {
        return "INT_TEST";
    }
}

class StringEntity extends AbstractIdNameFlagAuthEntity<String> {
    @Override
    public String flag() {
        return "STR_TEST";
    }
}