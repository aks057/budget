package com.budwiser.insight;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.budwiser.AbstractIntegrationTest;
import com.budwiser.insight.scheduler.InsightJob;
import com.budwiser.support.TestApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Proactive insights end to end: rules on real data, idempotent storage, read state, isolation and the daily job.
 */
class InsightIT extends AbstractIntegrationTest {
  private static final String INSIGHTS = "/api/v1/insights";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private InsightJob insightJob;
  @Autowired
  private JdbcTemplate jdbcTemplate;
  private TestApi api;
  private String token;

  @BeforeEach
  void setUp() throws Exception {
    api = new TestApi(mockMvc);
    token = api.registerUser();
  }

  @Test
  @DisplayName("refresh raises an exceeded-budget insight once; a second refresh adds nothing")
  void refreshIsIdempotent() throws Exception {
    overspendFood(token);

    api.post(token, INSIGHTS + "/refresh", "")
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.created").value(1));
    api.post(token, INSIGHTS + "/refresh", "").andExpect(jsonPath("$.data.created").value(0));

    api.get(token, INSIGHTS)
      .andExpect(jsonPath("$.data.length()").value(1))
      .andExpect(jsonPath("$.data[0].type").value("BUDGET_EXCEEDED"))
      .andExpect(jsonPath("$.data[0].severity").value("CRITICAL"))
      .andExpect(jsonPath("$.data[0].title").value("Food budget exceeded"))
      .andExpect(jsonPath("$.data[0].read").value(false));
  }

  @Test
  @DisplayName("unread count, mark one read (idempotent), mark all read")
  void readState() throws Exception {
    overspendFood(token);
    api.post(token, INSIGHTS + "/refresh", "");
    String insightId = TestApi.read(api.get(token, INSIGHTS), "$.data[0].id");

    api.get(token, INSIGHTS + "/unread-count").andExpect(jsonPath("$.data.count").value(1));
    api.post(token, INSIGHTS + "/" + insightId + "/read", "").andExpect(status().isOk());
    api.post(token, INSIGHTS + "/" + insightId + "/read", "").andExpect(status().isOk());
    api.get(token, INSIGHTS + "/unread-count").andExpect(jsonPath("$.data.count").value(0));
    api.get(token, INSIGHTS).andExpect(jsonPath("$.data[0].read").value(true));

    api.post(token, INSIGHTS + "/read-all", "").andExpect(jsonPath("$.data.updated").value(0));
  }

  @Test
  @DisplayName("isolation: another user cannot see or mark my insights (404, not 403)")
  void isolation() throws Exception {
    overspendFood(token);
    api.post(token, INSIGHTS + "/refresh", "");
    String insightId = TestApi.read(api.get(token, INSIGHTS), "$.data[0].id");
    String otherToken = api.registerUser();

    api.get(otherToken, INSIGHTS).andExpect(jsonPath("$.data").isEmpty());
    api.post(otherToken, INSIGHTS + "/" + insightId + "/read", "")
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.errors[0].code").value("BW-6001"));
    api.get(token, INSIGHTS + "/unread-count").andExpect(jsonPath("$.data.count").value(1));
  }

  @Test
  @DisplayName("daily job: generates for every user, is safe to re-run, and purges expired insights")
  void dailyJob() throws Exception {
    String otherToken = api.registerUser();
    overspendFood(token);
    overspendFood(otherToken);

    InsightJob.RunSummary first = insightJob.run();
    InsightJob.RunSummary second = insightJob.run();

    assertThat(first.users()).isGreaterThanOrEqualTo(2);
    assertThat(first.created()).isGreaterThanOrEqualTo(2);
    assertThat(first.failed()).isZero();
    assertThat(second.created()).isZero();
    api.get(token, INSIGHTS + "/unread-count").andExpect(jsonPath("$.data.count").value(1));
    api.get(otherToken, INSIGHTS + "/unread-count").andExpect(jsonPath("$.data.count").value(1));

    // Age one insight past the retention window: the run (generate, then purge) deletes it.
    String insightId = TestApi.read(api.get(token, INSIGHTS), "$.data[0].id");
    jdbcTemplate.update("UPDATE insights SET created_at = 0 WHERE id = ?", Long.valueOf(insightId));
    InsightJob.RunSummary third = insightJob.run();

    assertThat(third.purged()).isGreaterThanOrEqualTo(1);
    api.get(token, INSIGHTS).andExpect(jsonPath("$.data").isEmpty());
    api.get(otherToken, INSIGHTS).andExpect(jsonPath("$.data.length()").value(1));
  }

  /** A 1,000 Food budget with 1,200 spent today. */
  private void overspendFood(String userToken) throws Exception {
    String food = api.categoryId(userToken, "Food");
    api.create(userToken, "/api/v1/budgets", """
      {"categoryId": %s, "amount": 1000}
      """.formatted(food));
    api.create(userToken, "/api/v1/transactions", """
      {"amount": 1200, "categoryId": %s, "description": "Groceries", "transactionDate": "%s"}
      """.formatted(food, today()));
  }
}
