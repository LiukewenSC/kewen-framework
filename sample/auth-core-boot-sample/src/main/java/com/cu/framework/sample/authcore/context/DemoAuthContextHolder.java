package com.cu.framework.sample.authcore.context;

import com.cu.framework.auth.core.AuthCurrentUserContextHolder;
import com.cu.framework.auth.core.entity.BaseAuth;
import com.cu.framework.auth.core.entity.CurrentUser;
import com.cu.framework.sample.authcore.authobject.DemoAuthObject;
import com.cu.framework.sample.authcore.mapper.DemoUserAuthMapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * auth-core 的第一个强制扩展点：当前请求的权限从哪来。
 * <p>
 * 框架不关心身份是怎么确定的（这里来自请求头），也不关心权限是怎么组织的，
 * 只要求交出 {@link BaseAuth} 集合。本实现每次都按用户名查一次 demo_user_auth，
 * 并故意<b>不做请求内缓存</b>：那条查询没有声明数据范围，正好充当
 * "同一调用链上的其他语句不受影响"的现场观察对象
 * <p>
 * 权限串先按 {@code 标记__ID} 解析成实体、再由实体重新序列化，
 * 因此标记未注册或格式不合规会直接抛异常，不会静默降级
 *
 * @author kewen
 * @since 2026-10-20
 */
public class DemoAuthContextHolder implements AuthCurrentUserContextHolder {

    /** 用户权限的数据来源 */
    private final DemoUserAuthMapper demoUserAuthMapper;

    public DemoAuthContextHolder(DemoUserAuthMapper demoUserAuthMapper) {
        this.demoUserAuthMapper = demoUserAuthMapper;
    }

    /**
     * 未提供身份时返回 null：框架只用权限集合，集合为空即拒绝，不依赖这里
     */
    @Override
    public CurrentUser getUser() {
        String username = DemoUserContext.username();
        return username == null ? null : new CurrentUser().setUsername(username);
    }

    /**
     * 当前请求的权限集合；没有身份或该用户没有任何授权行时为空集合
     */
    @Override
    public Collection<BaseAuth> getAuths() {
        return loadAuthObject().listBaseAuth();
    }

    /**
     * 装载并解析当前请求的权限集合体，供 /demo/current-user 回显对象形态
     *
     * @return 解析后的权限集合体，无身份或无授权时为空集合体
     */
    public DemoAuthObject loadAuthObject() {
        DemoAuthObject authObject = new DemoAuthObject();
        authObject.setProperties(loadRawFromStore());
        return authObject;
    }

    /**
     * 从存储里原样读出的权限串，<b>没有</b>经过任何编解码。
     * 与 {@link DemoAuthObject#listBaseAuth()} 对照即可验证"串→对象→串"的往返一致性
     *
     * @return 权限串集合，无身份时为空集合
     */
    public List<BaseAuth> loadRawFromStore() {
        String username = DemoUserContext.username();
        if (username == null) {
            return Collections.emptyList();
        }
        List<BaseAuth> rows = demoUserAuthMapper.listByUsername(username);
        return rows == null ? Collections.<BaseAuth>emptyList() : rows;
    }
}
