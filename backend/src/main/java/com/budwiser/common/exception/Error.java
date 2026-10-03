package com.budwiser.common.exception;

import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.constant.ExceptionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Error {
  private String id;
  private String type;
  private String code;
  private String message;
  private Object errorInfo;

  public static Error of(ErrorCode errorCode, ExceptionType type) {
    return Error.builder()
      .type(type.name())
      .code(errorCode.getCode())
      .message(errorCode.getDescription())
      .build();
  }
}
