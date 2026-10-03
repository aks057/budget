package com.budwiser.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.budwiser.AbstractIntegrationTest;
import com.budwiser.agent.llm.LlmMessage;
import com.budwiser.common.exception.AiUnavailableException;
import com.budwiser.support.FakeLlmClient;
import com.budwiser.support.TestApi;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/**
 * End-to-end agent behaviour with a scripted model: real HTTP, real tools, real Postgres.
 */
class AgentIT extends AbstractIntegrationTest {
  private static final String CHAT = "/api/v1/agent/chat";
  private static final String ACTIONS = "/api/v1/agent/actions/";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private FakeLlmClient fakeLlm;
  private TestApi api;
  private String token;

  @BeforeEach
  void setUp() throws Exception {
    fakeLlm.reset();
    api = new TestApi(mockMvc);
    token = api.registerUser();
  }

  @Nested
  @DisplayName("read tools")
  class ReadTools {

    @Test
    @DisplayName("numbers reach the model from the backend: the tool result carries the real summary")
    void groundedAnswer() throws Exception {
      api.create(token, "/api/v1/transactions", """
        {"amount": 50000, "categoryId": %s, "description": "Salary", "transactionDate": "%s"}
        """.formatted(api.categoryId(token, "Salary"), today()));
      fakeLlm.thenCallTool("get_monthly_summary", "{}").thenReply("You earned ₹50,000 this month.");

      api.post(token, CHAT, chat("How much did I earn this month?"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.reply").value("You earned ₹50,000 this month."))
        .andExpect(jsonPath("$.data.conversationId").isString())
        .andExpect(jsonPath("$.data.toolCalls[0].name").value("get_monthly_summary"))
        .andExpect(jsonPath("$.data.toolCalls[0].status").value("SUCCESS"))
        .andExpect(jsonPath("$.data.pendingActions").isEmpty());

      assertThat(fakeLlm.lastToolResultSeenAt(1)).contains("\"status\":\"SUCCESS\"").contains("\"income\":50000");
      LlmMessage system = fakeLlm.requests().getFirst().getFirst();
      assertThat(system.role()).isEqualTo(LlmMessage.SYSTEM);
      assertThat(system.content()).contains("Every number you state must come from a tool result").contains("Food");
    }

    @Test
    @DisplayName("an unknown tool or bad arguments are REJECTED and the model gets the reason")
    void rejectedCalls() throws Exception {
      fakeLlm.thenCallTool("delete_everything", "{}")
        .thenCallTool("search_transactions", "{\"categoryName\": \"Crypto\"}")
        .thenReply("Sorry, I can't do that.");

      api.post(token, CHAT, chat("Wipe my data"))
        .andExpect(jsonPath("$.data.toolCalls[0].status").value("REJECTED"))
        .andExpect(jsonPath("$.data.toolCalls[1].status").value("REJECTED"));

      assertThat(fakeLlm.lastToolResultSeenAt(1)).contains("Unknown tool");
      assertThat(fakeLlm.lastToolResultSeenAt(2)).contains("Unknown category 'Crypto'").contains("Groceries");
    }
  }

  @Nested
  @DisplayName("write tools (human in the loop)")
  class WriteTools {

    @Test
    @DisplayName("proposal → nothing written → confirm → written exactly once")
    void confirmFlow() throws Exception {
      fakeLlm.thenCallTool("create_budget", "{\"categoryName\": \"food\", \"amount\": 10000}")
        .thenReply("I've prepared a ₹10,000 Food budget. Please confirm.");

      String actionId = TestApi.read(api.post(token, CHAT, chat("Set my food budget to 10k"))
        .andExpect(jsonPath("$.data.toolCalls[0].status").value("PENDING_CONFIRMATION"))
        .andExpect(jsonPath("$.data.pendingActions[0].toolName").value("create_budget"))
        .andExpect(jsonPath("$.data.pendingActions[0].summary").value("Create a monthly budget of ₹10,000.00 for Food")),
        "$.data.pendingActions[0].actionId");

      // The agent alone cannot change data.
      api.get(token, "/api/v1/budgets").andExpect(jsonPath("$.data.length()").value(0));

      api.post(token, ACTIONS + actionId + "/confirm", "")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("EXECUTED"))
        .andExpect(jsonPath("$.data.data.categoryName").value("Food"));
      api.get(token, "/api/v1/budgets")
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].amount").value(10000.0));

      // Double-click / replay protection.
      api.post(token, ACTIONS + actionId + "/confirm", "")
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errors[0].code").value("BW-5004"));
    }

    @Test
    @DisplayName("pending actions can be re-listed per conversation (UI reload) until confirmed; owner only")
    void pendingActionsListed() throws Exception {
      fakeLlm.thenCallTool("create_budget", "{\"categoryName\": \"Food\", \"amount\": 8000}").thenReply("Confirm?");
      String response = api.post(token, CHAT, chat("food budget 8k")).andReturn().getResponse().getContentAsString();
      String conversationId = JsonPath.read(response, "$.data.conversationId");
      String actionId = JsonPath.read(response, "$.data.pendingActions[0].actionId");
      String pending = "/api/v1/agent/conversations/" + conversationId + "/pending-actions";

      api.get(token, pending)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].actionId").value(actionId))
        .andExpect(jsonPath("$.data[0].toolName").value("create_budget"))
        .andExpect(jsonPath("$.data[0].summary").value("Create a monthly budget of ₹8,000.00 for Food"));
      api.get(api.registerUser(), pending).andExpect(status().isNotFound());

      api.post(token, ACTIONS + actionId + "/confirm", "").andExpect(status().isOk());
      api.get(token, pending).andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("invalid write arguments are rejected before anything is queued")
    void invalidWrite() throws Exception {
      fakeLlm.thenCallTool("create_budget", "{\"categoryName\": \"Food\", \"amount\": -500}").thenReply("That amount is invalid.");

      api.post(token, CHAT, chat("Make my food budget minus 500"))
        .andExpect(jsonPath("$.data.toolCalls[0].status").value("REJECTED"))
        .andExpect(jsonPath("$.data.pendingActions").isEmpty());
      assertThat(fakeLlm.lastToolResultSeenAt(1)).contains("amount");
    }

    @Test
    @DisplayName("a rejected proposal is cancelled and can no longer be confirmed")
    void rejectFlow() throws Exception {
      fakeLlm.thenCallTool("add_transaction", "{\"amount\": 450, \"categoryName\": \"Food\", \"description\": \"Swiggy\"}")
        .thenReply("Prepared. Confirm?");
      String actionId = TestApi.read(api.post(token, CHAT, chat("spent 450 on swiggy")),
        "$.data.pendingActions[0].actionId");

      api.post(token, ACTIONS + actionId + "/reject", "").andExpect(jsonPath("$.data.status").value("CANCELLED"));
      api.post(token, ACTIONS + actionId + "/confirm", "").andExpect(status().isConflict());
      api.get(token, "/api/v1/transactions").andExpect(jsonPath("$.page.totalElements").value(0));
    }

    @Test
    @DisplayName("a confirmed action that breaks a business rule is recorded as FAILED, not a 500")
    void failedExecution() throws Exception {
      fakeLlm.thenCallTool("create_budget", "{\"categoryName\": \"Rent\", \"amount\": 20000}").thenReply("Confirm?");
      String actionId = TestApi.read(api.post(token, CHAT, chat("rent budget 20k")), "$.data.pendingActions[0].actionId");
      // Meanwhile the user creates the same budget manually.
      api.create(token, "/api/v1/budgets", """
        {"categoryId": %s, "amount": 18000}
        """.formatted(api.categoryId(token, "Rent")));

      api.post(token, ACTIONS + actionId + "/confirm", "")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("FAILED"))
        .andExpect(jsonPath("$.data.message").value("A budget already exists for this category"));
    }

    @Test
    @DisplayName("isolation: another user can neither confirm nor reject my pending action")
    void isolation() throws Exception {
      fakeLlm.thenCallTool("create_goal",
        "{\"name\": \"Trip\", \"targetAmount\": 90000, \"targetDate\": \"%s\"}".formatted(today().plusMonths(9)))
        .thenReply("Confirm?");
      String actionId = TestApi.read(api.post(token, CHAT, chat("goal: trip 90k")), "$.data.pendingActions[0].actionId");
      String otherToken = api.registerUser();

      api.post(otherToken, ACTIONS + actionId + "/confirm", "")
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors[0].code").value("BW-5003"));
      api.post(otherToken, ACTIONS + actionId + "/reject", "").andExpect(status().isNotFound());
      api.post(token, ACTIONS + actionId + "/confirm", "").andExpect(jsonPath("$.data.status").value("EXECUTED"));
    }
  }

  @Nested
  @DisplayName("conversations")
  class Conversations {

    @Test
    @DisplayName("follow-up messages replay the conversation history to the model")
    void memory() throws Exception {
      fakeLlm.thenReply("Hi! How can I help?").thenReply("You asked about savings.");
      String conversationId = TestApi.read(api.post(token, CHAT, chat("Hello, I want to save more")),
        "$.data.conversationId");

      api.post(token, CHAT, """
        {"conversationId": %s, "message": "What did I just say?"}
        """.formatted(conversationId));

      List<LlmMessage> secondRequest = fakeLlm.requests().get(1);
      assertThat(secondRequest).extracting(LlmMessage::content)
        .contains("Hello, I want to save more", "Hi! How can I help?", "What did I just say?");
      api.get(token, "/api/v1/agent/conversations/" + conversationId + "/messages")
        .andExpect(jsonPath("$.data.length()").value(4))
        .andExpect(jsonPath("$.data[0].role").value("USER"));
      api.get(token, "/api/v1/agent/conversations")
        .andExpect(jsonPath("$.data[0].title").value("Hello, I want to save more"));
    }

    @Test
    @DisplayName("isolation: another user's conversation id is a 404")
    void conversationIsolation() throws Exception {
      fakeLlm.thenReply("ok");
      String conversationId = TestApi.read(api.post(token, CHAT, chat("private question")), "$.data.conversationId");
      String otherToken = api.registerUser();

      api.post(otherToken, CHAT, """
        {"conversationId": %s, "message": "continue"}
        """.formatted(conversationId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errors[0].code").value("BW-5002"));
      api.get(otherToken, "/api/v1/agent/conversations/" + conversationId + "/messages").andExpect(status().isNotFound());
    }
  }

  @Test
  @DisplayName("when every provider is down the API answers 503 with a friendly message")
  void aiUnavailable() throws Exception {
    fakeLlm.thenFail(new AiUnavailableException());

    api.post(token, CHAT, chat("hello"))
      .andExpect(status().isServiceUnavailable())
      .andExpect(jsonPath("$.errors[0].code").value("BW-5001"))
      .andExpect(jsonPath("$.message").value(containsString("dashboard and transactions still work")));
  }

  @Test
  @DisplayName("the step limit stops a model that keeps calling tools")
  void stepLimit() throws Exception {
    for (int i = 0; i < 10; i++) {
      fakeLlm.thenCallTool("get_goals", "{}");
    }

    api.post(token, CHAT, chat("loop forever"))
      .andExpect(jsonPath("$.data.reply").value(containsString("step limit")))
      .andExpect(jsonPath("$.data.toolCalls.length()").value(6));
  }

  private static String chat(String message) {
    return """
      {"message": "%s"}
      """.formatted(message);
  }
}
