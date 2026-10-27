package com.cu.framework.authentication.response;

import com.cu.framework.authentication.oauth2.Oauth2OidcProperties;
import com.cu.framework.authentication.properties.SsoLoginProperties;
import com.cu.framework.authentication.saml.properties.SamlProperties;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 认证结果的交付姿态判定器：决定这次认证结果该返回 JSON 还是把浏览器整页送回前端。
 * <p>
 * 判定依据是请求命中的认证端点属于哪一类协议，而不是浏览器请求特征，原因是认证失败时
 * 处理器只拿得到 {@code AuthenticationException}，其中的 Authentication 对象并不可靠；
 * 而三类协议的处理端点由各自的配置项给出，成功与失败都落在同一个请求上，可以共用一条规则。
 * 账号密码端点与一切非认证端点（业务接口、静态页）都不在名单内，因此恒返回 JSON。
 *
 * @author kewen
 * @since 2026-10-16
 */
public class LoginDeliveryResolver {

    /**
     * 端点路径匹配器。这里用的是 Spring 的 Ant 语义而不是 Spring Security 的
     * {@code AntPathRequestMatcher}，后者对以 {@code /*} 结尾的模板有"尾斜杠"要求，
     * 匹配不上 {@code /login/oauth2/code/demo-oauth2} 这类真实回调地址
     */
    private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * 取请求在应用内的路径，剥离 contextPath 与矩阵参数
     */
    private static final UrlPathHelper URL_PATH_HELPER = UrlPathHelper.defaultInstance;

    /**
     * 回跳地址上表示流程结果的参数名，供前端判断是成功回来还是失败回来
     */
    public static final String STATE_PARAMETER = "loginState";

    /**
     * 单点登录流程结束后的结果标记取值
     */
    public static final String STATE_SUCCESS = "success";

    /**
     * 单点登录流程失败后的结果标记取值
     */
    public static final String STATE_FAILURE = "failure";

    private final SsoLoginProperties ssoLoginProperties;

    /**
     * 三类单点登录协议中，负责接收身份源回调的处理端点路径模板
     */
    private final List<String> ssoProcessingPatterns;

    public LoginDeliveryResolver(SsoLoginProperties ssoLoginProperties,
                                 Oauth2OidcProperties oauth2OidcProperties,
                                 SamlProperties samlProperties) {
        this.ssoLoginProperties = ssoLoginProperties;
        this.ssoProcessingPatterns = new ArrayList<>();
        this.ssoProcessingPatterns.add(oauth2OidcProperties.getLoginProcessingUrl());
        this.ssoProcessingPatterns.add(samlProperties.getLoginProcessingUrl());
    }

    /**
     * 本次认证结果是否应改为重定向交付：配置了回跳地址，且请求命中某个单点登录协议的处理端点
     *
     * @param request 触发本次认证结果的请求
     * @return 需要重定向返回 true，保持 JSON 返回 false
     */
    public boolean shouldRedirect(HttpServletRequest request) {
        String resultUrl = ssoLoginProperties.getResultUrl();
        if (resultUrl == null || resultUrl.trim().isEmpty()) {
            return false;
        }
        String path = URL_PATH_HELPER.getPathWithinApplication(request);
        for (String pattern : ssoProcessingPatterns) {
            if (pattern != null && PATH_MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 把浏览器送回前端地址并附上结果标记，响应体不写入任何业务数据
     *
     * @param response 待写出的响应
     * @param state    结果标记取值，取 {@link #STATE_SUCCESS} 或 {@link #STATE_FAILURE}
     * @throws IOException 重定向写出失败时抛出
     */
    public void redirect(HttpServletResponse response, String state) throws IOException {
        response.sendRedirect(buildRedirectUrl(ssoLoginProperties.getResultUrl(), state));
    }

    /**
     * 拼接回跳地址与结果标记，地址自带 query 时改用 {@code &} 追加
     *
     * @param resultUrl 配置的结果页地址，相对或绝对均可
     * @param state       结果标记取值
     * @return 完整重定向地址，不含会话标识与令牌
     */
    static String buildRedirectUrl(String resultUrl, String state) {
        String separator = resultUrl.indexOf('?') >= 0 ? "&" : "?";
        return resultUrl + separator + STATE_PARAMETER + "=" + state;
    }
}
