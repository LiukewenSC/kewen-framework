package com.cu.framework.authentication.oauth2;

import com.cu.framework.authentication.oauth2.Oauth2Config;
import com.cu.framework.authentication.oauth2.Oauth2OidcProperties;
import com.cu.framework.authentication.oauth2.Oauth2OidcProperties.Oauth2OidcRegistrationClientProperties;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @descrpition userinfo 用户信息服务按客户端配置接线的正确性
 * @author kewen
 * @since 2026-10-13
 */
public class Oauth2UserServiceWiringTest {

    private Oauth2Config configWith(Oauth2OidcRegistrationClientProperties... clients) throws Exception {
        Oauth2OidcProperties properties = new Oauth2OidcProperties();
        properties.setClients(Arrays.asList(clients));

        Oauth2Config config = new Oauth2Config();
        Field field = Oauth2Config.class.getDeclaredField("oauth2OidcProperties");
        field.setAccessible(true);
        field.set(config, properties);
        return config;
    }

    private Oauth2OidcRegistrationClientProperties client(String registrationId, boolean ignoreSsl) {
        Oauth2OidcRegistrationClientProperties client = new Oauth2OidcRegistrationClientProperties();
        client.setRegistrationId(registrationId);
        client.setIgnoreSsl(ignoreSsl);
        return client;
    }

    /**
     * RestOperationBuilder 构造的 RestTemplate 只装了 2 个消息转换器；
     * DefaultOAuth2UserService 默认的 RestTemplate 使用 Spring 的全套转换器。
     * 以此区分"是否被替换过 RestOperations"，无需真实发起 HTTP 请求。
     */
    private boolean restOperationsWasReplaced(OAuth2UserService<OAuth2UserRequest, OAuth2User> service) throws Exception {
        Assert.assertEquals(DefaultOAuth2UserService.class, service.getClass());
        Field field = DefaultOAuth2UserService.class.getDeclaredField("restOperations");
        field.setAccessible(true);
        Object restOperations = field.get(service);
        if (!(restOperations instanceof RestTemplate)) {
            return false;
        }
        List<?> converters = ((RestTemplate) restOperations).getMessageConverters();
        return converters.size() == 2;
    }

    @Test
    public void defaultIsSslVerification() throws Exception {
        Oauth2OidcRegistrationClientProperties unset = client("github", false);
        Assert.assertFalse("ignoreSsl 的默认值必须是不跳过校验", unset.isIgnoreSsl());

        Map<String, OAuth2UserService<OAuth2UserRequest, OAuth2User>> map =
                configWith(unset).buildUserServiceMap();

        Assert.assertSame("未开启 ignore-ssl 的客户端应复用执行证书校验的实例",
                Oauth2Config.SECURE_FALLBACK, map.get("github"));
        Assert.assertFalse(restOperationsWasReplaced(Oauth2Config.SECURE_FALLBACK));
    }

    @Test
    public void ignoreSslIsAppliedPerClient() throws Exception {
        Map<String, OAuth2UserService<OAuth2UserRequest, OAuth2User>> map =
                configWith(client("internal", true), client("github", false)).buildUserServiceMap();

        OAuth2UserService<OAuth2UserRequest, OAuth2User> insecure = map.get("internal");
        OAuth2UserService<OAuth2UserRequest, OAuth2User> secure = map.get("github");

        Assert.assertNotSame("两个客户端不得共用同一个实例", insecure, secure);
        Assert.assertTrue("ignore-ssl=true 的客户端应拿到被替换过的 RestOperations",
                restOperationsWasReplaced(insecure));
        Assert.assertFalse("同一个 IDP 的豁免不得影响另一个客户端",
                restOperationsWasReplaced(secure));
        Assert.assertSame(secure, Oauth2Config.SECURE_FALLBACK);
    }

    @Test
    public void unknownRegistrationIdFallsBackToSecure() throws Exception {
        Map<String, OAuth2UserService<OAuth2UserRequest, OAuth2User>> map =
                configWith(client("internal", true)).buildUserServiceMap();

        Assert.assertFalse(map.containsKey("typo-in-config"));
        Assert.assertSame("配置中不存在的 registrationId 必须落到安全实例，而不是已配置的豁免实例",
                Oauth2Config.SECURE_FALLBACK,
                Oauth2Config.resolveUserService(map, "typo-in-config"));
    }

    @Test
    public void configuredIdResolvesToItsOwnInstance() throws Exception {
        Map<String, OAuth2UserService<OAuth2UserRequest, OAuth2User>> map =
                configWith(client("internal", true), client("github", false)).buildUserServiceMap();

        Assert.assertSame(map.get("internal"), Oauth2Config.resolveUserService(map, "internal"));
        Assert.assertSame(map.get("github"), Oauth2Config.resolveUserService(map, "github"));
    }
}
