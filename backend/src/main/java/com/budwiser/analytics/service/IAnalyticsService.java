package com.budwiser.analytics.service;

import com.budwiser.analytics.dto.AnomalyDto;
import com.budwiser.analytics.dto.CategoryComparisonDto;
import com.budwiser.analytics.dto.CategorySpendingDto;
import com.budwiser.analytics.dto.DailyCashflowDto;
import com.budwiser.analytics.dto.MonthlySummaryDto;
import com.budwiser.analytics.dto.MonthlyTrendDto;
import com.budwiser.analytics.dto.OverviewDto;
import com.budwiser.analytics.dto.RecurringExpenseDto;
import com.budwiser.common.constant.TransactionType;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * The financial engine's application service. Takes an explicit userId because it is shared by the REST API,
 * the AI agent's tools and the insights scheduler (which runs without a request). The userId always originates
 * from the authenticated principal or the scheduler — never from a client or LLM argument.
 * A null month means the current month in the business timezone.
 */
public interface IAnalyticsService {

  int MAX_TREND_MONTHS = 24;

  int MIN_YEAR = 2000;
  int MAX_YEAR = 2100;

  MonthlySummaryDto summary(Long userId, YearMonth month);

  /** Income/expense/balance for any range (defaults to the current month; max 366 days). */
  OverviewDto overview(Long userId, LocalDate from, LocalDate to);

  /** January..December of {@code year}, zero-filled. */
  List<MonthlyTrendDto> yearly(Long userId, int year);

  /** Years that have at least one transaction, ascending; the current year when there are none. */
  List<Integer> periods(Long userId);

  List<CategorySpendingDto> categoryBreakdown(Long userId, LocalDate from, LocalDate to, TransactionType type);

  List<CategoryComparisonDto> comparison(Long userId, YearMonth month);

  /** The last {@code months} months, oldest first, ending with the current month. */
  List<MonthlyTrendDto> trends(Long userId, int months);

  /** One row per day of the month up to today (or the whole month if it is in the past). */
  List<DailyCashflowDto> cashflow(Long userId, YearMonth month);

  List<RecurringExpenseDto> recurringExpenses(Long userId);

  List<AnomalyDto> anomalies(Long userId, YearMonth month);
}
