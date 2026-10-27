package com.cu.framework.authentication.oauth2;

import com.cu.framework.authentication.oauth2.Oauth2OidcProperties.Oauth2OidcRegistrationClientProperties;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * userinfo 用户信息服务按客户端配置接线的正确性，以及配置缺失时的失败方向
 *
 * @author kewen
 * @since 2026-10-19
 */
public class RoutingOAuth2UserServiceTest {

    /**
     * RestOperationBuilder 构造的 RestTemplate 只装了 2 个消息转换器；
     * DefaultOAuth2UserService 默认的 RestTemplate 使用 Spring 的全套转换器。
     * 以此区分"是否被替换过 RestOperations"，无需真实发起 HTTP 请求。
     * <p>按字段而非按实例类比对，是因为被替换过的实例仍是一个 DefaultOAuth2UserService，
     * 用 getClass() 精确比对区分不出"安全实例"与"豁免实例"。
     */
    private static boolean restOperationsWasReplaced(OAuth2UserService<?, OAuth2User> service) throws Exception {
        Field field = DefaultOAuth2UserService.class.getDeclaredField("restOperations");
        field.setAccessible(true);
        Object restOperations = field.get(service);
        if (!(restOperations instanceof RestTemplate)) {
            return false;
        }
        List<?> converters = ((RestTemplate) restOperations).getMessageConverters();
        return converters.size() == 2;
    }

    private static Oauth2OidcRegistrationClientProperties client(String registrationId, boolean ignoreSsl) {
        Oauth2OidcRegistrationClientProperties client = new Oauth2OidcRegistrationClientProperties();
        client.setRegistrationId(registrationId);
        client.setIgnoreSsl(ignoreSsl);
        return client;
    }

    private static RoutingOAuth2UserService routerWith(Oauth2OidcRegistrationClientProperties... clients) {
        Oauth2OidcProperties properties = new Oauth2OidcProperties();
        properties.setClients(Arrays.asList(clients));
        return new RoutingOAuth2UserService(properties);
    }

    /**
     * 语义一：ignore-ssl 不配置时默认执行证书校验，不得有任何代码路径默认跳过
     */
    @Test
    public void defaultIsSslVerification() throws Exception {
        Oauth2OidcRegistrationClientProperties unset = client("github", false);
        Assert.assertFalse("ignoreSsl 的默认值必须是不跳过校验", unset.isIgnoreSsl());

        OAuth2UserService<?, OAuth2User> resolved = routerWith(unset).resolve("github");

        Assert.assertSame("未开启 ignore-ssl 的客户端应复用执行证书校验的实例",
                RoutingOAuth2UserService.SECURE_FALLBACK, resolved);
        Assert.assertFalse(restOperationsWasReplaced(resolved));
    }

    /**
     * 语义二：豁免以单个客户端为粒度生效，一个 IDP 的豁免不得串到另一个身份源
     */
    @Test
    public void ignoreSslIsAppliedPerClient() throws Exception {
        RoutingOAuth2UserService router = routerWith(client("internal", true), client("github", false));

        OAuth2UserService<?, OAuth2User> insecure = router.resolve("internal");
        OAuth2UserService<?, OAuth2User> secure = router.resolve("github");

        Assert.assertNotSame("两个客户端不得共用同一个实例", insecure, secure);
        Assert.assertTrue("ignore-ssl=true 的客户端应拿到被替换过 RestOperations 的实例",
                restOperationsWasReplaced(insecure));
        Assert.assertFalse("同一个 IDP 的豁免不得影响另一个客户端",
                restOperationsWasReplaced(secure));
        Assert.assertSame(RoutingOAuth2UserService.SECURE_FALLBACK, secure);
    }

    /**
     * 语义三：registrationId 未匹配时落到标准校验实例，失败方向是"登录不上"而不是"悄悄不校验"
     */
    @Test
    public void unknownRegistrationIdFallsBackToSecure() throws Exception {
        // 只配置一个豁免客户端：若回落逻辑错取已配置实例，本用例即变红
        RoutingOAuth2UserService router = routerWith(client("internal", true));

        OAuth2UserService<?, OAuth2User> resolved = router.resolve("typo-in-config");

        Assert.assertSame("配置中不存在的 registrationId 必须落到安全实例，而不是已配置的豁免实例",
                RoutingOAuth2UserService.SECURE_FALLBACK, resolved);
        Assert.assertFalse(restOperationsWasReplaced(resolved));
    }

    /**
     * 已配置的 registrationId 各自解析到自己的实例，且安全实例是共享单例而非每次新建
     */
    @Test
    public void configuredIdsResolveToTheirOwnInstances() throws Exception {
        RoutingOAuth2UserService router = routerWith(client("internal", true), client("github", false));

        Assert.assertNotSame(router.resolve("internal"), router.resolve("github"));
        Assert.assertSame("同一个 Router 内多次解析安全客户端必须拿到同一个实例",
                router.resolve("github"), router.resolve("github"));
    }

    /**
     * 安全实例跨 Router 共享：每个应用上下文只有一个执行标准校验的 HTTP 客户端实例
     */
    @Test
    public void secureFallbackIsSharedAcrossRouters() throws Exception {
        Assert.assertSame(RoutingOAuth2UserService.SECURE_FALLBACK,
                routerWith(client("a", false)).resolve("a"));
        Assert.assertSame(RoutingOAuth2UserService.SECURE_FALLBACK,
                routerWith(client("b", false)).resolve("b"));
        Assert.assertFalse(restOperationsWasReplaced(RoutingOAuth2UserService.SECURE_FALLBACK));
    }
}
