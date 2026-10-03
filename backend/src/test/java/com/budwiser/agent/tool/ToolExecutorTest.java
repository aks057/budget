package com.budwiser.agent.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budwiser.agent.constant.ActionStatus;
import com.budwiser.agent.entity.AgentAction;
import com.budwiser.agent.llm.LlmToolCall;
import com.budwiser.agent.repository.IAgentActionRepository;
import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.exception.ConflictException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

class ToolExecutorTest {
  private static final Instant NOW = Instant.parse("2026-10-02T10:00:00Z");
  private static final ToolContext CONTEXT = new ToolContext(1L, 10L);

  private final JsonMapper jsonMapper = JsonMapper.builder().build();
  private final AtomicBoolean writeExecuted = new AtomicBoolean();
  private ValidatorFactory validatorFactory;
  private IAgentActionRepository mockRepository;
  private ToolExecutor executor;

  public record DoubleArgs(@NotNull @Min(1) Integer n) {}

  @BeforeEach
  void setUp() {
    validatorFactory = Validation.buildDefaultValidatorFactory();
    mockRepository = mock(IAgentActionRepository.class);
    when(mockRepository.save(any())).thenAnswer(invocation -> {
      AgentAction action = invocation.getArgument(0);
      action.setId(77L);
      return action;
    });
    ToolRegistry registry = new ToolRegistry(List.of(
      readTool("double_it", args -> args.n() * 2),
      readTool("conflicting", args -> {
        throw new ConflictException(ErrorCode.BUDGET_ALREADY_EXISTS);
      }),
      readTool("crashing", args -> {
        throw new IllegalStateException("db exploded: secret detail");
      }),
      writeTool()), jsonMapper);
    executor = new ToolExecutor(registry, mockRepository, validatorFactory.getValidator(), jsonMapper,
      Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @AfterEach
  void tearDown() {
    validatorFactory.close();
  }

  @Test
  @DisplayName("valid read call executes and is audited as SUCCESS")
  void success() {
    ToolExecution actual = executor.execute(call("double_it", "{\"n\": 21}"), CONTEXT);

    assertThat(actual.result().status()).isEqualTo(ToolStatus.SUCCESS);
    assertThat(actual.result().data()).isEqualTo(42);
    assertThat(savedAction().getStatus()).isEqualTo(ActionStatus.SUCCESS);
    assertThat(savedAction().getUserId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("unknown tool, malformed JSON and constraint violations are REJECTED with a reason")
  void rejections() {
    assertThat(executor.execute(call("drop_tables", "{}"), CONTEXT).result())
      .satisfies(result -> {
        assertThat(result.status()).isEqualTo(ToolStatus.REJECTED);
        assertThat(result.message()).contains("Unknown tool");
      });
    assertThat(executor.execute(call("double_it", "{not json"), CONTEXT).result().status())
      .isEqualTo(ToolStatus.REJECTED);
    assertThat(executor.execute(call("double_it", "{\"n\": 0}"), CONTEXT).result().message())
      .contains("n must be greater than or equal to 1");
  }

  @Test
  @DisplayName("business-rule exceptions become REJECTED (the model can self-correct)")
  void businessRule() {
    ToolResult actual = executor.execute(call("conflicting", "{\"n\": 1}"), CONTEXT).result();

    assertThat(actual.status()).isEqualTo(ToolStatus.REJECTED);
    assertThat(actual.message()).isEqualTo(ErrorCode.BUDGET_ALREADY_EXISTS.getDescription());
  }

  @Test
  @DisplayName("unexpected exceptions become a generic ERROR — internal details never reach the model")
  void unexpectedFailure() {
    ToolResult actual = executor.execute(call("crashing", "{\"n\": 1}"), CONTEXT).result();

    assertThat(actual.status()).isEqualTo(ToolStatus.ERROR);
    assertThat(actual.message()).doesNotContain("secret");
  }

  @Test
  @DisplayName("write tools are NOT executed: they are queued PENDING_CONFIRMATION with a 15-minute expiry")
  void writeIsQueued() {
    ToolResult actual = executor.execute(call("set_limit", "{\"n\": 5000}"), CONTEXT).result();

    assertThat(actual.status()).isEqualTo(ToolStatus.PENDING_CONFIRMATION);
    assertThat(actual.actionId()).isEqualTo("77");
    assertThat(actual.message()).isEqualTo("Set limit to 5000");
    assertThat(writeExecuted).isFalse();
    AgentAction saved = savedAction();
    assertThat(saved.getStatus()).isEqualTo(ActionStatus.PENDING_CONFIRMATION);
    assertThat(saved.getExpiresAt()).isEqualTo(NOW.plusSeconds(15 * 60).toEpochMilli());
    assertThat(saved.getArguments()).isEqualTo("{\"n\": 5000}");
  }

  @Test
  @DisplayName("results sent back to the model are size-capped")
  void truncation() {
    String serialized = executor.serializeForModel(ToolResult.success("x".repeat(20_000)));

    assertThat(serialized).hasSizeLessThanOrEqualTo(8_000).endsWith("...(truncated)");
  }

  @Test
  @DisplayName("the registry refuses any tool whose schema exposes a user id")
  void registryRejectsUserIdInSchema() {
    FinancialTool<DoubleArgs> leaky = new TestTool("leaky", false, args -> null) {
      @Override
      public String parametersSchema() {
        return "{\"type\": \"object\", \"properties\": {\"user_id\": {\"type\": \"integer\"}}}";
      }
    };

    assertThatThrownBy(() -> new ToolRegistry(List.of(leaky), jsonMapper))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("user id");
  }

  private AgentAction savedAction() {
    ArgumentCaptor<AgentAction> captor = ArgumentCaptor.forClass(AgentAction.class);
    verify(mockRepository, atLeastOnce()).save(captor.capture());
    return captor.getValue();
  }

  private static LlmToolCall call(String name, String arguments) {
    return new LlmToolCall("call_1", name, arguments);
  }

  private static FinancialTool<DoubleArgs> readTool(String name, Function<DoubleArgs, Object> body) {
    return new TestTool(name, false, body);
  }

  private FinancialTool<DoubleArgs> writeTool() {
    return new TestTool("set_limit", true, args -> {
      writeExecuted.set(true);
      return null;
    });
  }

  private static class TestTool implements FinancialTool<DoubleArgs> {
    private final String name;
    private final boolean write;
    private final Function<DoubleArgs, Object> body;

    TestTool(String name, boolean write, Function<DoubleArgs, Object> body) {
      this.name = name;
      this.write = write;
      this.body = body;
    }

    @Override
    public String name() {
      return name;
    }

    @Override
    public String description() {
      return name;
    }

    @Override
    public String parametersSchema() {
      return "{\"type\": \"object\", \"properties\": {\"n\": {\"type\": \"integer\"}}}";
    }

    @Override
    public Class<DoubleArgs> argumentsType() {
      return DoubleArgs.class;
    }

    @Override
    public boolean requiresConfirmation() {
      return write;
    }

    @Override
    public Object execute(DoubleArgs arguments, ToolContext context) {
      return body.apply(arguments);
    }

    @Override
    public String describe(DoubleArgs arguments, ToolContext context) {
      return "Set limit to " + arguments.n();
    }
  }
}
