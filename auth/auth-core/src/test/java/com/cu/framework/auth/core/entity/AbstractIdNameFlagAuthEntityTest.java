package com.cu.framework.auth.core.entity;

import com.cu.framework.auth.core.exception.AuthEntityException;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.springframework.core.GenericTypeResolver;

import java.util.Objects;

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

    // ==================== deSerialize 正常场景 ====================

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

    @Test
    public void deSerializeNegativeLong() {
        LongEntity entity = new LongEntity();
        entity.deSerialize(0, "-100", "负数ID");
        assertEquals(Long.valueOf(-100L), entity.getId());
        assertEquals("负数ID", entity.getName());
    }

    @Test
    public void deSerializeZeroId() {
        IntegerEntity entity = new IntegerEntity();
        entity.deSerialize(0, "0", "零值");
        assertEquals(Integer.valueOf(0), entity.getId());
    }

    @Test
    public void deSerializeEmptyStringId() {
        StringEntity entity = new StringEntity();
        entity.deSerialize(0, "", "空字符串ID");
        assertEquals("", entity.getId());
        assertEquals("空字符串ID", entity.getName());
    }

    // ==================== deSerialize 异常场景 ====================

    @Test(expected = NumberFormatException.class)
    public void deSerializeLongWithNonNumericThrows() {
        LongEntity entity = new LongEntity();
        entity.deSerialize(0, "not-a-number", "异常测试");
    }

    @Test(expected = NumberFormatException.class)
    public void deSerializeIntegerWithOverflowThrows() {
        IntegerEntity entity = new IntegerEntity();
        entity.deSerialize(0, "99999999999", "溢出测试");
    }

    @Test(expected = AuthEntityException.class)
    public void deSerializeUnsupportedTypeThrows() {
        DoubleEntity entity = new DoubleEntity();
        entity.deSerialize(0, "3.14", "不支持类型");
    }

    // ==================== serialize ====================

    @Test
    public void serializeLongEntity() {
        LongEntity entity = new LongEntity();
        entity.deSerialize(0, "42", "答案");
        Pair<String, String> result = entity.serialize(0);
        assertEquals("42", result.getLeft());
        assertEquals("答案", result.getRight());
    }

    @Test
    public void serializeStringEntity() {
        StringEntity entity = new StringEntity();
        entity.deSerialize(0, "uuid-abc-123", "标识符");
        Pair<String, String> result = entity.serialize(0);
        assertEquals("uuid-abc-123", result.getLeft());
        assertEquals("标识符", result.getRight());
    }

    @Test
    public void serializeRoundTrip() {
        LongEntity entity = new LongEntity();
        entity.deSerialize(0, "777", "往返测试");
        Pair<String, String> pair = entity.serialize(0);

        LongEntity restored = new LongEntity();
        restored.deSerialize(0, pair.getLeft(), pair.getRight());
        assertEquals(entity.getId(), restored.getId());
        assertEquals(entity.getName(), restored.getName());
    }

    // ==================== 转换器缓存验证 ====================

    @Test
    public void converterCachingProducesSameResult() {
        LongEntity e1 = new LongEntity();
        LongEntity e2 = new LongEntity();
        e1.deSerialize(0, "100", "first");
        e2.deSerialize(0, "200", "second");
        assertEquals(Long.valueOf(100L), e1.getId());
        assertEquals(Long.valueOf(200L), e2.getId());
    }

    @Test
    public void multipleDeSerializeCallsUpdateId() {
        LongEntity entity = new LongEntity();
        entity.deSerialize(0, "1", "第一次");
        assertEquals(Long.valueOf(1L), entity.getId());
        entity.deSerialize(0, "2", "第二次");
        assertEquals(Long.valueOf(2L), entity.getId());
        assertEquals("第二次", entity.getName());
    }

    // ==================== equals & hashCode ====================

    @Test
    public void equalsWithSameId() {
        LongEntity e1 = new LongEntity();
        e1.deSerialize(0, "1", "名称A");
        LongEntity e2 = new LongEntity();
        e2.deSerialize(0, "1", "名称B");
        assertEquals(e1, e2);
    }

    @Test
    public void equalsWithDifferentId() {
        LongEntity e1 = new LongEntity();
        e1.deSerialize(0, "1", "相同名称");
        LongEntity e2 = new LongEntity();
        e2.deSerialize(0, "2", "相同名称");
        assertNotEquals(e1, e2);
    }

    @Test
    public void equalsWithNull() {
        LongEntity e1 = new LongEntity();
        e1.deSerialize(0, "1", "test");
        assertNotEquals(e1, null);
    }

    @Test
    public void equalsWithDifferentType() {
        LongEntity longEntity = new LongEntity();
        longEntity.deSerialize(0, "1", "test");
        IntegerEntity intEntity = new IntegerEntity();
        intEntity.deSerialize(0, "1", "test");
        assertNotEquals(longEntity, intEntity);
    }

    @Test
    public void hashCodeConsistency() {
        LongEntity e1 = new LongEntity();
        e1.deSerialize(0, "999", "hash测试");
        LongEntity e2 = new LongEntity();
        e2.deSerialize(0, "999", "hash测试");
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    public void hashCodeWithNullId() {
        LongEntity entity = new LongEntity();
        assertEquals(Objects.hash((Object) null), entity.hashCode());
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

class DoubleEntity extends AbstractIdNameFlagAuthEntity<Double> {
    @Override
    public String flag() {
        return "DOUBLE_TEST";
    }
}
