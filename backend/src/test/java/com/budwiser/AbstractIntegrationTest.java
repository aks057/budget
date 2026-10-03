package com.budwiser;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base for integration tests: full Spring context + MockMvc against a real Postgres 16 (Testcontainers).
 * The context (and container) is cached and shared by every subclass.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, TestLlmConfiguration.class})
public abstract class AbstractIntegrationTest {

  /** The application's business-timezone clock — tests must use it, not the machine's default zone. */
  @Autowired
  protected Clock clock;

  protected LocalDate today() {
    return LocalDate.now(clock);
  }

  protected YearMonth currentMonth() {
    return YearMonth.now(clock);
  }
}
