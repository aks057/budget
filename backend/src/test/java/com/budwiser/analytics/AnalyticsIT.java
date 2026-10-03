package com.budwiser.analytics;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.nullValue;
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

/**
 * End-to-end over real SQL aggregations. Seeded history:
 * - 3 prior months: Shopping ₹7,000 and a ₹649 "Netflix" bill each month
 * - current month: salary ₹90,000, Food ₹9,500, Shopping ₹10,500 (+50% vs average → spike)
 */
class AnalyticsIT extends AbstractIntegrationTest {
  private static final String ANALYTICS = "/api/v1/analytics";

  @Autowired
  private MockMvc mockMvc;
  private TestApi api;
  private String token;

  @BeforeEach
  void setUp() throws Exception {
    api = new TestApi(mockMvc);
    token = api.registerUser();
    String food = api.categoryId(token, "Food");
    String shopping = api.categoryId(token, "Shopping");
    String bills = api.categoryId(token, "Bills");
    String salary = api.categoryId(token, "Salary");

    // Different descriptions: shopping must not itself look like a recurring bill.
    String[] shoppingTrips = {"Clothes", "Shoes", "Electronics"};
    for (int back = 1; back <= 3; back++) {
      LocalDate day = currentMonth().minusMonths(back).atDay(10);
      transaction(shopping, "7000", shoppingTrips[back - 1], day);
      transaction(bills, "649", "NETFLIX #" + back, currentMonth().minusMonths(back).atDay(5));
    }
    transaction(salary, "90000", "Salary", today());
    transaction(food, "9500", "Groceries and dining", today());
    transaction(shopping, "10500", "Festive shopping", today());
  }

  @Test
  @DisplayName("summary: income, expense, savings rate, previous month and a run-rate forecast")
  void summary() throws Exception {
    api.get(token, ANALYTICS + "/summary")
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.month").value(currentMonth().toString()))
      .andExpect(jsonPath("$.data.income").value(90000.0))
      .andExpect(jsonPath("$.data.expense").value(20000.0))
      .andExpect(jsonPath("$.data.savings").value(70000.0))
      .andExpect(jsonPath("$.data.savingsRate").value(77.8))
      .andExpect(jsonPath("$.data.previousMonthExpense").value(7649.0))
      .andExpect(jsonPath("$.data.forecast.daysElapsed").value(today().getDayOfMonth()))
      .andExpect(jsonPath("$.data.forecast.daysInMonth").value(currentMonth().lengthOfMonth()));
  }

  @Test
  @DisplayName("category breakdown is sorted by amount with share of total")
  void categoryBreakdown() throws Exception {
    api.get(token, ANALYTICS + "/categories")
      .andExpect(jsonPath("$.data[*].categoryName").value(contains("Shopping", "Food")))
      .andExpect(jsonPath("$.data[0].total").value(10500.0))
      .andExpect(jsonPath("$.data[0].percentOfTotal").value(52.5));
  }

  @Test
  @DisplayName("comparison: Shopping is +50% vs its 3-month average and vs last month")
  void comparison() throws Exception {
    api.get(token, ANALYTICS + "/comparison")
      .andExpect(jsonPath("$.data[?(@.categoryName == 'Shopping')].threeMonthAverage").value(hasItem(7000.0)))
      .andExpect(jsonPath("$.data[?(@.categoryName == 'Shopping')].deviationVsAveragePercent").value(hasItem(50.0)))
      .andExpect(jsonPath("$.data[?(@.categoryName == 'Shopping')].changeVsPreviousPercent").value(hasItem(50.0)));
  }

  @Test
  @DisplayName("anomalies: the Shopping spike is detected from real data")
  void anomalies() throws Exception {
    api.get(token, ANALYTICS + "/anomalies")
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data[?(@.type == 'SPENDING_SPIKE')].categoryName").value(hasItem("Shopping")));
  }

  @Test
  @DisplayName("recurring: the monthly Netflix bill is found despite varying descriptions")
  void recurring() throws Exception {
    api.get(token, ANALYTICS + "/recurring")
      .andExpect(jsonPath("$.data.length()").value(1))
      .andExpect(jsonPath("$.data[0].categoryName").value("Bills"))
      .andExpect(jsonPath("$.data[0].typicalAmount").value(649.0))
      .andExpect(jsonPath("$.data[0].occurrences").value(3));
  }

  @Test
  @DisplayName("trends: one row per month, oldest first, ending with the current month; range is validated")
  void trends() throws Exception {
    api.get(token, ANALYTICS + "/trends?months=4")
      .andExpect(jsonPath("$.data.length()").value(4))
      .andExpect(jsonPath("$.data[0].month").value(currentMonth().minusMonths(3).toString()))
      .andExpect(jsonPath("$.data[0].expense").value(7649.0))
      .andExpect(jsonPath("$.data[0].savingsRate").value(nullValue()))
      .andExpect(jsonPath("$.data[3].income").value(90000.0));
    api.get(token, ANALYTICS + "/trends?months=30").andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("cashflow: one row per elapsed day; the running balance ends at the month's net")
  void cashflow() throws Exception {
    int days = today().getDayOfMonth();
    api.get(token, ANALYTICS + "/cashflow")
      .andExpect(jsonPath("$.data.length()").value(days))
      .andExpect(jsonPath("$.data[" + (days - 1) + "].runningBalance").value(70000.0));
  }

  @Test
  @DisplayName("overview: income/expense/balance for an arbitrary range (dashboard date picker)")
  void overview() throws Exception {
    api.get(token, ANALYTICS + "/overview?from=" + currentMonth().minusMonths(3).atDay(1) + "&to=" + today())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.income").value(90000.0))
      .andExpect(jsonPath("$.data.expense").value(42947.0))
      .andExpect(jsonPath("$.data.balance").value(47053.0));
    api.get(token, ANALYTICS + "/overview?from=2026-05-10&to=2026-05-01")
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors[0].code").value("BW-2003"));
  }

  @Test
  @DisplayName("yearly: 12 zero-filled months for the year; periods lists years with data")
  void yearlyAndPeriods() throws Exception {
    int year = today().getYear();
    api.get(token, ANALYTICS + "/yearly?year=" + year)
      .andExpect(jsonPath("$.data.length()").value(12))
      .andExpect(jsonPath("$.data[0].month").value(year + "-01"))
      .andExpect(jsonPath("$.data[" + (today().getMonthValue() - 1) + "].income").value(90000.0));
    api.get(token, ANALYTICS + "/yearly?year=1990").andExpect(status().isBadRequest());
    api.get(token, ANALYTICS + "/periods")
      .andExpect(jsonPath("$.data").value(hasItem(year)));
  }

  @Test
  @DisplayName("isolation: another user's analytics see none of this data")
  void isolation() throws Exception {
    String otherToken = api.registerUser();

    api.get(otherToken, ANALYTICS + "/summary")
      .andExpect(jsonPath("$.data.income").value(0.0))
      .andExpect(jsonPath("$.data.expense").value(0.0));
    api.get(otherToken, ANALYTICS + "/comparison").andExpect(jsonPath("$.data.length()").value(0));
    api.get(otherToken, ANALYTICS + "/recurring").andExpect(jsonPath("$.data.length()").value(0));
    api.get(otherToken, ANALYTICS + "/anomalies").andExpect(jsonPath("$.data.length()").value(0));
    api.get(otherToken, ANALYTICS + "/overview").andExpect(jsonPath("$.data.income").value(0.0));
    api.get(otherToken, ANALYTICS + "/periods").andExpect(jsonPath("$.data.length()").value(1));
  }

  private void transaction(String categoryId, String amount, String description, LocalDate date) throws Exception {
    api.create(token, "/api/v1/transactions", """
      {"amount": %s, "categoryId": %s, "description": "%s", "transactionDate": "%s"}
      """.formatted(amount, categoryId, description, date));
  }
}
