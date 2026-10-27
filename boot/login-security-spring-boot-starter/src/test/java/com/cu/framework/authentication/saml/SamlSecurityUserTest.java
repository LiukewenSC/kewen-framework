package com.cu.framework.authentication.saml;

import com.cu.framework.authentication.model.SecurityUser;
import com.cu.framework.authentication.model.SecurityUserAuthority;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SAML2 统一认证主体的字段来源与协议原生数据
 *
 * @author kewen
 * @since 2026-10-18
 */
public class SamlSecurityUserTest {

    private SecurityUser localUser() {
        return new SecurityUser()
                .setId(9L)
                .setUsername("demo")
                .setName("演示用户")
                .setEnabled(true)
                .setUserAuthorities(Collections.singletonList(new SecurityUserAuthority("ROLE_USER", null)));
    }

    /**
     * 断言原生数据按组装时给定的内容返回：属性是多值的，会话索引与服务注册标识都要留得住
     */
    @Test
    public void keepsNativeAssertionData() {
        Map<String, List<Object>> attributes = new LinkedHashMap<>();
        attributes.put("email", Arrays.asList("demo@example.com", "demo2@example.com"));
        List<String> sessionIndexes = Collections.singletonList("_abc123");

        SamlSecurityUser principal = SamlSecurityUser.of(localUser(), attributes, sessionIndexes, "demo-saml");

        Assert.assertTrue(principal instanceof Saml2AuthenticatedPrincipal);
        Assert.assertEquals("demo@example.com", principal.getFirstAttribute("email"));
        Assert.assertEquals(2, principal.getAttribute("email").size());
        Assert.assertEquals(sessionIndexes, principal.getSessionIndexes());
        Assert.assertEquals("demo-saml", principal.getRelyingPartyRegistrationId());
    }

    /**
     * 账号信息与权限来自本系统加载的用户，身份源断言只提供标识
     */
    @Test
    public void copiesLocalUserFields() {
        SamlSecurityUser principal = SamlSecurityUser.of(localUser(), null, null, "demo-saml");

        Assert.assertEquals(Long.valueOf(9L), principal.getId());
        Assert.assertEquals("demo", principal.getUsername());
        Assert.assertEquals("演示用户", principal.getName());
        Assert.assertEquals("ROLE_USER", principal.getAuthorities().iterator().next().getAuthority());
        Assert.assertTrue(principal.getAttributes().isEmpty());
        Assert.assertTrue(principal.getSessionIndexes().isEmpty());
    }
}
