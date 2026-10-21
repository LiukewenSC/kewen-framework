package com.cu.framework.rbac.security;

import com.cu.framework.auth.rbac.composite.SysUserComposite;
import com.cu.framework.auth.rbac.composite.model.UserAuthInfo;
import com.cu.framework.auth.rbac.model.UserAuthObject;
import com.cu.framework.auth.rbac.mp.entity.SysUser;
import com.cu.framework.auth.rbac.mp.entity.SysUserCredential;
import com.cu.framework.authentication.model.SecurityUser;
import com.cu.framework.authentication.model.SecurityUserAuthority;
import com.cu.framework.authentication.service.SecurityUserDetailsService;
import lombok.Setter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.stream.Collectors;


/**
 * 基于Rbac的权限实现体系，前提是要引入了 <cu-auth-starter-rbac>
 *
 * @author kewen
 * @since 2024-07-02
 */

@Setter
public class RbacSecurityUserDetailsService implements SecurityUserDetailsService {

    private SysUserComposite sysUserComposite;

    public UserAuthObject getUserAuthObjectByUsername(String username) {
        return sysUserComposite.getUserAuthObjectByUsername(username);
    }

    /**
     * 按用户名加载用户、凭证与权限对象并组装为 SecurityUser
     *
     * @param username 登录用户名
     * @return 组装完成的安全用户
     * @throws UsernameNotFoundException 用户名不存在时抛出
     */

    @Override
    public SecurityUser loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAuthObject userAuthObject = getUserAuthObjectByUsername(username);
        if (userAuthObject == null) {
            throw new UsernameNotFoundException("未找到用户");
        }
        SysUser sysUser = userAuthObject.getSysUser();

        SecurityUser securityUser = new SecurityUser()
                .setId(sysUser.getId())
                .setName(sysUser.getName())
                .setUsername(sysUser.getUsername())
                .setNickName(sysUser.getNickName())
                .setPhone(sysUser.getPhone())
                .setEmail(sysUser.getEmail())
                .setAvatarFileId(sysUser.getAvatarFileId())
                // 库中 gender 是 tinyint，认证模型里是 String（单点登录子类型要实现 OidcUser 的标准声明访问器）
                .setGender(sysUser.getGender() == null ? null : String.valueOf(sysUser.getGender()))
                .setCreateTime(sysUser.getCreateTime())
                .setUpdateTime(sysUser.getUpdateTime());
        SysUserCredential credential = userAuthObject.getSysUserCredential();
        UserAuthInfo userAuthInfo = userAuthObject.getAuthInfo();
        securityUser.setUserAuthorities(
                userAuthInfo.listBaseAuth().stream().map(
                        a -> new SecurityUserAuthority(a.getAuth(), a.getDescription())
                ).collect(Collectors.toList())
        );

        if (credential != null) {
            securityUser
                    .setPassword(credential.getPassword())
                    .setPasswordExpiredTime(credential.getPasswordExpiredTime())
                    .setAccountLockedDeadline(credential.getAccountLockedDeadline())
                    .setEnabled(credential.getEnabled());
        }
        return securityUser;
    }

    /**
     * 更新指定用户的密码
     *
     * @param user                 待更新的用户
     * @param encryptedNewPassword 已加密的新密码
     * @return 原用户对象
     * @throws RuntimeException 密码更新失败时抛出
     */

    @Override
    public UserDetails updatePassword(UserDetails user, String encryptedNewPassword) {
        boolean b = sysUserComposite.updatePassword(user.getUsername(), encryptedNewPassword);
        if (!b) {
            throw new RuntimeException("密码更新失败");
        }
        return user;
    }
}
