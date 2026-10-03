package com.budwiser.common.exception;

import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.constant.ExceptionType;
import org.springframework.http.HttpStatus;

public class AuthException extends BudWiserException {

  public AuthException(ErrorCode errorCode) {
    super(errorCode, ExceptionType.UNAUTHORIZED, null);
  }

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.UNAUTHORIZED;
  }
}
