package com.budwiser.transaction;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.budwiser.AbstractIntegrationTest;
import com.budwiser.support.TestApi;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class TransactionIT extends AbstractIntegrationTest {
  private static final String TRANSACTIONS = "/api/v1/transactions";

  @Autowired
  private MockMvc mockMvc;
  private TestApi api;
  private String token;
  private String foodId;
  private String salaryId;

  @BeforeEach
  void setUp() throws Exception {
    api = new TestApi(mockMvc);
    token = api.registerUser();
    foodId = api.categoryId(token, "Food");
    salaryId = api.categoryId(token, "Salary");
  }

  @Nested
  @DisplayName("create")
  class Create {

    @Test
    @DisplayName("type is derived from the category; ids are strings; category name is resolved")
    void createExpense() throws Exception {
      api.post(token, TRANSACTIONS, body("1299.50", foodId, "Swiggy", today()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").isString())
        .andExpect(jsonPath("$.data.type").value("EXPENSE"))
        .andExpect(jsonPath("$.data.amount").value(1299.5))
        .andExpect(jsonPath("$.data.categoryId").value(foodId))
        .andExpect(jsonPath("$.data.categoryName").value("Food"))
        .andExpect(jsonPath("$.data.description").value("Swiggy"));
    }

    @Test
    @DisplayName("rejects zero/negative amounts and more than 2 decimals")
    void invalidAmounts() throws Exception {
      api.post(token, TRANSACTIONS, body("0", foodId, "x", today())).andExpect(status().isBadRequest());
      api.post(token, TRANSACTIONS, body("-5", foodId, "x", today())).andExpect(status().isBadRequest());
      api.post(token, TRANSACTIONS, body("10.999", foodId, "x", today()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].errorInfo").value("amount"));
    }

    @Test
    @DisplayName("rejects dates more than a day in the future")
    void futureDate() throws Exception {
      api.post(token, TRANSACTIONS, body("100", foodId, "x", today().plusDays(3)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].code").value("BW-2002"));
    }

    @Test
    @DisplayName("an unknown category id is a 404")
    void unknownCategory() throws Exception {
      api.post(token, TRANSACTIONS, body("100", "999999999", "x", today()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors[0].code").value("BW-2101"));
    }
  }

  @Nested
  @DisplayName("list")
  class ListTransactions {

    @Test
    @DisplayName("paginates newest-first with page metadata")
    void pagination() throws Exception {
      LocalDate today = today();
      api.create(token, TRANSACTIONS, body("10", foodId, "oldest", today.minusDays(2)));
      api.create(token, TRANSACTIONS, body("20", foodId, "middle", today.minusDays(1)));
      api.create(token, TRANSACTIONS, body("30", foodId, "newest", today));
      String range = "?from=" + today.minusDays(10) + "&to=" + today;

      api.get(token, TRANSACTIONS + range + "&size=2&page=0")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[*].description").value(contains("newest", "middle")))
        .andExpect(jsonPath("$.page.totalElements").value(3))
        .andExpect(jsonPath("$.page.totalPages").value(2));
      api.get(token, TRANSACTIONS + range + "&size=2&page=1")
        .andExpect(jsonPath("$.data[*].description").value(contains("oldest")));
    }

    @Test
    @DisplayName("filters by type and by category")
    void filters() throws Exception {
      LocalDate today = today();
      api.create(token, TRANSACTIONS, body("500", foodId, "Dinner", today));
      api.create(token, TRANSACTIONS, body("90000", salaryId, "October salary", today));
      String range = "?from=" + today.minusDays(1) + "&to=" + today;

      api.get(token, TRANSACTIONS + range + "&type=INCOME")
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[*].type").value(everyItem(is("INCOME"))));
      api.get(token, TRANSACTIONS + range + "&categoryId=" + foodId)
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].description").value("Dinner"));
    }

    @Test
    @DisplayName("rejects an inverted range and oversized pages")
    void invalidQuery() throws Exception {
      api.get(token, TRANSACTIONS + "?from=2026-05-10&to=2026-05-01")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].code").value("BW-2003"));
      api.get(token, TRANSACTIONS + "?size=500").andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("update and delete")
  class UpdateDelete {

    @Test
    @DisplayName("moving a transaction to an income category re-derives its type")
    void updateReclassifies() throws Exception {
      String id = api.create(token, TRANSACTIONS, body("1000", foodId, "Refund?", today()));

      api.put(token, TRANSACTIONS + "/" + id, body("1000", salaryId, "Bonus", today()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.type").value("INCOME"))
        .andExpect(jsonPath("$.data.categoryName").value("Salary"));
    }

    @Test
    @DisplayName("deleted transactions are gone")
    void delete() throws Exception {
      String id = api.create(token, TRANSACTIONS, body("75", foodId, "Chai", today()));

      api.delete(token, TRANSACTIONS + "/" + id).andExpect(status().isOk());
      api.get(token, TRANSACTIONS + "/" + id)
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors[0].code").value("BW-2001"));
    }
  }

  @Test
  @DisplayName("isolation: another user gets 404 on my transaction and cannot use my categories")
  void isolation() throws Exception {
    String id = api.create(token, TRANSACTIONS, body("4999", foodId, "Private dinner", today()));
    String otherToken = api.registerUser();

    api.get(otherToken, TRANSACTIONS + "/" + id).andExpect(status().isNotFound());
    api.put(otherToken, TRANSACTIONS + "/" + id, body("1", api.categoryId(otherToken, "Food"), "hack", today()))
      .andExpect(status().isNotFound());
    api.delete(otherToken, TRANSACTIONS + "/" + id).andExpect(status().isNotFound());
    api.post(otherToken, TRANSACTIONS, body("1", foodId, "using your category", today()))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.errors[0].code").value("BW-2101"));
    api.get(otherToken, TRANSACTIONS)
      .andExpect(jsonPath("$.page.totalElements").value(0));

    // And the owner's data is untouched.
    api.get(token, TRANSACTIONS + "/" + id).andExpect(jsonPath("$.data.amount").value(4999));
  }

  private static String body(String amount, String categoryId, String description, LocalDate date) {
    return """
      {"amount": %s, "categoryId": %s, "description": "%s", "transactionDate": "%s"}
      """.formatted(amount, categoryId, description, date);
  }
}
