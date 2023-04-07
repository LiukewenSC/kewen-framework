package com.kewen.framework.boot.auth.web.service.impl;



import com.kewen.framework.base.authority.model.UserCredential;
import com.kewen.framework.base.authority.support.SysUserComposite;
import com.kewen.framework.base.common.exception.AuthorizationException;
import com.kewen.framework.boot.auth.bussiness.model.LoginReq;
import com.kewen.framework.boot.auth.bussiness.model.LoginResp;
import com.kewen.framework.boot.auth.web.support.UserDetailStore;
import com.kewen.framework.boot.auth.web.service.LoginService;
import com.kewen.framework.base.authority.model.UserDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author kewen
 * @descrpition
 * @since 2022-11-28 17:18
 */
@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    SysUserComposite sysUserComposite;


    @Autowired
    UserDetailStore userDetailStore;

    @Autowired
    PasswordEncoder passwordEncoder;



    @Override
    public LoginResp login(LoginReq loginReq) {

        //查询用户
        UserDetail userDetail = sysUserComposite.getUserDetail(loginReq.getUsername());
        UserCredential userCredential = sysUserComposite.getUserCredential(userDetail.getUser().getUserId());

        checkAccount(userCredential);

        passwordEncoder.matches(loginReq.getPassword(),userCredential.getPassword());

        // 保存
        LoginResp loginResp = userDetailStore.saveUserDetail(userDetail);

        return loginResp;
    }
    private void checkAccount(UserCredential credential){
        if (!credential.isEnabled()){
            throw new AuthorizationException("账号未启用");
        }
        if (!credential.isNonLocked()){
            throw new AuthorizationException("账号已锁定");
        }
        if (credential.isNonExpired()){
            throw new AuthorizationException("账号已过期");
        }
    }

}
