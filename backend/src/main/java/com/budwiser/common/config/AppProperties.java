package com.budwiser.common.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @param frontendUrl base URL the backend redirects the browser to after Google sign-in
 * @param zone        business timezone that defines "today" and "this month" for users (timestamps stay UTC epoch ms)
 */
@Validated
@ConfigurationProperties(prefix = "budwiser.app")
public record AppProperties(@NotBlank String frontendUrl, @NotNull ZoneId zone) {}
