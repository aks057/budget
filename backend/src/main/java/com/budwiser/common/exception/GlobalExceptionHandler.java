package com.budwiser.common.exception;

import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.constant.ExceptionType;
import com.budwiser.common.response.Response;
import com.budwiser.common.response.ResultStatus;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Single place that turns exceptions into {@link Response} envelopes. Also receives Spring Security
 * 401/403s, delegated from the REST entry point and access-denied handler.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BudWiserException.class)
  public ResponseEntity<Response<Void>> handleBudWiser(BudWiserException ex) {
    log.warn("[handleBudWiser] {}: {}", ex.getClass().getSimpleName(), ex.getMessage());
    return build(ex.getHttpStatus(), ex.getMessage(), ex.getErrors());
  }

  /** More specific than BudWiserException, so it wins: adds the standard Retry-After header. */
  @ExceptionHandler(RateLimitExceededException.class)
  public ResponseEntity<Response<Void>> handleRateLimit(RateLimitExceededException ex) {
    ResponseEntity<Response<Void>> response = build(ex.getHttpStatus(), ex.getMessage(), ex.getErrors());
    return ResponseEntity.status(response.getStatusCode())
      .header(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSeconds()))
      .body(response.getBody());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Response<Void>> handleBodyValidation(MethodArgumentNotValidException ex) {
    List<Error> errors = ex.getBindingResult().getFieldErrors().stream()
      .map(fieldError -> Error.builder()
        .type(ExceptionType.VALIDATION_ERROR.name())
        .code(ErrorCode.VALIDATION_FAILED.getCode())
        .message(fieldError.getDefaultMessage())
        .errorInfo(fieldError.getField())
        .build())
      .toList();
    return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED.getDescription(), errors);
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<Response<Void>> handleParameterValidation(HandlerMethodValidationException ex) {
    return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED, ExceptionType.VALIDATION_ERROR);
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MissingServletRequestParameterException.class,
    MethodArgumentTypeMismatchException.class
  })
  public ResponseEntity<Response<Void>> handleMalformed(Exception ex) {
    log.info("[handleMalformed] {}", ex.getClass().getSimpleName());
    return build(HttpStatus.BAD_REQUEST, ErrorCode.MALFORMED_REQUEST, ExceptionType.BAD_REQUEST);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Response<Void>> handleAuthentication(AuthenticationException ex) {
    return build(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED, ExceptionType.UNAUTHORIZED);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Response<Void>> handleAccessDenied(AccessDeniedException ex) {
    return build(HttpStatus.FORBIDDEN, ErrorCode.ACCESS_DENIED, ExceptionType.FORBIDDEN);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Response<Void>> handleNoResource(NoResourceFoundException ex) {
    return build(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, ExceptionType.ENTITY_NOT_FOUND);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<Response<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
    return build(HttpStatus.METHOD_NOT_ALLOWED, ErrorCode.METHOD_NOT_ALLOWED, ExceptionType.BAD_REQUEST);
  }

  @ExceptionHandler(OptimisticLockingFailureException.class)
  public ResponseEntity<Response<Void>> handleOptimisticLock(OptimisticLockingFailureException ex) {
    log.warn("[handleOptimisticLock] concurrent modification detected");
    return build(HttpStatus.CONFLICT, ErrorCode.CONCURRENT_MODIFICATION, ExceptionType.CONFLICT);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Response<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
    log.warn("[handleDataIntegrity] constraint violation");
    return build(HttpStatus.CONFLICT, ErrorCode.DATA_CONFLICT, ExceptionType.CONFLICT);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Response<Void>> handleUnexpected(Exception ex) {
    log.error("[handleUnexpected] unhandled exception", ex);
    return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, ExceptionType.INTERNAL);
  }

  private ResponseEntity<Response<Void>> build(HttpStatus status, ErrorCode errorCode, ExceptionType type) {
    return build(status, errorCode.getDescription(), List.of(Error.of(errorCode, type)));
  }

  private ResponseEntity<Response<Void>> build(HttpStatus status, String message, List<Error> errors) {
    return ResponseEntity.status(status).body(Response.<Void>builder()
      .status(ResultStatus.ERROR)
      .message(message)
      .errors(errors)
      .build());
  }
}
