package com.budwiser.budget;

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

class BudgetIT extends AbstractIntegrationTest {
  private static final String BUDGETS = "/api/v1/budgets";
  private static final String TRANSACTIONS = "/api/v1/transactions";

  @Autowired
  private MockMvc mockMvc;
  private TestApi api;
  private String token;
  private String shoppingId;

  @BeforeEach
  void setUp() throws Exception {
    api = new TestApi(mockMvc);
    token = api.registerUser();
    shoppingId = api.categoryId(token, "Shopping");
  }

  @Test
  @DisplayName("PRD example: ₹8,400 of a ₹10,000 shopping budget → 84%, ₹1,600 left, WARNING")
  void statusFromRealTransactions() throws Exception {
    api.create(token, BUDGETS, budget(shoppingId, "10000"));
    api.create(token, TRANSACTIONS, expense(shoppingId, "5000", today()));
    api.create(token, TRANSACTIONS, expense(shoppingId, "3400", today()));
    // Spending in another category must not count.
    api.create(token, TRANSACTIONS, expense(api.categoryId(token, "Food"), "999", today()));

    api.get(token, BUDGETS)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.length()").value(1))
      .andExpect(jsonPath("$.data[0].categoryName").value("Shopping"))
      .andExpect(jsonPath("$.data[0].spent").value(8400.0))
      .andExpect(jsonPath("$.data[0].remaining").value(1600.0))
      .andExpect(jsonPath("$.data[0].percentUsed").value(84.0))
      .andExpect(jsonPath("$.data[0].level").value("WARNING"))
      .andExpect(jsonPath("$.data[0].month").value(currentMonth().toString()));
  }

  @Test
  @DisplayName("status is computed per month: last month's view does not include this month's spending")
  void perMonth() throws Exception {
    api.create(token, BUDGETS, budget(shoppingId, "2000"));
    api.create(token, TRANSACTIONS, expense(shoppingId, "2500", today()));

    api.get(token, BUDGETS).andExpect(jsonPath("$.data[0].level").value("EXCEEDED"));
    api.get(token, BUDGETS + "?month=" + currentMonth().minusMonths(1))
      .andExpect(jsonPath("$.data[0].spent").value(0.0))
      .andExpect(jsonPath("$.data[0].level").value("ON_TRACK"));
  }

  @Test
  @DisplayName("one budget per category; income categories cannot be budgeted")
  void rules() throws Exception {
    api.create(token, BUDGETS, budget(shoppingId, "7000"));

    api.post(token, BUDGETS, budget(shoppingId, "8000"))
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.errors[0].code").value("BW-3002"));
    api.post(token, BUDGETS, budget(api.categoryId(token, "Salary"), "8000"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors[0].code").value("BW-3003"));
  }

  @Test
  @DisplayName("update changes the limit; delete removes the budget")
  void updateAndDelete() throws Exception {
    String id = api.create(token, BUDGETS, budget(shoppingId, "7000"));

    api.put(token, BUDGETS + "/" + id, """
        {"amount": 9000}
        """)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.amount").value(9000.0));
    api.delete(token, BUDGETS + "/" + id).andExpect(status().isOk());
    api.get(token, BUDGETS).andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  @DisplayName("isolation: another user cannot see or modify my budgets")
  void isolation() throws Exception {
    String id = api.create(token, BUDGETS, budget(shoppingId, "7000"));
    String otherToken = api.registerUser();

    api.get(otherToken, BUDGETS).andExpect(jsonPath("$.data.length()").value(0));
    api.put(otherToken, BUDGETS + "/" + id, """
        {"amount": 1}
        """)
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.errors[0].code").value("BW-3001"));
    api.delete(otherToken, BUDGETS + "/" + id).andExpect(status().isNotFound());
    api.post(otherToken, BUDGETS, budget(shoppingId, "1")).andExpect(status().isNotFound());
  }

  private static String budget(String categoryId, String amount) {
    return """
      {"categoryId": %s, "amount": %s}
      """.formatted(categoryId, amount);
  }

  private static String expense(String categoryId, String amount, LocalDate date) {
    return """
      {"amount": %s, "categoryId": %s, "description": "test", "transactionDate": "%s"}
      """.formatted(amount, categoryId, date);
  }
}
