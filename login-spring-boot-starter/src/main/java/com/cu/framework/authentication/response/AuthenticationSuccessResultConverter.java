package com.cu.framework.authentication.response;

import com.cu.framework.authentication.model.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 认证成功结果转换器：将认证成功后产生的 Authentication 转换为返回给前端的 JsonSuccessResult，
 * 或转换为用户上下文使用的 SecurityUser；不同认证协议可提供各自的实现
 * @author kewen
 * @since 2026-02-22
 */
public interface AuthenticationSuccessResultConverter {

    /**
     * 判断本转换器是否支持处理该认证结果
     * @param authentication 认证成功产生的认证对象
     * @return 支持返回 true
     */
    boolean support(Authentication authentication);

    /**
     * 将认证结果转换为登录成功响应体
     * @param authentication 认证成功产生的认证对象
     * @return 登录成功响应体
     */
    LoginSuccessResult convert(Authentication authentication);

    /**
     * 将认证结果转换为用户上下文使用的 SecurityUser
     * @param userDetailsService 用户详情服务，供需要重新加载用户的实现使用
     * @param authentication 认证成功产生的认证对象
     * @return 安全用户对象
     */
    SecurityUser convert(UserDetailsService userDetailsService, Authentication authentication);

    /**
     * 默认转换实现：将 SecurityUser 的基本信息字段逐一拷贝到 JsonSuccessResult（不含 token 与 password）
     * @param securityUser 安全用户对象
     * @return 登录成功响应体
     */
    default LoginSuccessResult convert(SecurityUser securityUser) {
        LoginSuccessResult result = new LoginSuccessResult();
        result.setName(securityUser.getName());
        result.setNickName(securityUser.getNickName());
        result.setUsername(securityUser.getUsername());
        result.setPhone(securityUser.getPhone());
        result.setEmail(securityUser.getEmail());
        result.setAvatarFileId(securityUser.getAvatarFileId());
        result.setGender(securityUser.getGender());
        result.setAuthorities(securityUser.getUserAuthorities());
        result.setLoginTime(securityUser.getLoginTime());
        return result;
    }

}
