package com.budwiser.common.exception;

import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.constant.ExceptionType;
import java.util.List;
import org.springframework.http.HttpStatus;

public class ValidationException extends BudWiserException {

  public ValidationException(String message, List<Error> errors) {
    super(message, errors);
  }

  public ValidationException(ErrorCode errorCode) {
    super(errorCode, ExceptionType.VALIDATION_ERROR, null);
  }

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.BAD_REQUEST;
  }
}
