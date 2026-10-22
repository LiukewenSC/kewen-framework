package com.cu.framework.sample.authcore.authobject;

import com.cu.framework.auth.core.entity.AbstractAuthObject;
import com.cu.framework.auth.core.entity.FlatteningAuthority;
import com.cu.framework.auth.core.entity.IFlagAuthEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * 示例的权限集合体，<b>以对象形态存放</b>：每个标记一个类型化列表，回给前端的是结构而不是拼好的字符串。
 * <p>
 * 继承 {@link AbstractAuthObject} 就自动获得"权限串按 {@code __} 拆标记、按标记找回实体类、
 * 再按 {@code _} 逐位置回填"的解析规则；本类只做两件事——把解析出的实体<b>按类型归位</b>，
 * 以及从这些对象<em>重新序列化</em>出权限串。
 * <p>
 * 之所以要重新序列化而不是原样退回入参：判定用的串必须是从对象结构拼出来的，
 * 往返不一致就说明编解码规则没走通，问题会暴露在这里而不是藏在判定结果里。
 * <p>
 * 列表的个数与实体命名按示例实际需要来：本示例有 {@code USER}、{@code ROLE}、
 * {@code DEPT_ROLE}、{@code DEPTINHERIT} 四种标记，因此只有四个列表——没有的标记不预留空列表
 *
 * @author kewen
 * @since 2026-10-20
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class DemoAuthObject extends AbstractAuthObject {

    private static final long serialVersionUID = 1L;

    /** 用户维度权限，标记 {@code USER} */
    private List<DemoUser> demoUsers = new ArrayList<>();
    /** 角色维度权限，标记 {@code ROLE} */
    private List<DemoRole> demoRoles = new ArrayList<>();
    /** 部门+角色的组合权限，标记 {@code DEPT_ROLE} */
    private List<DeptRole> deptRoles = new ArrayList<>();
    /** 继承型权限，标记 {@code DEPTINHERIT}，语义由上层赋予，核心层只管编解码 */
    private List<DemoInheritDept> inheritDepts = new ArrayList<>();

    /**
     * 从归位后的对象重新拼出权限串，用 Set 去重
     */
    @Override
    public Collection<FlatteningAuthority> listBaseAuth() {
        Collection<FlatteningAuthority> flatteningAuthorities = new HashSet<>();
        for (DemoUser demoUser : demoUsers) {
            flatteningAuthorities.add(demoUser.getAuth());
        }
        for (DemoRole demoRole : demoRoles) {
            flatteningAuthorities.add(demoRole.getAuth());
        }
        for (DeptRole deptRole : deptRoles) {
            flatteningAuthorities.add(deptRole.getAuth());
        }
        for (DemoInheritDept inheritDept : inheritDepts) {
            flatteningAuthorities.add(inheritDept.getAuth());
        }
        return flatteningAuthorities;
    }

    /**
     * {@link AbstractAuthObject} 解析完成后的回写钩子：按实体类型归位到各自的列表
     */
    @Override
    protected void setBaseAuth(List<IFlagAuthEntity> abstractAuthEntities) {
        demoUsers.clear();
        demoRoles.clear();
        deptRoles.clear();
        inheritDepts.clear();
        for (IFlagAuthEntity entity : abstractAuthEntities) {
            if (entity instanceof DemoUser) {
                demoUsers.add((DemoUser) entity);
            } else if (entity instanceof DemoRole) {
                demoRoles.add((DemoRole) entity);
            } else if (entity instanceof DeptRole) {
                deptRoles.add((DeptRole) entity);
            } else if (entity instanceof DemoInheritDept) {
                inheritDepts.add((DemoInheritDept) entity);
            }
        }
    }

    public void addUsers(DemoUser... items) {
        demoUsers.addAll(Arrays.asList(items));
    }

    public void addRoles(DemoRole... items) {
        demoRoles.addAll(Arrays.asList(items));
    }

    public void addDeptRoles(DeptRole... items) {
        deptRoles.addAll(Arrays.asList(items));
    }

    public void addInheritDepts(DemoInheritDept... items) {
        inheritDepts.addAll(Arrays.asList(items));
    }
}
