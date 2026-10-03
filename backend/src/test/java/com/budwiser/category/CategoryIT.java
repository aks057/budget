package com.budwiser.category;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.budwiser.AbstractIntegrationTest;
import com.budwiser.category.constant.CategoryConstants;
import com.budwiser.support.TestApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class CategoryIT extends AbstractIntegrationTest {
  private static final String CATEGORIES = "/api/v1/categories";

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
  @DisplayName("new users get the default categories, filterable by type")
  void defaultsSeededOnRegistration() throws Exception {
    long expenseDefaults = CategoryConstants.DEFAULT_CATEGORIES.stream()
      .filter(category -> category.type().name().equals("EXPENSE")).count();

    api.get(token, CATEGORIES)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.length()").value(CategoryConstants.DEFAULT_CATEGORIES.size()));
    api.get(token, CATEGORIES + "?type=EXPENSE")
      .andExpect(jsonPath("$.data.length()").value((int) expenseDefaults))
      .andExpect(jsonPath("$.data[*].type").value(everyItem(is("EXPENSE"))));
  }

  @Test
  @DisplayName("names are unique per type, case-insensitively; the same name is allowed for the other type")
  void uniquenessPerType() throws Exception {
    api.post(token, CATEGORIES, """
        {"name": "  FOOD ", "icon": "🍕", "type": "EXPENSE"}
        """)
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.errors[0].code").value("BW-2102"));

    api.post(token, CATEGORIES, """
        {"name": "Food", "icon": "🍕", "type": "INCOME"}
        """)
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.data.name").value("Food"))
      .andExpect(jsonPath("$.data.type").value("INCOME"));
  }

  @Test
  @DisplayName("rename keeps the type; renaming onto an existing name is a conflict")
  void update() throws Exception {
    String id = api.create(token, CATEGORIES, """
      {"name": "Pets", "icon": "🐶", "type": "EXPENSE"}
      """);

    api.put(token, CATEGORIES + "/" + id, """
        {"name": "Pet care", "icon": "🐾"}
        """)
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.name").value("Pet care"))
      .andExpect(jsonPath("$.data.type").value("EXPENSE"));
    api.put(token, CATEGORIES + "/" + id, """
        {"name": "groceries", "icon": "🐾"}
        """)
      .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("an unused category can be deleted; one with transactions cannot")
  void delete() throws Exception {
    String unused = api.create(token, CATEGORIES, """
      {"name": "Travel", "icon": "✈️", "type": "EXPENSE"}
      """);
    api.delete(token, CATEGORIES + "/" + unused).andExpect(status().isOk());

    String food = api.categoryId(token, "Food");
    api.create(token, "/api/v1/transactions", """
      {"amount": 250, "categoryId": %s, "description": "Lunch", "transactionDate": "%s"}
      """.formatted(food, today()));
    api.delete(token, CATEGORIES + "/" + food)
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.errors[0].code").value("BW-2103"));
  }

  @Test
  @DisplayName("isolation: another user cannot see, rename or delete my categories")
  void isolation() throws Exception {
    String mine = api.create(token, CATEGORIES, """
      {"name": "Side hustle", "icon": "🚀", "type": "INCOME"}
      """);
    String otherToken = api.registerUser();

    api.put(otherToken, CATEGORIES + "/" + mine, """
        {"name": "Stolen", "icon": ""}
        """)
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.errors[0].code").value("BW-2101"));
    api.delete(otherToken, CATEGORIES + "/" + mine).andExpect(status().isNotFound());
    api.get(otherToken, CATEGORIES)
      .andExpect(jsonPath("$.data[?(@.name == 'Side hustle')]").isEmpty());
  }
}
