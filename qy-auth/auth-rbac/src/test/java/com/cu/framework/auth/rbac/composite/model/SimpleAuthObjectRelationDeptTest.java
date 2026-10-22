package com.cu.framework.auth.rbac.composite.model;

import com.cu.framework.auth.core.entity.AuthConstant;
import com.cu.framework.auth.core.entity.FlatteningAuthority;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

/**
 * 配置权限体上部门继承标记的编解码往返与标记形态。
 * <p>
 * 断言只落在权限串与 id 上，不判定解析出来的元素具体是哪个类——
 * {@code DeptWithParents} 与 {@code Dept} 共用同一个标记，谁被注册上取决于类路径扫描顺序
 *
 * @author kewen
 * @since 2026-10-22
 */
public class SimpleAuthObjectRelationDeptTest {

    private static FlatteningAuthority single(Collection<FlatteningAuthority> authorities) {
        Assert.assertEquals("应当只产出一条权限", 1, authorities.size());
        return authorities.iterator().next();
    }

    @Test
    public void relationDeptSurvivesRoundTrip() {
        SimpleAuthObject granted = new SimpleAuthObject();
        granted.addRelationDepts(new ParentDept(2L, "中心", 1));

        FlatteningAuthority stored = single(granted.listBaseAuth());
        Assert.assertEquals("RELATIONDEPT__2", stored.getAuth());
        Assert.assertEquals("RELATIONDEPT__中心", stored.getDescription());

        SimpleAuthObject parsed = new SimpleAuthObject();
        parsed.setProperties(granted.listBaseAuth());
        Assert.assertEquals("解析回来的元素个数", 1, parsed.getRelationDepts().size());
        Assert.assertTrue("配置侧的部门集合不得混进继承部门", parsed.getDepts().isEmpty());
        Assert.assertEquals(Long.valueOf(2L), parsed.getRelationDepts().get(0).getId());
        Assert.assertEquals("中心", parsed.getRelationDepts().get(0).getName());

        FlatteningAuthority reSerialised = single(parsed.listBaseAuth());
        Assert.assertEquals(stored.getAuth(), reSerialised.getAuth());
        Assert.assertEquals("描述也要无损往返，菜单权限表里存的就是它",
                stored.getDescription(), reSerialised.getDescription());
    }

    @Test
    public void levelDoesNotRideOnTheAuthorityString() {
        SimpleAuthObject granted = new SimpleAuthObject();
        granted.addRelationDepts(new ParentDept(2L, "中心", 3));
        Assert.assertEquals(3, granted.getRelationDepts().get(0).getLevel());

        SimpleAuthObject parsed = new SimpleAuthObject();
        parsed.setProperties(granted.listBaseAuth());
        Assert.assertEquals("层级深度不随权限串存储，反解回来是默认值",
                0, parsed.getRelationDepts().get(0).getLevel());
        Assert.assertEquals("但判定只看串，所以两者等价",
                single(granted.listBaseAuth()).getAuth(),
                single(parsed.listBaseAuth()).getAuth());
    }

    @Test
    public void flagIsASingleTokenSoPositionsMatch() {
        ParentDept parentDept = new ParentDept();
        Assert.assertFalse("标记含次级分隔符会被当成多个位置参与拼接",
                parentDept.flag().contains(AuthConstant.AUTH_SUB_SPLIT));

        FlatteningAuthority authority = new ParentDept(2L, "中心", 1).getAuth();
        String[] authParts = authority.getAuth().split(AuthConstant.AUTH_SPLIT);
        String[] descParts = authority.getDescription().split(AuthConstant.AUTH_SPLIT);
        Assert.assertEquals("标记一段、值一段", 2, authParts.length);
        Assert.assertEquals("ID 段只能有一个值", 1, authParts[1].split(AuthConstant.AUTH_SUB_SPLIT).length);
        Assert.assertEquals("描述段也只能有一个值", 1, descParts[1].split(AuthConstant.AUTH_SUB_SPLIT).length);
    }

    @Test
    public void sameDeptUnderBothFlagsStaysTwoDistinctAuthorities() {
        SimpleAuthObject granted = new SimpleAuthObject();
        granted.addDepts(new Dept(2L, "中心"));
        granted.addRelationDepts(new ParentDept(2L, "中心", 1));

        Collection<FlatteningAuthority> authorities = granted.listBaseAuth();
        Assert.assertEquals("本部门与子孙部门是两条正交的权限", 2, authorities.size());

        SimpleAuthObject parsed = new SimpleAuthObject();
        parsed.setProperties(authorities);
        Assert.assertEquals(1, parsed.getDepts().size());
        Assert.assertEquals(1, parsed.getRelationDepts().size());
        Assert.assertEquals(Long.valueOf(2L), parsed.getDepts().get(0).getId());
        Assert.assertEquals(Long.valueOf(2L), parsed.getRelationDepts().get(0).getId());
    }

    @Test
    public void relationDeptCoexistsWithOtherAuthorities() {
        SimpleAuthObject granted = new SimpleAuthObject();
        granted.addUsers(new User(7L, "使用者"));
        granted.addRoles(new Role(9L, "角色"));
        granted.addRelationDepts(new ParentDept(2L, "中心", 1), new ParentDept(1L, "根", 2));
        Assert.assertEquals("互不覆盖", 4, granted.listBaseAuth().size());

        SimpleAuthObject parsed = new SimpleAuthObject();
        parsed.setProperties(granted.listBaseAuth());
        Assert.assertEquals(1, parsed.getUsers().size());
        Assert.assertEquals(1, parsed.getRoles().size());
        Assert.assertEquals(2, parsed.getRelationDepts().size());
        Assert.assertEquals(granted.listBaseAuth(), parsed.listBaseAuth());
    }
}
