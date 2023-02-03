package com.kewen.framework.cloud.security.client.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2RestOperationsConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author kewen
 * @descrpition
 * @since 2022-12-16 17:42
 */
@Configuration
@EnableOAuth2Sso
@PropertySource(name = "oAuthClientConfigPropertySource",value = "classpath:application-securityclient.properties")
public class OauthClientConfig {

}
