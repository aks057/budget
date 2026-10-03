package com.budwiser.common.exception;

import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.constant.ExceptionType;
import org.springframework.http.HttpStatus;

/**
 * Every configured LLM provider failed (timeout, rate limit, 5xx) or none is configured. The rest of the app keeps
 * working; only the assistant degrades (PRD §30).
 */
public class AiUnavailableException extends BudWiserException {

  public AiUnavailableException() {
    super(ErrorCode.AI_UNAVAILABLE, ExceptionType.UPSTREAM_FAILURE, null);
  }

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.SERVICE_UNAVAILABLE;
  }
}
