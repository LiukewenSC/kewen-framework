package com.cu.framework.sample.authcore.authobject;

import com.cu.framework.auth.core.entity.FlatteningAuthority;
import com.cu.framework.auth.core.exception.AuthEntityException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 继承型权限标记 {@code DEPTINHERIT} 的编解码形态，与"配置侧可反解 / 用户侧不可反解"这条不对称。
 * <p>
 * 这里不起 Spring 上下文：要断言的是核心层的编解码规则本身，与装配无关。
 * 端到端那一半由 {@code verify.sh} 的 F 组用例覆盖
 *
 * @author kewen
 * @since 2026-10-22
 */
public class DemoInheritAuthObjectTest {

    private static final long DEPT_ID = 100L;

    private static Set<String> toStrings(Collection<FlatteningAuthority> authorities) {
        Set<String> values = new HashSet<>();
        for (FlatteningAuthority authority : authorities) {
            values.add(authority.getAuth());
        }
        return values;
    }

    @Test
    public void flagIsASingleTokenAndAuthorityHasExactlyOnePosition() {
        Assertions.assertFalse(DemoInheritDept.FLAG.contains("_"),
                "标记含次级分隔符会被拆成多个位置，拼出的串就与实体对不上");

        FlatteningAuthority authority = new DemoInheritDept(DEPT_ID, "研发部").getAuth();
        Assertions.assertEquals("DEPTINHERIT__100", authority.getAuth());
        Assertions.assertEquals("DEPTINHERIT__研发部", authority.getDescription());
    }

    @Test
    public void configSideRoundTripIsLossless() {
        DemoAuthObject granted = new DemoAuthObject();
        granted.addInheritDepts(new DemoInheritDept(DEPT_ID, "研发部"));

        DemoAuthObject parsed = new DemoAuthObject();
        parsed.setProperties(granted.listBaseAuth());

        Assertions.assertEquals(1, parsed.getInheritDepts().size(), "串要归位到继承部门列表，不能落到别的列表");
        Assertions.assertEquals(Long.valueOf(DEPT_ID), parsed.getInheritDepts().get(0).getId());
        Assertions.assertEquals("研发部", parsed.getInheritDepts().get(0).getName());
        Assertions.assertEquals(toStrings(granted.listBaseAuth()), toStrings(parsed.listBaseAuth()),
                "对象→串→对象→串 必须回到同一条串");
    }

    @Test
    public void userSideBodySerialisesButRefusesToBeParsed() {
        DemoInheritDept target = new DemoInheritDept(DEPT_ID, "研发部");
        DemoUserAuthInfo userSide = new DemoUserAuthInfo(
                Collections.singletonList(new DemoUser("1", "爱丽丝")),
                Collections.singletonList(target));

        Assertions.assertEquals(new HashSet<>(Arrays.asList("USER__1", "DEPTINHERIT__100")),
                toStrings(userSide.listBaseAuth()));

        DemoAuthObject configSide = new DemoAuthObject();
        configSide.addUsers(new DemoUser("1", "爱丽丝"));
        configSide.addInheritDepts(target);
        Assertions.assertEquals(toStrings(configSide.listBaseAuth()), toStrings(userSide.listBaseAuth()),
                "同一份数据从两种权限体拼出的串完全一致，所以判定结果也一致——不对称只发生在反解方向");

        AuthEntityException error = Assertions.assertThrows(AuthEntityException.class,
                () -> userSide.setProperties(configSide.listBaseAuth()));
        Assertions.assertTrue(error.getMessage().contains(DemoInheritDept.FLAG),
                "失败原因要点名是哪个标记反解不出来，实际是: " + error.getMessage());
    }
}
