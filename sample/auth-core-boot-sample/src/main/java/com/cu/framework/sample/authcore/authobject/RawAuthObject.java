package com.cu.framework.sample.authcore.authobject;

import com.cu.framework.auth.core.entity.FlatteningAuthority;
import com.cu.framework.auth.core.entity.IAuthObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 原样承载权限串的集合体，不参与标记解析。
 * <p>
 * {@link DemoAuthObject} 走的是"串必须是 标记__ID 形式"这条路；
 * 权限维护接口需要把调用方给的权限串与描述<b>原样</b>写进权限表（包括含引号的载荷），
 * 所以另需一个不做解析的载体。两者并存正说明 auth-core 只认 {@link IAuthObject} 这个接口
 *
 * @author kewen
 * @since 2026-10-20
 */
public class RawAuthObject implements IAuthObject {

    private static final long serialVersionUID = 1L;

    /** 原样保存的权限串 */
    private final List<FlatteningAuthority> auths = new ArrayList<>();

    /**
     * 用一组权限串构造集合体
     *
     * @param flatteningAuthorities 权限串，null 视为空集合
     */
    public RawAuthObject(Collection<FlatteningAuthority> flatteningAuthorities) {
        if (flatteningAuthorities != null) {
            auths.addAll(flatteningAuthorities);
        }
    }

    @Override
    public Collection<FlatteningAuthority> listBaseAuth() {
        return auths;
    }

    @Override
    public void setProperties(Collection<FlatteningAuthority> flatteningAuthorities) {
        auths.clear();
        if (flatteningAuthorities != null) {
            auths.addAll(flatteningAuthorities);
        }
    }
}
