package com.budwiser.common.exception;

import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.constant.ExceptionType;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RateLimitExceededException extends BudWiserException {
  private final long retryAfterSeconds;

  public RateLimitExceededException(long retryAfterSeconds) {
    super(ErrorCode.RATE_LIMITED, ExceptionType.RATE_LIMITED, null);
    this.retryAfterSeconds = retryAfterSeconds;
  }

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.TOO_MANY_REQUESTS;
  }
}
