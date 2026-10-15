package com.cu.framework.auth.rbac.extension;


import com.cu.framework.auth.core.entity.CurrentUser;
import com.cu.framework.auth.rbac.composite.SysUserComposite;
import com.cu.framework.auth.rbac.mp.entity.SysUserCredential;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 *  当前用户登录
 * @author kewen
 * @since 2024-08-22
 */
@Setter
public class RbacCurrentUserService {

    private SysUserComposite sysUserComposite;

    private PasswordEncoder passwordEncoder;

    /**PasswordEncoder passwordEncoder
     * 更新密码
     * @param currentUser 当前登录人
     * @param oldPassword 旧密码，未加密
     * @param newPassword 新密码，未加密
     */
     public void updatePassword(CurrentUser currentUser, String oldPassword, String newPassword){
         SysUserCredential credential = sysUserComposite.getUserCredentialByUsername(currentUser.getUsername());
         if (!passwordEncoder.matches(oldPassword, credential.getPassword())) {
             throw new RuntimeException("旧密码不正确");
         }
         String encryptedNewPassword = passwordEncoder.encode(newPassword);

         sysUserComposite.updatePassword(currentUser.getUsername(), encryptedNewPassword);
     }

}
