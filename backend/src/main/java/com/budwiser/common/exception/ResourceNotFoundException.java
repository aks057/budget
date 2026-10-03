package com.budwiser.common.exception;

import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.constant.ExceptionType;
import org.springframework.http.HttpStatus;

/**
 * Entity missing OR owned by another user — both are 404 so other users' ids are never confirmed.
 */
public class ResourceNotFoundException extends BudWiserException {

  public ResourceNotFoundException(Object id, ErrorCode errorCode) {
    super(errorCode, ExceptionType.ENTITY_NOT_FOUND, String.valueOf(id));
  }

  @Override
  public HttpStatus getHttpStatus() {
    return HttpStatus.NOT_FOUND;
  }
}
