package com.budwiser.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Machine-readable error codes, grouped by feature:
 * BW-1xxx auth, BW-2xxx transaction/category, BW-3xxx budget, BW-4xxx goal, BW-5xxx agent, BW-9xxx generic.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  INVALID_CREDENTIALS("BW-1001", "Invalid email or password"),
  EMAIL_ALREADY_REGISTERED("BW-1002", "An account with this email already exists"),
  INVALID_REFRESH_TOKEN("BW-1003", "Session expired, please sign in again"),
  REFRESH_TOKEN_REUSED("BW-1004", "Session revoked, please sign in again"),
  UNAUTHORIZED("BW-1005", "Authentication required"),
  ACCESS_DENIED("BW-1006", "Access denied"),
  USER_NOT_FOUND("BW-1007", "User not found"),
  OAUTH_EMAIL_NOT_VERIFIED("BW-1008", "Google account email is not verified"),

  TRANSACTION_NOT_FOUND("BW-2001", "Transaction not found"),
  TRANSACTION_DATE_IN_FUTURE("BW-2002", "Transaction date cannot be in the future"),
  INVALID_DATE_RANGE("BW-2003", "'from' must be on or before 'to'"),
  DATE_RANGE_TOO_LARGE("BW-2004", "Date range cannot exceed 366 days"),
  CATEGORY_NOT_FOUND("BW-2101", "Category not found"),
  CATEGORY_ALREADY_EXISTS("BW-2102", "A category with this name already exists"),
  CATEGORY_IN_USE("BW-2103", "Category has transactions and cannot be deleted"),
  CATEGORY_LIMIT_REACHED("BW-2104", "Category limit reached"),

  BUDGET_NOT_FOUND("BW-3001", "Budget not found"),
  BUDGET_ALREADY_EXISTS("BW-3002", "A budget already exists for this category"),
  BUDGET_CATEGORY_NOT_EXPENSE("BW-3003", "Budgets can only be set on expense categories"),

  GOAL_NOT_FOUND("BW-4001", "Goal not found"),
  GOAL_DATE_IN_PAST("BW-4002", "Goal target date cannot be in the past"),
  GOAL_LIMIT_REACHED("BW-4003", "Goal limit reached"),

  AI_UNAVAILABLE("BW-5001",
    "AI assistant is temporarily unavailable. Your dashboard and transactions still work."),
  CONVERSATION_NOT_FOUND("BW-5002", "Conversation not found"),
  ACTION_NOT_FOUND("BW-5003", "Action not found"),
  ACTION_ALREADY_PROCESSED("BW-5004", "This action was already confirmed, cancelled or expired"),
  ACTION_EXPIRED("BW-5005", "This action expired, please ask the assistant again"),

  INSIGHT_NOT_FOUND("BW-6001", "Insight not found"),

  VALIDATION_FAILED("BW-9001", "Validation failed"),
  MALFORMED_REQUEST("BW-9002", "Malformed request"),
  CONCURRENT_MODIFICATION("BW-9003", "The resource was modified concurrently, please retry"),
  RESOURCE_NOT_FOUND("BW-9004", "Resource not found"),
  METHOD_NOT_ALLOWED("BW-9005", "Method not allowed"),
  DATA_CONFLICT("BW-9006", "Request conflicts with existing data"),
  RATE_LIMITED("BW-9007", "Too many requests, please slow down and try again shortly"),
  INTERNAL_ERROR("BW-9999", "Something went wrong, please try again");

  private final String code;
  private final String description;
}
