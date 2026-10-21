package com.cu.framework.sample.authcore.context;

import com.cu.framework.auth.core.entity.FlatteningAuthority;
import com.cu.framework.auth.core.menu.AuthMenuHandler;
import com.cu.framework.sample.authcore.mapper.DemoMenuAuthMapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * auth-core 的第二个强制扩展点：某个请求路径需要哪种权限，由示例自己定规则。
 * <p>
 * 这里的规则刻意做得直白——查 demo_menu_auth 里该路径被授予了哪些权限串，与当前用户的权限集合求交集；
 * <b>路径没有登记就视为没有授予任何人</b>，绝不因为查不到记录而放行。
 * 换成另一套规则（比如按路径前缀、或者改读别的表）不需要动框架一行代码，
 * 这正是本示例要证明的事：核心层不认识任何具体权限模型
 * <p>
 * 路径是应用内路径：拦截器已把 contextPath 剥掉，所以登记时不要带 /coredemo 前缀
 *
 * @author kewen
 * @since 2026-10-20
 */
public class DemoMenuAuthHandler implements AuthMenuHandler {

    /** 菜单授权的数据来源 */
    private final DemoMenuAuthMapper demoMenuAuthMapper;

    public DemoMenuAuthHandler(DemoMenuAuthMapper demoMenuAuthMapper) {
        this.demoMenuAuthMapper = demoMenuAuthMapper;
    }

    /**
     * 判断当前权限集合能否访问该路径
     *
     * @param auths 当前用户的权限集合，可为空
     * @param path  应用内请求路径
     * @return 有交集则放行；路径未登记或权限集合为空时拒绝
     */
    @Override
    public boolean hasMenuAccessAuth(Collection<FlatteningAuthority> auths, String path) {
        List<String> granted = demoMenuAuthMapper.listAuthsByPath(path);
        if (granted == null || granted.isEmpty()) {
            return false;
        }
        if (auths == null || auths.isEmpty()) {
            return false;
        }
        Set<String> mine = new HashSet<>();
        for (FlatteningAuthority auth : auths) {
            if (auth.getAuth() != null) {
                mine.add(auth.getAuth());
            }
        }
        for (String auth : granted) {
            if (mine.contains(auth)) {
                return true;
            }
        }
        return false;
    }
}
