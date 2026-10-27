package com.cu.framework.authentication.extension;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *  没考虑明白，rememberMe如果用header传参的话岂不是每个请求都会带上，那就没有意义了，直接把token的时间延长就好了呀
 *  因此token的模式用SpringSession之后这其实也不需要了，
 * @author kewen
 * @since 2024-08-27
 */
@Deprecated
public abstract class AbstractHeaderRememberMeServices implements RememberMeServices, LogoutHandler, InitializingBean {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractHeaderRememberMeServices.class);
    /** 携带 remember-me token 的请求头/响应头名称，默认 remember-me-token */
    protected String headerName ="remember-me-token";
    /** 标记本次登录是否要求记住我的请求头或请求参数名，默认 remember-me */
    protected String rememberMeParam ="remember-me";
    /** 构造 RememberMeAuthenticationToken 所用的密钥，启动时校验非空 */
    protected String key="123456";
    /** Header 值中 series 与 token 的拼接分隔符，默认英文逗号 */
    private String split=",";
    /** 用户权限映射器，默认 NullAuthoritiesMapper 原样返回权限 */
    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();
    /** 认证详情构造源，默认从当前请求构造 WebAuthenticationDetails */
    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
    /** 为 true 时忽略 remember-me 参数，始终视为请求了记住我 */
    private boolean alwaysRemember;
    /** 加载用户详情的服务，启动时校验非空，供子类自动登录时使用 */
    protected UserDetailsService userDetailsService;
    /** Spring Security 国际化消息访问器，供子类取错误提示文案 */
    protected final MessageSourceAccessor messages = SpringSecurityMessageSource
            .getAccessor();
    //两周
    private int tokenValiditySeconds = 1209600;


    /** 自动登录前的用户状态校验器，默认校验账号锁定/过期等状态 */
    private UserDetailsChecker userDetailsChecker = new AccountStatusUserDetailsChecker();

    /**
     * 初始化校验：key 必须非空，userDetailsService 必须已注入，否则启动失败
     */
    @Override
    public void afterPropertiesSet() {
        Assert.hasLength(key, "key cannot be empty or null");
        Assert.notNull(userDetailsService, "A UserDetailsService is required");
    }

    /**
     * 自动登录
     */
    @Override
    public Authentication autoLogin(HttpServletRequest request, HttpServletResponse response) {
        String header = request.getHeader(headerName);
        if (header == null) {
            return null;
        }
        logger.debug("Remember-me header detected");
        UserDetails user = null;

        Pair<String, String> stringStringPair = decodeHeader(header);
        String presentedSeries = stringStringPair.getLeft();
        String presentedToken = stringStringPair.getRight();
        user = processAutoLoginCookie(presentedSeries,presentedToken, request, response);
        userDetailsChecker.check(user);

        logger.debug("Remember-me header accepted");

        return createSuccessfulAuthentication(request, user);
    }
    /**
     * 基于校验通过的用户详情构造 RememberMe 自动登录认证结果
     * @param request 当前请求，用于构造认证详情
     * @param user 已通过状态校验的用户详情
     * @return 携带 key 与用户权限的 RememberMeAuthenticationToken
     */
    protected Authentication createSuccessfulAuthentication(HttpServletRequest request, UserDetails user) {
        RememberMeAuthenticationToken auth = new RememberMeAuthenticationToken(key, user,
                authoritiesMapper.mapAuthorities(user.getAuthorities()));
        auth.setDetails(authenticationDetailsSource.buildDetails(request));
        return auth;
    }

    /**
     * 自动登录
     * @param series  解密后的series
     * @param token 解密后的token
     */
    protected abstract UserDetails processAutoLoginCookie(String series,String token,
                                                          HttpServletRequest request, HttpServletResponse response)
            throws RememberMeAuthenticationException, UsernameNotFoundException;


    /**
     * 登录失败
     */
    @Override
    public void loginFail(HttpServletRequest request, HttpServletResponse response) {
        //todo 这里要不要考虑清除掉存储
        response.setHeader(headerName,null);
        onLoginFail(request, response);
    }
    /**
     * 登录失败后的钩子方法，默认空实现，子类可覆写（如清除已存储的 token）
     */
    protected void onLoginFail(HttpServletRequest request, HttpServletResponse response){
    }

    /**
     * 登录成功的后续处理
     */
    @Override
    public void loginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {
        if (!rememberMeRequested(request)) {
            logger.debug("Remember-me login not requested.");
            return;
        }

        onLoginSuccess(request, response, successfulAuthentication);
    }
    /**
     * 登录成功且请求了记住我之后的回调，由子类实现具体的 token 签发与存储逻辑
     * @param request 当前请求
     * @param response 当前响应，用于写回 remember-me token 响应头
     * @param successfulAuthentication 登录成功的认证结果
     */
    protected abstract void onLoginSuccess(HttpServletRequest request,
                                           HttpServletResponse response, Authentication successfulAuthentication);

    /**
     * 登出
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        response.setHeader(headerName,null);
    }

    /**
     * 是否支持rememberMe
     */
    protected boolean rememberMeRequested(HttpServletRequest request) {
        if (alwaysRemember) {
            return true;
        }

        String rememberMeStr = request.getHeader(rememberMeParam);
        if (StringUtils.isBlank(rememberMeStr)) {
            rememberMeStr = request.getParameter(rememberMeParam);
        }
        if (rememberMeStr != null) {
            if (rememberMeStr.equalsIgnoreCase("true") || rememberMeStr.equalsIgnoreCase("on")
                    || rememberMeStr.equalsIgnoreCase("yes") || rememberMeStr.equals("1")) {
                return true;
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Did not send remember-me header (principal did not set parameter '"
                    + rememberMeParam + "')");
        }

        return false;
    }

    /**
     * 设置返回token
     */
    protected void setResponseToken(PersistentRememberMeToken token, HttpServletRequest request,
                                    HttpServletResponse response) {
        response.setHeader(headerName,encodeHeader(token.getSeries(),token.getTokenValue()));
    }
    /**
     * 按分隔符拆分 remember-me 请求头的值
     * @param header 请求头原始值，格式为 series + 分隔符 + token
     * @return 左值为 series、右值为 token 的键值对
     */
    private Pair<String,String> decodeHeader(String header) {
        String[] split1 = header.split(split);
        return Pair.of(split1[0], split1[1]);
    }

    /**
     * 将 series 与 token 用分隔符拼接为写入响应头的值（未做 Base64 编码）
     * @param series token 系列号
     * @param token token 值
     * @return 拼接后的响应头值
     */
    private String encodeHeader(String series, String token) {
        String concat = series.concat(split).concat(token);
        //return Base64.getEncoder().encodeToString(concat.getBytes());
        return concat;
    }



    public void setAlwaysRemember(boolean alwaysRemember) {
        this.alwaysRemember = alwaysRemember;
    }

    public void setTokenValiditySeconds(int tokenValiditySeconds) {
        this.tokenValiditySeconds = tokenValiditySeconds;
    }

    protected int getTokenValiditySeconds() {
        return tokenValiditySeconds;
    }

    protected AuthenticationDetailsSource<HttpServletRequest, ?> getAuthenticationDetailsSource() {
        return authenticationDetailsSource;
    }

    public void setAuthenticationDetailsSource(
            AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        Assert.notNull(authenticationDetailsSource,
                "AuthenticationDetailsSource cannot be null");
        this.authenticationDetailsSource = authenticationDetailsSource;
    }

    /**
     * Sets the strategy to be used to validate the {@code UserDetails} object obtained
     * for the user when processing a remember-me cookie to automatically log in a user.
     *
     * @param userDetailsChecker the strategy which will be passed the user object to
     * allow it to be rejected if account should not be allowed to authenticate (if it is
     * locked, for example). Defaults to a {@code AccountStatusUserDetailsChecker}
     * instance.
     *
     */
    public void setUserDetailsChecker(UserDetailsChecker userDetailsChecker) {
        this.userDetailsChecker = userDetailsChecker;
    }

    public void setAuthoritiesMapper(GrantedAuthoritiesMapper authoritiesMapper) {
        this.authoritiesMapper = authoritiesMapper;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
