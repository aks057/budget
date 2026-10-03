package com.budwiser.common.exception;

import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.constant.ExceptionType;
import java.util.List;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base of all business exceptions. Each subclass owns its HTTP status, so the global handler stays a single mapping.
 */
@Getter
public abstract class BudWiserException extends RuntimeException {
  private final transient List<Error> errors;

  protected BudWiserException(String message, List<Error> errors) {
    super(message);
    this.errors = List.copyOf(errors);
  }

  protected BudWiserException(ErrorCode errorCode, ExceptionType type, String id) {
    this(errorCode.getDescription(), List.of(Error.builder()
      .id(id)
      .type(type.name())
      .code(errorCode.getCode())
      .message(errorCode.getDescription())
      .build()));
  }

  public abstract HttpStatus getHttpStatus();
}
