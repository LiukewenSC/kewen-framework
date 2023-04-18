package com.kewen.framework.auth.core.model;

import com.kewen.framework.common.core.model.IUser;

import java.util.List;
import java.util.stream.Collectors;

/**
 *  用户信息，带权限 抽象
 *  <E> 泛型权限实体
 * @author kewen
 * @since 2023-04-10
 */
public interface AuthUserInfo<E extends AuthEntity> extends IUser {



     List<E> authorities();



}
