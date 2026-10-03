package com.budwiser.insight.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Scheduling is only switched on when the insights job is enabled, so tests run without background threads. */
@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "budwiser.insights", name = "enabled", havingValue = "true", matchIfMissing = true)
public class InsightSchedulingConfig {}
