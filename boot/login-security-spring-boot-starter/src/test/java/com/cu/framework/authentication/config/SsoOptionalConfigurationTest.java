package com.cu.framework.authentication.config;

import com.cu.framework.authentication.loginpage.LoginOptionsFactory;
import com.cu.framework.authentication.oauth2.Oauth2Config;
import com.cu.framework.authentication.oauth2.Oauth2OidcProperties;
import com.cu.framework.authentication.password.config.PasswordSecurityConfig;
import com.cu.framework.authentication.response.SecurityAuthenticationSuccessHandler;
import com.cu.framework.authentication.saml.SamlConfig;
import com.cu.framework.authentication.saml.properties.SamlProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 纯账号密码登录场景的装配契约：没有 OAuth2 clients 与 SAML registrations 时，
 * 两个单点登录配置类都不装配，但属性 Bean 仍然存在，登录基础配置可正常启动。
 *
 * @author kewen
 * @since 2026-10-27
 */
public class SsoOptionalConfigurationTest {

    @Test
    public void oauth2ConditionOnlyMatchesWhenClientsConfigured() {
        try (AnnotationConfigApplicationContext context = context(new MockEnvironment(), Oauth2ProbeConfig.class)) {
            assertFalse(context.containsBean("oauth2ConfiguredProbe"));
        }

        MockEnvironment configured = new MockEnvironment()
                .withProperty("cu.security.login.oauth2.clients[0].registration-id", "probe");
        try (AnnotationConfigApplicationContext context = context(configured, Oauth2ProbeConfig.class)) {
            assertTrue(context.containsBean("oauth2ConfiguredProbe"));
        }
    }

    @Test
    public void samlConditionOnlyMatchesWhenRegistrationsConfigured() {
        try (AnnotationConfigApplicationContext context = context(new MockEnvironment(), SamlProbeConfig.class)) {
            assertFalse(context.containsBean("samlConfiguredProbe"));
        }

        MockEnvironment configured = new MockEnvironment()
                .withProperty("cu.security.login.saml.registrations[0].registration-id", "probe");
        try (AnnotationConfigApplicationContext context = context(configured, SamlProbeConfig.class)) {
            assertTrue(context.containsBean("samlConfiguredProbe"));
        }
    }

    @Test
    public void loginConfigurationStartsWithNoSsoProperties() {
        try (AnnotationConfigApplicationContext context = context(new MockEnvironment(),
                SecurityBeanConfig.class,
                PasswordSecurityConfig.class,
                Oauth2Config.class,
                SamlConfig.class,
                TestSupportConfig.class)) {
            assertNotNull(context.getBean(Oauth2OidcProperties.class));
            assertNotNull(context.getBean(SamlProperties.class));
            assertNotNull(context.getBean(LoginOptionsFactory.class));
            assertEquals(0, context.getBeanNamesForType(Oauth2Config.class).length);
            assertEquals(0, context.getBeanNamesForType(SamlConfig.class).length);
        }
    }

    private AnnotationConfigApplicationContext context(MockEnvironment environment, Class<?>... configurations) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.setEnvironment(environment);
        context.register(configurations);
        context.refresh();
        return context;
    }

    @Configuration
    @Conditional(Oauth2ConfiguredCondition.class)
    static class Oauth2ProbeConfig {
        @Bean
        String oauth2ConfiguredProbe() {
            return "ok";
        }
    }

    @Configuration
    @Conditional(SamlConfiguredCondition.class)
    static class SamlProbeConfig {
        @Bean
        String samlConfiguredProbe() {
            return "ok";
        }
    }

    @Configuration
    static class TestSupportConfig {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        SecurityAuthenticationSuccessHandler securityAuthenticationSuccessHandler() {
            return new SecurityAuthenticationSuccessHandler() {
                @Override
                public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                    Authentication authentication) {
                }

                @Override
                public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                            Authentication authentication) {
                }
            };
        }

        @Bean
        HandlerExceptionResolver handlerExceptionResolver() {
            return new HandlerExceptionResolver() {
                @Override
                public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                                     Object handler, Exception ex) {
                    return null;
                }
            };
        }

        @Bean
        RequestMappingHandlerMapping requestMappingHandlerMapping() {
            return new RequestMappingHandlerMapping() {
                @Override
                public void afterPropertiesSet() {
                    // 配置层测试不启动 MVC，仅让 PropertiesUrlSecurityCustomizer 能取到空映射表
                }
            };
        }
    }
}
