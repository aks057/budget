package com.budwiser.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param secure send the refresh cookie only over HTTPS; disabled in the dev profile for plain http://localhost
 */
@ConfigurationProperties(prefix = "budwiser.security.cookie")
public record AuthCookieProperties(boolean secure) {}
