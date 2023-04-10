package com.kewen.framework.boot.auth;

import com.kewen.framework.base.common.model.IUser;

import java.util.List;
import java.util.stream.Collectors;

/**
 *  用户信息，带权限 抽象
 * @author kewen
 * @since 2023-04-10
 */
public interface AuthUserInfo extends IUser {



     List<AuthEntity> authEntities();
     default List<String> authorities(){
          return authEntities().stream().map(AuthEntity::getAuth).collect(Collectors.toList());
     }

}
