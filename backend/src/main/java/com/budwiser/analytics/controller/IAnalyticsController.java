package com.budwiser.analytics.controller;

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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only financial analytics (PRD §12, §26). {@code month} is yyyy-MM and defaults to the current month.
 */
@RestController
@RequestMapping("/api/v1/analytics")
public interface IAnalyticsController {

  @GetMapping("/summary")
  Response<MonthlySummaryDto> summary(@RequestParam(required = false) YearMonth month);

  @GetMapping("/overview")
  Response<OverviewDto> overview(
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to);

  @GetMapping("/yearly")
  Response<List<MonthlyTrendDto>> yearly(
    @RequestParam @Min(IAnalyticsService.MIN_YEAR) @Max(IAnalyticsService.MAX_YEAR) int year);

  @GetMapping("/periods")
  Response<List<Integer>> periods();

  @GetMapping("/categories")
  Response<List<CategorySpendingDto>> categories(
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
    @RequestParam(required = false) TransactionType type);

  @GetMapping("/comparison")
  Response<List<CategoryComparisonDto>> comparison(@RequestParam(required = false) YearMonth month);

  @GetMapping("/trends")
  Response<List<MonthlyTrendDto>> trends(
    @RequestParam(defaultValue = "6") @Min(1) @Max(IAnalyticsService.MAX_TREND_MONTHS) int months);

  @GetMapping("/cashflow")
  Response<List<DailyCashflowDto>> cashflow(@RequestParam(required = false) YearMonth month);

  @GetMapping("/recurring")
  Response<List<RecurringExpenseDto>> recurring();

  @GetMapping("/anomalies")
  Response<List<AnomalyDto>> anomalies(@RequestParam(required = false) YearMonth month);
}
