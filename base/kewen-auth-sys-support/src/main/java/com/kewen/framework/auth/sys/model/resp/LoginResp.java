package com.kewen.framework.auth.sys.model.resp;

import com.kewen.framework.auth.core.model.AuthUserInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @descrpition 登录返回
 * @author kewen
 * @since 2022-11-28 17:16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResp  {


    private String token;

    private AuthUserInfo userInfo;
}
