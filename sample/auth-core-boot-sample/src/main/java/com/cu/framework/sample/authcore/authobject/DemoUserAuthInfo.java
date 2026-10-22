package com.cu.framework.sample.authcore.authobject;

import com.cu.framework.auth.core.entity.FlatteningAuthority;
import com.cu.framework.auth.core.entity.IAuthObject;
import com.cu.framework.auth.core.exception.AuthEntityException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户侧权限集合体：<b>只出不进</b>。
 * <p>
 * 它刻意不继承 {@link com.cu.framework.auth.core.entity.AbstractAuthObject}——那套设施提供的
 * 是"按标记找回实体类、再逐位置回填"的反解能力，而用户权限体没有反解场景：
 * {@code DEPTINHERIT__100} 这条串里不含"它是哪个部门的下级"，从串本身推不出原始结构。
 * 与其静默给出一个空权限体（判定结果会伪装成数据问题），不如在入口处当场失败。
 * <p>
 * 与 {@link DemoAuthObject} 并存正是这组用例要演示的不对称：配置侧可无损往返，用户侧不可反解。
 *
 * @author kewen
 * @since 2026-10-22
 */
public class DemoUserAuthInfo implements IAuthObject {

    private static final long serialVersionUID = 1L;

    /** 用户维度的权限 */
    private final List<DemoUser> users = new ArrayList<>();
    /** 继承型权限，串形如 {@code DEPTINHERIT__100} */
    private final List<DemoInheritDept> inheritDepts = new ArrayList<>();

    /**
     * 由结构化数据装配用户权限体——这是唯一的进入路径，
     * 因为"谁属于哪个部门的下级"只能由上层的数据算出来，不能从串里读出来
     *
     * @param users        用户维度权限，可为 null
     * @param inheritDepts 继承型权限，可为 null
     */
    public DemoUserAuthInfo(List<DemoUser> users, List<DemoInheritDept> inheritDepts) {
        if (users != null) {
            this.users.addAll(users);
        }
        if (inheritDepts != null) {
            this.inheritDepts.addAll(inheritDepts);
        }
    }

    @Override
    public Collection<FlatteningAuthority> listBaseAuth() {
        Set<FlatteningAuthority> authorities = new LinkedHashSet<>();
        for (DemoUser user : users) {
            authorities.add(user.getAuth());
        }
        for (DemoInheritDept inheritDept : inheritDepts) {
            authorities.add(inheritDept.getAuth());
        }
        return authorities;
    }

    /**
     * 用户权限体不接受从权限串反解
     *
     * @param flatteningAuthorities 任意入参，一律拒绝
     * @throws AuthEntityException 总是抛出，异常信息说明为什么反解不出来
     */
    @Override
    public void setProperties(Collection<FlatteningAuthority> flatteningAuthorities) {
        throw new AuthEntityException("用户权限体不做反解析："
                + DemoInheritDept.FLAG + "__<部门ID> 里没有\"这是哪个部门的下级\"这条信息，"
                + "反解只能靠猜；装配用户权限体请走构造入参");
    }
}
