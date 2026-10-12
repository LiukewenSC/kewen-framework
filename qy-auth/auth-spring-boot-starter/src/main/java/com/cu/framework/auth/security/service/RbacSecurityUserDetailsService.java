package com.cu.framework.auth.security.service;

import com.cu.framework.auth.rbac.composite.SysUserComposite;
import com.cu.framework.auth.rbac.composite.model.SimpleAuthObject;
import com.cu.framework.auth.rbac.extension.RbacCurrentUserService;
import com.cu.framework.auth.rbac.model.UserAuthObject;
import com.cu.framework.auth.rbac.mp.entity.SysUser;
import com.cu.framework.auth.rbac.mp.entity.SysUserCredential;
import com.cu.framework.auth.security.model.SecurityUser;
import lombok.Setter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * 基于Rbac的权限实现体系，前提是要引入了 <cu-auth-starter-rbac>
 *
 * @author kewen
 * @since 2024-07-02
 */
@Setter
public class RbacSecurityUserDetailsService implements SecurityUserDetailsService, RbacCurrentUserService<SecurityUser> {

    SysUserComposite sysUserComposite;
    PasswordEncoder passwordEncoder;

    /**
     * 按用户名加载用户、凭证与权限对象并组装为 SecurityUser
     * @param username 登录用户名
     * @return 组装完成的安全用户
     * @throws UsernameNotFoundException 用户名不存在时抛出
     */
    @Override
    public SecurityUser loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAuthObject userAuthObject = sysUserComposite.loadByUsername(username);
        if (userAuthObject == null) {
            throw new UsernameNotFoundException("未找到用户");
        }
        SysUser sysUser = userAuthObject.getSysUser();
        SysUserCredential credential = userAuthObject.getSysUserCredential();
        SimpleAuthObject authObject = userAuthObject.getAuthObject();

        return new SecurityUser()
                .setId(sysUser.getId())
                .setName(sysUser.getName())
                .setUsername(sysUser.getUsername())
                .setNickName(sysUser.getNickName())
                .setPhone(sysUser.getPhone())
                .setEmail(sysUser.getEmail())
                .setAvatarFileId(sysUser.getAvatarFileId())
                .setPassword(credential.getPassword())
                .setGender(sysUser.getGender())
                .setAuthObject(authObject)
                .setPasswordExpiredTime(credential.getPasswordExpiredTime())
                .setAccountLockedDeadline(credential.getAccountLockedDeadline())
                .setEnabled(credential.getEnabled())
                .setCreateTime(sysUser.getCreateTime())
                .setUpdateTime(sysUser.getUpdateTime())
                ;
    }

    /**
     * 更新指定用户的密码
     * @param user 待更新的用户
     * @param newPassword 已加密的新密码
     * @return 原用户对象
     * @throws RuntimeException 密码更新失败时抛出
     */
    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        boolean b = sysUserComposite.updatePassword(user.getUsername(), newPassword);
        if (!b) {
            throw new RuntimeException("密码更新失败");
        }
        return user;
    }

    /**
     * 校验旧密码后更新为新密码：重新加载用户获取库中密码密文与旧密码比对，
     * 通过后将新密码加密并更新
     * @param currentUser 当前登录用户
     * @param oldPassword 明文旧密码
     * @param newPassword 明文新密码
     * @throws RuntimeException 旧密码错误或更新失败时抛出
     */
    @Override
    public void updatePassword(SecurityUser currentUser, String oldPassword, String newPassword) {
        //原来的上下文中没保存密码，只有再获取一次，也不影响
        SecurityUser securityUser = loadUserByUsername(currentUser.getUsername());
        String dbPassword = securityUser.getPassword();
        if (!passwordEncoder.matches(oldPassword, dbPassword)) {
            throw new RuntimeException("旧密码错误");
        }
        updatePassword(currentUser, passwordEncoder.encode(newPassword));
    }

}
