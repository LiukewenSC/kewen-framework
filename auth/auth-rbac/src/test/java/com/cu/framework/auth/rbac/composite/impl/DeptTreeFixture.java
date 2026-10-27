package com.cu.framework.auth.rbac.composite.impl;

import com.cu.framework.auth.rbac.composite.SysUserComposite;
import com.cu.framework.auth.rbac.composite.model.ParentDept;
import com.cu.framework.auth.rbac.mp.entity.SysDept;
import com.cu.framework.auth.rbac.model.UserAuthObject;
import com.cu.framework.auth.rbac.mp.entity.SysUserCredential;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 测试用的部门树与用户权限组合服务桩。
 * <p>
 * 上级链<b>走生产实现</b>而不是各测试自己写死一条链：这样"展开结果"的断言能反映链路口径，
 * 把生产实现改回含自身时，依赖它的断言会当场失败而不是空转
 *
 * @author kewen
 * @since 2026-10-22
 */
public final class DeptTreeFixture {

    /** 根部门 */
    public static final long ROOT = 1L;
    /** 根的下一级，演示中被当作"授权挂在它身上"的那个部门 */
    public static final long CENTER = 2L;
    /** 中心的下一级 */
    public static final long SECTION = 3L;
    /** 最底层，隔了两级 */
    public static final long GROUP = 4L;

    private DeptTreeFixture() {
    }

    public static SysDept dept(Long id, String name, Long parentId) {
        return new SysDept().setId(id).setName(name).setParentId(parentId);
    }

    /** 根(1) -&gt; 中心(2) -&gt; 处(3) -&gt; 组(4) */
    public static Map<Long, SysDept> fourLevelDepts() {
        Map<Long, SysDept> tree = new HashMap<>();
        tree.put(ROOT, dept(ROOT, "根", 0L));
        tree.put(CENTER, dept(CENTER, "中心", ROOT));
        tree.put(SECTION, dept(SECTION, "处", CENTER));
        tree.put(GROUP, dept(GROUP, "组", SECTION));
        return tree;
    }

    public static Function<Long, SysDept> fourLevelTree() {
        return fourLevelDepts()::get;
    }

    /**
     * 以上述四层树为数据源的权限组合服务桩，只实现上级链，其余方法不参与权限展开
     */
    public static SysUserComposite compositeForFourLevelTree() {
        Map<Long, SysDept> tree = fourLevelDepts();
        return new SysUserComposite() {
            @Override
            public List<ParentDept> listAncestorDepts(Long deptId) {
                return SysUserCompositeImpl.listAncestorDepts(deptId, tree::get);
            }

            @Override
            public SysUserCredential getUserCredentialByUsername(String username) {
                return null;
            }

            @Override
            public UserAuthObject getUserAuthObjectByUsername(String username) {
                return null;
            }

            @Override
            public boolean updatePassword(String username, String encodedNewPassword) {
                return false;
            }
        };
    }
}
