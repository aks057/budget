package com.budwiser.common.response;

import com.budwiser.common.exception.Error;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * Uniform envelope for every REST response (success and error).
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {
  @Builder.Default
  private final long timestamp = Instant.now().toEpochMilli();

  @Builder.Default
  private final ResultStatus status = ResultStatus.SUCCESS;

  private final String message;
  private final T data;
  private final List<Error> errors;
  /** Present only on paginated list responses. */
  private final PageMeta page;
}
