package com.cu.framework.sample.login;

import com.cu.framework.basic.model.Result;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 示例用当前登录用户接口，供登录页在每次加载时判定会话是否已认证。
 * <p>
 * 刻意不加 {@code @SecurityIgnore}：未登录时它落到 {@code anyRequest().authenticated()}，
 * 返回需要登录的 JSON，前端据此渲染登录表单，这正是页面需要的信号。
 * 本 sample 没有引入 auth-rbac 那个 starter，框架的 {@code cu.security.current-user-url}
 * 在这里没有消费方，所以页面判定登录态要靠这个接口。
 *
 * @author kewen
 * @since 2026-10-16
 */
@RestController
public class SampleCurrentUserController {

    /**
     * 返回当前会话的认证信息
     *
     * @param authentication 安全上下文中的认证结果，已通过认证才会走到这里
     * @return 登录名、展示名与权限列表
     */
    @GetMapping("/api/me")
    public Result<Map<String, Object>> me(Authentication authentication) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("username", authentication.getName());
        data.put("authenticated", authentication.isAuthenticated());
        List<String> authorities = new ArrayList<>();
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            authorities.add(authority.getAuthority());
        }
        data.put("authorities", authorities);
        return Result.success(data);
    }
}
