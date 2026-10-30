package com.cu.framework.authentication.extension;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.rememberme.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

/**
 *  没考虑明白，rememberMe如果用header传参的话岂不是每个请求都会带上，那就没有意义了，直接把token的时间延长就好了呀
 *  因此token的模式用SpringSession之后这其实也不需要了，
 * @author kewen
 * @since 2024-08-27
 */
@Deprecated
public class PersistentHeaderRememberMeService extends AbstractHeaderRememberMeServices {

    /** 持久化 token 仓库，默认使用内存实现 InMemoryTokenRepositoryImpl */
    private PersistentTokenRepository tokenRepository = new InMemoryTokenRepositoryImpl();
    /** 生成 series 与 token 随机数所用的安全随机数生成器 */
    private SecureRandom random = new SecureRandom();
    /** 生成 series 时随机字节数组的长度，默认 16 */
    private int seriesLength = 16;
    /** 生成 token 时随机字节数组的长度，默认 16 */
    private int tokenLength = 16;

    /**
     * 自动登录 需要解析 原始header的token
     * @param series
     * @param token
     * @param request
     * @param response
     * @return
     * @throws RememberMeAuthenticationException
     * @throws UsernameNotFoundException
     */
    @Override
    protected UserDetails processAutoLoginCookie(String series, String token, HttpServletRequest request, HttpServletResponse response) throws RememberMeAuthenticationException, UsernameNotFoundException {

        PersistentRememberMeToken persistentRememberMeToken = tokenRepository
                .getTokenForSeries(series);
        if (persistentRememberMeToken == null) {
            // No series match, so we can't authenticate using this cookie
            throw new RememberMeAuthenticationException(
                    "No persistent token found for series id: " + series);
        }
        // We have a match for this user/series combination
        if (!token.equals(persistentRememberMeToken.getTokenValue())) {
            // Token doesn't match series value. Delete all logins for this user and throw
            // an exception to warn them.
            tokenRepository.removeUserTokens(persistentRememberMeToken.getUsername());

            throw new CookieTheftException(
                    messages.getMessage(
                            "PersistentTokenBasedRememberMeServices.cookieStolen",
                            "Invalid remember-me token (Series/token) mismatch. Implies previous cookie theft attack."));
        }
        if (persistentRememberMeToken.getDate().getTime() + getTokenValiditySeconds() * 1000L < System
                .currentTimeMillis()) {
            throw new RememberMeAuthenticationException("Remember-me login has expired");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Refreshing persistent login token for user '"
                    + persistentRememberMeToken.getUsername() + "', series '" + persistentRememberMeToken.getSeries() + "'");
        }

        PersistentRememberMeToken newToken = new PersistentRememberMeToken(
                persistentRememberMeToken.getUsername(), persistentRememberMeToken.getSeries(), generateTokenData(), new Date());

        try {
            tokenRepository.updateToken(newToken.getSeries(), newToken.getTokenValue(),
                    newToken.getDate());
            setResponseToken(newToken, request, response);
        }
        catch (Exception e) {
            logger.error("Failed to update token: ", e);
            throw new RememberMeAuthenticationException(
                    "Autologin failed due to data access problem");
        }

        return userDetailsService.loadUserByUsername(persistentRememberMeToken.getUsername());
    }


    /**
     * 登录成功后签发新的持久化 token：生成 series 与 token 存入仓库，并写回响应头；保存失败仅记录日志不中断登录
     */
    @Override
    protected void onLoginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {
        String username = successfulAuthentication.getName();

        logger.debug("Creating new persistent login for user " + username);

        PersistentRememberMeToken persistentToken = new PersistentRememberMeToken(
                username, generateSeriesData(), generateTokenData(), new Date());
        try {
            tokenRepository.createNewToken(persistentToken);
            setResponseToken(persistentToken, request, response);
        }
        catch (Exception e) {
            logger.error("Failed to save persistent token ", e);
        }
    }
    /**
     * 登出：在父类清除响应头的基础上，删除该用户在仓库中的所有持久化 token
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
                       Authentication authentication) {

        super.logout(request, response, authentication);

        if (authentication != null) {
            tokenRepository.removeUserTokens(authentication.getName());
        }

    }

    /**
     * 生成 seriesLength 长度随机字节并 Base64 编码，作为 token 系列号
     * @return Base64 编码的 series 字符串
     */
    protected String generateSeriesData() {
        byte[] newSeries = new byte[seriesLength];
        random.nextBytes(newSeries);
        return new String(Base64.getEncoder().encode(newSeries));
    }

    /**
     * 生成 tokenLength 长度随机字节并 Base64 编码，作为 token 值
     * @return Base64 编码的 token 字符串
     */
    protected String generateTokenData() {
        byte[] newToken = new byte[tokenLength];
        random.nextBytes(newToken);
        return new String(Base64.getEncoder().encode(newToken));
    }


    public void setRandom(SecureRandom random) {
        this.random = random;
    }

    public void setTokenRepository(PersistentTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public void setTokenLength(int tokenLength) {
        this.tokenLength = tokenLength;
    }

    public void setSeriesLength(int seriesLength) {
        this.seriesLength = seriesLength;
    }
}
