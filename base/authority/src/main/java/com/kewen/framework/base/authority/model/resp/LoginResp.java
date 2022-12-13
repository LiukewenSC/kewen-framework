package com.kewen.framework.base.authority.model.resp;

import com.kewen.framework.base.common.model.UserDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @descrpition 登录返回
 * @author kewen
 * @since 2022-11-28 17:16
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResp extends UserDetail {
    private String token;
    public static LoginResp copy(UserDetail userDetail){
        LoginResp resp = new LoginResp();
        resp.setUser(userDetail.getUser());
        resp.setDept(userDetail.getDept());
        resp.setRoles(userDetail.getRoles());
        resp.setPermissions(userDetail.getPermissions());
        return resp;
    }
}
