package com.kewen.framework.boot.auth.web.service.impl;



import com.kewen.framework.base.common.exception.AuthorizationException;
import com.kewen.framework.boot.auth.bussiness.model.LoginReq;
import com.kewen.framework.boot.auth.bussiness.model.LoginResp;
import com.kewen.framework.boot.auth.AuthHandler;
import com.kewen.framework.boot.auth.AuthUserCredential;
import com.kewen.framework.boot.auth.AuthUserInfo;
import com.kewen.framework.boot.auth.web.service.LoginService;
import com.kewen.framework.boot.auth.web.WebAuthUserInfoContextContainer;
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
    AuthHandler authHandler;


    @Autowired
    WebAuthUserInfoContextContainer webAuthUserInfoContextContainer;

    @Autowired
    PasswordEncoder passwordEncoder;



    @Override
    public LoginResp login(LoginReq loginReq) {

        //查询用户
        Long userId = authHandler.getUserId(loginReq.getUsername());
        AuthUserInfo authUser = authHandler.getAuthUser(userId);
        AuthUserCredential userCredential = authHandler.getCredential(userId);

        checkAccount(userCredential);

        passwordEncoder.matches(loginReq.getPassword(),userCredential.getPassword());

        // 保存
        String token = webAuthUserInfoContextContainer.saveAuthUserInfo(authUser);

        LoginResp loginResp = new LoginResp();

        return loginResp;
    }
    private void checkAccount(AuthUserCredential credential){
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
