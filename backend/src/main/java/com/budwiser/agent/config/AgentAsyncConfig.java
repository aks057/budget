package com.budwiser.agent.config;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Bounded pool for streaming agent turns. The decorator carries the caller's SecurityContext and MDC (request id,
 * user id) onto the worker thread — without it, tools would run with no authenticated user and logs would lose
 * their correlation ids.
 */
@Configuration
public class AgentAsyncConfig {
  public static final String AGENT_EXECUTOR = "agentExecutor";
  private static final int CORE_THREADS = 4;
  private static final int MAX_THREADS = 16;
  private static final int QUEUE_CAPACITY = 32;

  @Bean(name = AGENT_EXECUTOR)
  public ThreadPoolTaskExecutor agentExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(CORE_THREADS);
    executor.setMaxPoolSize(MAX_THREADS);
    executor.setQueueCapacity(QUEUE_CAPACITY);
    executor.setThreadNamePrefix("agent-");
    executor.setTaskDecorator(contextPropagatingDecorator());
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.initialize();
    return executor;
  }

  static TaskDecorator contextPropagatingDecorator() {
    return runnable -> {
      SecurityContext callerContext = SecurityContextHolder.createEmptyContext();
      callerContext.setAuthentication(SecurityContextHolder.getContext().getAuthentication());
      Map<String, String> callerMdc = MDC.getCopyOfContextMap();
      return () -> {
        SecurityContextHolder.setContext(callerContext);
        if (callerMdc != null) {
          MDC.setContextMap(callerMdc);
        }
        try {
          runnable.run();
        } finally {
          SecurityContextHolder.clearContext();
          MDC.clear();
        }
      };
    };
  }
}
