package com.kewen.framework.cloud.security.client.config;

import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author kewen
 * @descrpition
 * @since 2022-12-16 17:42
 */
@Configuration
@PropertySource(name = "oAuthClientConfigPropertySource",value = "classpath:application-securityclient.properties")
public class OauthClientConfig {

}
