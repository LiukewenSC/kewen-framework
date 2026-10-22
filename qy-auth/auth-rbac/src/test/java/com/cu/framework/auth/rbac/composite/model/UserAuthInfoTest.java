package com.cu.framework.auth.rbac.composite.model;

import com.cu.framework.auth.core.entity.FlatteningAuthority;
import com.cu.framework.auth.rbac.composite.impl.DeptTreeFixture;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户权限体展开出来的权限串：部门自身串与上级继承串的边界、层级覆盖范围、装载期填充的可重复性。
 * <p>
 * 这些断言只挂权限串，不挂 {@code DEPT_ROLE} 组合串——角色×部门的组合形态尚未定案，
 * 因此下面的用例一律不给权限体配角色
 *
 * @author kewen
 * @since 2026-10-22
 */
public class UserAuthInfoTest {

    private static final String USER_AUTH = "USER__7";
    private static final String RELATION_PREFIX = "RELATIONDEPT__";

    /** 组装一个隶属于指定部门的用户权限体，上级链走生产实现 */
    private static UserAuthInfo infoOfDept(Long... deptIds) {
        UserAuthInfo info = new UserAuthInfo();
        info.setUser(new User(7L, "使用者"));
        List<DeptWithParents> depts = new ArrayList<>(deptIds.length);
        for (Long deptId : deptIds) {
            depts.add(new DeptWithParents(deptId, "部门" + deptId));
        }
        info.setDepts(depts);
        info.fillUserAuth(DeptTreeFixture.compositeForFourLevelTree());
        return info;
    }

    private static Set<String> authStrings(UserAuthInfo info) {
        Set<String> strings = new HashSet<>();
        for (FlatteningAuthority authority : info.listBaseAuth()) {
            strings.add(authority.getAuth());
        }
        return strings;
    }

    @Test
    public void noAuthoritiesWhenUserMissing() {
        UserAuthInfo info = new UserAuthInfo();
        info.setDepts(Collections.singletonList(
                new DeptWithParents(DeptTreeFixture.GROUP, "组")));
        info.setRoles(Collections.singletonList(new Role(9L, "角色")));
        Assert.assertTrue("用户本身缺失时不得产出任何权限，也不得抛异常", info.listBaseAuth().isEmpty());
    }

    @Test
    public void descendantCarriesEveryAncestorRelationDept() {
        // 组(4) 的上级链是 根(1) -> 中心(2) -> 处(3)
        Set<String> auths = authStrings(infoOfDept(DeptTreeFixture.GROUP));
        Assert.assertEquals(new HashSet<>(Arrays.asList(
                        USER_AUTH,
                        "DEPT__" + DeptTreeFixture.GROUP,
                        RELATION_PREFIX + DeptTreeFixture.ROOT,
                        RELATION_PREFIX + DeptTreeFixture.CENTER,
                        RELATION_PREFIX + DeptTreeFixture.SECTION)),
                auths);
    }

    @Test
    public void ownDeptMembersAreNotCoveredByItsOwnRelationDept() {
        Set<String> auths = authStrings(infoOfDept(DeptTreeFixture.SECTION));
        Assert.assertFalse("处自己的人不得被处的继承串命中，这条是含自身与不含自身的分界",
                auths.contains(RELATION_PREFIX + DeptTreeFixture.SECTION));
        Assert.assertTrue("但中心的继承串要命中，因为处是中心的子部门",
                auths.contains(RELATION_PREFIX + DeptTreeFixture.CENTER));
    }

    @Test
    public void skipLevelDescendantIsAlsoCovered() {
        // 中心(2) 的上级只有根(1)：隔级(根 -> 中心 -> 处)与直接上级都命中，但中心自己不命中
        Set<String> centerMember = authStrings(infoOfDept(DeptTreeFixture.CENTER));
        Assert.assertEquals(new HashSet<>(Arrays.asList(
                        USER_AUTH,
                        "DEPT__" + DeptTreeFixture.CENTER,
                        RELATION_PREFIX + DeptTreeFixture.ROOT)),
                centerMember);

        Set<String> sectionMember = authStrings(infoOfDept(DeptTreeFixture.SECTION));
        Assert.assertTrue("根授予的继承权限，隔了一级的处也拿得到",
                sectionMember.contains(RELATION_PREFIX + DeptTreeFixture.ROOT));
    }

    @Test
    public void rootMemberHasNothingToInherit() {
        Set<String> auths = authStrings(infoOfDept(DeptTreeFixture.ROOT));
        Assert.assertEquals("根部门只有自己那一条，没有任何继承串",
                new HashSet<>(Arrays.asList(USER_AUTH, "DEPT__" + DeptTreeFixture.ROOT)), auths);
    }

    @Test
    public void multiDeptMembershipTakesTheUnion() {
        // 同时在中心(2) 和处(3) 任职：处是中心的子部门，所以中心的继承串经由"处"这一重身份成立
        Set<String> both = authStrings(infoOfDept(DeptTreeFixture.CENTER, DeptTreeFixture.SECTION));
        Assert.assertTrue("多重任职取并集，子部门身份带来父部门的继承串",
                both.contains(RELATION_PREFIX + DeptTreeFixture.CENTER));
        Assert.assertTrue(both.contains("DEPT__" + DeptTreeFixture.CENTER));
        Assert.assertTrue(both.contains("DEPT__" + DeptTreeFixture.SECTION));
        Assert.assertFalse("但仅凭中心这个身份本身不产生中心的继承串",
                authStrings(infoOfDept(DeptTreeFixture.CENTER)).contains(RELATION_PREFIX + DeptTreeFixture.CENTER));
    }

    @Test
    public void deptAndRelationDeptAuthoritiesDoNotCollapse() {
        // 同一条权限串集合里 DEPT__x 与 RELATIONDEPT__x 是两个不同的判定目标，不能因为 id 相同而合并
        UserAuthInfo info = infoOfDept(DeptTreeFixture.CENTER, DeptTreeFixture.SECTION);
        Set<String> auths = authStrings(info);
        Assert.assertTrue(auths.contains("DEPT__" + DeptTreeFixture.CENTER));
        Assert.assertTrue(auths.contains(RELATION_PREFIX + DeptTreeFixture.CENTER));
        Assert.assertEquals("USER 一条、两个部门各一条、两条继承串（根与中心，根那条由两个身份共同贡献且去重）",
                5, auths.size());
    }

    @Test
    public void fillUserAuthIsRepeatable() {
        UserAuthInfo info = infoOfDept(DeptTreeFixture.GROUP);
        info.setRoles(Arrays.asList(new Role(9L, "角色9"), new Role(10L, "角色10")));
        info.fillUserAuth(DeptTreeFixture.compositeForFourLevelTree());
        int parents = info.getDepts().get(0).getParents().size();
        int deptRoles = info.getDeptRoles().size();
        info.fillUserAuth(DeptTreeFixture.compositeForFourLevelTree());
        Assert.assertEquals("重复装载不累积上级链", parents, info.getDepts().get(0).getParents().size());
        Assert.assertEquals("重复装载不累积部门角色", deptRoles, info.getDeptRoles().size());
    }
}
