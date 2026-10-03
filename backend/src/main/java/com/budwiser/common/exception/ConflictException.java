package com.budwiser.common.exception;

import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.constant.ExceptionType;
import org.springframework.http.HttpStatus;

public class ConflictException extends BudWiserException {

  public ConflictException(ErrorCode errorCode) {
    super(errorCode, ExceptionType.CONFLICT, null);
  }

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.CONFLICT;
  }
}
