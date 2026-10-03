package com.budwiser.common.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Injectable clock in the business timezone, so LocalDate.now(clock) is the user's "today" (not the server's UTC date)
 * and time-dependent logic is testable with a fixed clock.
 */
@Configuration
public class ClockConfig {

  @Bean
  public Clock clock(AppProperties appProperties) {
    return Clock.system(appProperties.zone());
  }
}
