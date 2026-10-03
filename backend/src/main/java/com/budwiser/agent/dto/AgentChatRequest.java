package com.budwiser.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentChatRequest {

  /** Omit to start a new conversation. */
  private Long conversationId;

  @NotBlank(message = "Message is required")
  @Size(max = 2000, message = "Message must be at most 2000 characters")
  private String message;
}
