package com.budwiser.analytics.controller.impl;

import com.budwiser.analytics.controller.IAnalyticsController;
import com.budwiser.analytics.dto.AnomalyDto;
import com.budwiser.analytics.dto.CategoryComparisonDto;
import com.budwiser.analytics.dto.CategorySpendingDto;
import com.budwiser.analytics.dto.DailyCashflowDto;
import com.budwiser.analytics.dto.MonthlySummaryDto;
import com.budwiser.analytics.dto.MonthlyTrendDto;
import com.budwiser.analytics.dto.OverviewDto;
import com.budwiser.analytics.dto.RecurringExpenseDto;
import com.budwiser.analytics.service.IAnalyticsService;
import com.budwiser.common.constant.TransactionType;
import com.budwiser.common.response.Response;
import com.budwiser.security.service.ICurrentUserProvider;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the caller from the validated JWT and delegates; the service takes an explicit userId so the agent
 * and scheduler can share it.
 */
@Component
@RequiredArgsConstructor
public class AnalyticsController implements IAnalyticsController {
  private final IAnalyticsService analyticsService;
  private final ICurrentUserProvider currentUserProvider;

  @Override
  public Response<MonthlySummaryDto> summary(YearMonth month) {
    return ok(analyticsService.summary(userId(), month));
  }

  @Override
  public Response<OverviewDto> overview(LocalDate from, LocalDate to) {
    return ok(analyticsService.overview(userId(), from, to));
  }

  @Override
  public Response<List<MonthlyTrendDto>> yearly(int year) {
    return ok(analyticsService.yearly(userId(), year));
  }

  @Override
  public Response<List<Integer>> periods() {
    return ok(analyticsService.periods(userId()));
  }

  @Override
  public Response<List<CategorySpendingDto>> categories(LocalDate from, LocalDate to, TransactionType type) {
    return ok(analyticsService.categoryBreakdown(userId(), from, to, type));
  }

  @Override
  public Response<List<CategoryComparisonDto>> comparison(YearMonth month) {
    return ok(analyticsService.comparison(userId(), month));
  }

  @Override
  public Response<List<MonthlyTrendDto>> trends(int months) {
    return ok(analyticsService.trends(userId(), months));
  }

  @Override
  public Response<List<DailyCashflowDto>> cashflow(YearMonth month) {
    return ok(analyticsService.cashflow(userId(), month));
  }

  @Override
  public Response<List<RecurringExpenseDto>> recurring() {
    return ok(analyticsService.recurringExpenses(userId()));
  }

  @Override
  public Response<List<AnomalyDto>> anomalies(YearMonth month) {
    return ok(analyticsService.anomalies(userId(), month));
  }

  private Long userId() {
    return currentUserProvider.getUserId();
  }

  private static <T> Response<T> ok(T data) {
    return Response.<T>builder().data(data).build();
  }
}
