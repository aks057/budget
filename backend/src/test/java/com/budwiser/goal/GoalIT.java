package com.budwiser.goal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.budwiser.AbstractIntegrationTest;
import com.budwiser.support.TestApi;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class GoalIT extends AbstractIntegrationTest {
  private static final String GOALS = "/api/v1/goals";

  @Autowired
  private MockMvc mockMvc;
  private TestApi api;
  private String token;

  @BeforeEach
  void setUp() throws Exception {
    api = new TestApi(mockMvc);
    token = api.registerUser();
  }

  @Test
  @DisplayName("PRD example: emergency fund 3,00,000 with 1,20,000 saved, 12 months out → 15,000/month")
  void progressComputedOnRead() throws Exception {
    LocalDate deadline = currentMonth().plusMonths(12).atEndOfMonth();

    api.post(token, GOALS, goal("Emergency Fund", "300000", "120000", deadline))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.data.remaining").value(180000.0))
      .andExpect(jsonPath("$.data.monthsRemaining").value(12))
      .andExpect(jsonPath("$.data.requiredMonthlyContribution").value(15000.0))
      .andExpect(jsonPath("$.data.percentComplete").value(40.0))
      .andExpect(jsonPath("$.data.achieved").value(false));
  }

  @Test
  @DisplayName("updating the saved amount past the target marks the goal achieved")
  void achieved() throws Exception {
    LocalDate deadline = today().plusMonths(6);
    String id = api.create(token, GOALS, goal("Laptop", "80000", "10000", deadline));

    api.put(token, GOALS + "/" + id, goal("Laptop", "80000", "80000", deadline))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.achieved").value(true))
      .andExpect(jsonPath("$.data.requiredMonthlyContribution").value(0.0));
    api.get(token, GOALS).andExpect(jsonPath("$.data.length()").value(1));
  }

  @Test
  @DisplayName("deadline in the past is rejected")
  void pastDeadline() throws Exception {
    api.post(token, GOALS, goal("Trip", "50000", "0", today().minusDays(1)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors[0].code").value("BW-4002"));
  }

  @Test
  @DisplayName("isolation: another user cannot read, change or delete my goals")
  void isolation() throws Exception {
    String id = api.create(token, GOALS, goal("Wedding", "500000", "0", today().plusYears(2)));
    String otherToken = api.registerUser();

    api.get(otherToken, GOALS + "/" + id)
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.errors[0].code").value("BW-4001"));
    api.put(otherToken, GOALS + "/" + id, goal("Mine now", "1", "0", today().plusYears(1)))
      .andExpect(status().isNotFound());
    api.delete(otherToken, GOALS + "/" + id).andExpect(status().isNotFound());
    api.get(otherToken, GOALS).andExpect(jsonPath("$.data.length()").value(0));
  }

  private static String goal(String name, String target, String current, LocalDate targetDate) {
    return """
      {"name": "%s", "targetAmount": %s, "currentAmount": %s, "targetDate": "%s"}
      """.formatted(name, target, current, targetDate);
  }
}
