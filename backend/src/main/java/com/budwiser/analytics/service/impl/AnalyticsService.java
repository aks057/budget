package com.budwiser.analytics.service.impl;

import com.budwiser.analytics.dto.AnomalyDto;
import com.budwiser.analytics.dto.CategoryComparisonDto;
import com.budwiser.analytics.dto.CategorySpendingDto;
import com.budwiser.analytics.dto.DailyCashflowDto;
import com.budwiser.analytics.dto.MonthlySummaryDto;
import com.budwiser.analytics.dto.MonthlyTrendDto;
import com.budwiser.analytics.dto.OverviewDto;
import com.budwiser.analytics.dto.RecurringExpenseDto;
import com.budwiser.analytics.engine.Anomaly;
import com.budwiser.analytics.engine.AnomalyDetector;
import com.budwiser.analytics.engine.BudgetStatus;
import com.budwiser.analytics.engine.ComparisonCalculator;
import com.budwiser.analytics.engine.ExpenseItem;
import com.budwiser.analytics.engine.ForecastCalculator;
import com.budwiser.analytics.engine.MonthForecast;
import com.budwiser.analytics.engine.MonthlySummary;
import com.budwiser.analytics.engine.RecurringExpenseDetector;
import com.budwiser.analytics.engine.SpendingComparison;
import com.budwiser.analytics.engine.SummaryCalculator;
import com.budwiser.analytics.repository.CategoryTotalView;
import com.budwiser.analytics.repository.DayTypeTotalView;
import com.budwiser.analytics.repository.IAnalyticsRepository;
import com.budwiser.analytics.repository.MonthCategoryTotalView;
import com.budwiser.analytics.repository.MonthTypeTotalView;
import com.budwiser.analytics.service.IAnalyticsService;
import com.budwiser.budget.dto.BudgetDto;
import com.budwiser.budget.service.IBudgetService;
import com.budwiser.category.entity.Category;
import com.budwiser.category.service.ICategoryService;
import com.budwiser.common.constant.TransactionType;
import com.budwiser.common.util.DateRange;
import com.budwiser.common.util.MoneyMath;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService implements IAnalyticsService {
  private static final int RECURRING_LOOKBACK_MONTHS = 6;
  private static final int LARGE_TRANSACTION_LOOKBACK_DAYS = 90;

  private final IAnalyticsRepository analyticsRepository;
  private final ICategoryService categoryService;
  private final IBudgetService budgetService;
  private final Clock clock;

  /** One query covers both the target and the previous month. */
  @Override
  public MonthlySummaryDto summary(Long userId, YearMonth month) {
    YearMonth target = resolve(month);
    Map<YearMonth, Map<TransactionType, BigDecimal>> totals = monthlyTotals(userId, target.minusMonths(1), target);
    Map<TransactionType, BigDecimal> current = totals.getOrDefault(target, Map.of());
    BigDecimal previousExpense = MoneyMath.money(MoneyMath.zeroIfNull(
      totals.getOrDefault(target.minusMonths(1), Map.of()).get(TransactionType.EXPENSE)));

    MonthlySummary summary = SummaryCalculator.summarize(current.get(TransactionType.INCOME),
      current.get(TransactionType.EXPENSE));
    MonthForecast forecast = target.equals(currentMonth())
      ? ForecastCalculator.forecast(summary.expense(), today().getDayOfMonth(), target.lengthOfMonth())
      : null;
    return new MonthlySummaryDto(target, summary.income(), summary.expense(), summary.savings(), summary.savingsRate(),
      previousExpense, MoneyMath.changePercent(summary.expense(), previousExpense), forecast);
  }

  @Override
  public OverviewDto overview(Long userId, LocalDate from, LocalDate to) {
    DateRange range = DateRange.resolve(from, to, currentMonth());
    Map<TransactionType, BigDecimal> totals = new EnumMap<>(TransactionType.class);
    analyticsRepository.totalsByType(userId, range.from(), range.toExclusive())
      .forEach(row -> totals.put(TransactionType.valueOf(row.getType()), row.getTotal()));
    BigDecimal income = MoneyMath.zeroIfNull(totals.get(TransactionType.INCOME));
    BigDecimal expense = MoneyMath.zeroIfNull(totals.get(TransactionType.EXPENSE));
    return new OverviewDto(range.from(), range.to(), MoneyMath.money(income), MoneyMath.money(expense),
      MoneyMath.money(income.subtract(expense)));
  }

  @Override
  public List<MonthlyTrendDto> yearly(Long userId, int year) {
    YearMonth start = YearMonth.of(year, 1);
    YearMonth end = YearMonth.of(year, 12);
    Map<YearMonth, Map<TransactionType, BigDecimal>> totals = monthlyTotals(userId, start, end);
    List<MonthlyTrendDto> rows = new ArrayList<>(12);
    for (YearMonth month = start; !month.isAfter(end); month = month.plusMonths(1)) {
      Map<TransactionType, BigDecimal> monthTotals = totals.getOrDefault(month, Map.of());
      MonthlySummary summary = SummaryCalculator.summarize(monthTotals.get(TransactionType.INCOME),
        monthTotals.get(TransactionType.EXPENSE));
      rows.add(new MonthlyTrendDto(month, summary.income(), summary.expense(), summary.savings(), summary.savingsRate()));
    }
    return rows;
  }

  @Override
  public List<Integer> periods(Long userId) {
    List<Integer> years = analyticsRepository.distinctYears(userId);
    return years.isEmpty() ? List.of(currentMonth().getYear()) : years;
  }

  @Override
  public List<CategorySpendingDto> categoryBreakdown(Long userId, LocalDate from, LocalDate to, TransactionType type) {
    DateRange range = DateRange.resolve(from, to, currentMonth());
    TransactionType targetType = type != null ? type : TransactionType.EXPENSE;
    List<CategoryTotalView> totals = analyticsRepository.totalsByCategory(userId, range.from(), range.toExclusive(),
      targetType.name());
    BigDecimal grandTotal = totals.stream().map(CategoryTotalView::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    Map<Long, Category> categories = categoryService.getCategoriesById(userId);

    return totals.stream()
      .sorted(Comparator.comparing(CategoryTotalView::getTotal).reversed())
      .map(row -> {
        Category category = categories.get(row.getCategoryId());
        return new CategorySpendingDto(String.valueOf(row.getCategoryId()), category.getName(), category.getIcon(),
          MoneyMath.money(row.getTotal()), MoneyMath.percent(row.getTotal(), grandTotal));
      })
      .toList();
  }

  @Override
  public List<CategoryComparisonDto> comparison(Long userId, YearMonth month) {
    Map<Long, SpendingComparison> comparisons = comparisonsByCategory(userId, resolve(month));
    Map<Long, Category> categories = categoryService.getCategoriesById(userId);
    return comparisons.entrySet().stream()
      .sorted(Map.Entry.<Long, SpendingComparison>comparingByValue(
        Comparator.comparing(SpendingComparison::current)).reversed())
      .map(entry -> {
        Category category = categories.get(entry.getKey());
        SpendingComparison c = entry.getValue();
        return new CategoryComparisonDto(String.valueOf(entry.getKey()), category.getName(), category.getIcon(),
          c.current(), c.previous(), c.threeMonthAverage(), c.changeVsPreviousPercent(), c.deviationVsAveragePercent());
      })
      .toList();
  }

  @Override
  public List<MonthlyTrendDto> trends(Long userId, int months) {
    YearMonth end = currentMonth();
    YearMonth start = end.minusMonths(months - 1L);
    Map<YearMonth, Map<TransactionType, BigDecimal>> totals = monthlyTotals(userId, start, end);

    List<MonthlyTrendDto> trend = new ArrayList<>(months);
    for (YearMonth month = start; !month.isAfter(end); month = month.plusMonths(1)) {
      Map<TransactionType, BigDecimal> monthTotals = totals.getOrDefault(month, Map.of());
      MonthlySummary summary = SummaryCalculator.summarize(monthTotals.get(TransactionType.INCOME),
        monthTotals.get(TransactionType.EXPENSE));
      trend.add(new MonthlyTrendDto(month, summary.income(), summary.expense(), summary.savings(), summary.savingsRate()));
    }
    return trend;
  }

  @Override
  public List<DailyCashflowDto> cashflow(Long userId, YearMonth month) {
    YearMonth target = resolve(month);
    int lastDay = lastDayToReport(target);
    DateRange range = DateRange.ofMonth(target);
    Map<Integer, Map<TransactionType, BigDecimal>> byDay = new HashMap<>();
    for (DayTypeTotalView row : analyticsRepository.dailyTotalsByType(userId, range.from(), range.toExclusive())) {
      byDay.computeIfAbsent(row.getDay(), day -> new EnumMap<>(TransactionType.class))
        .put(TransactionType.valueOf(row.getType()), row.getTotal());
    }

    List<DailyCashflowDto> cashflow = new ArrayList<>(lastDay);
    BigDecimal running = BigDecimal.ZERO;
    for (int day = 1; day <= lastDay; day++) {
      Map<TransactionType, BigDecimal> totals = byDay.getOrDefault(day, Map.of());
      BigDecimal income = MoneyMath.zeroIfNull(totals.get(TransactionType.INCOME));
      BigDecimal expense = MoneyMath.zeroIfNull(totals.get(TransactionType.EXPENSE));
      BigDecimal net = income.subtract(expense);
      running = running.add(net);
      cashflow.add(new DailyCashflowDto(target.atDay(day), MoneyMath.money(income), MoneyMath.money(expense),
        MoneyMath.money(net), MoneyMath.money(running)));
    }
    return cashflow;
  }

  @Override
  public List<RecurringExpenseDto> recurringExpenses(Long userId) {
    LocalDate from = currentMonth().minusMonths(RECURRING_LOOKBACK_MONTHS - 1L).atDay(1);
    List<ExpenseItem> expenses = expenseItems(userId, from, today().plusDays(1));
    Map<Long, Category> categories = categoryService.getCategoriesById(userId);
    return RecurringExpenseDetector.detect(expenses).stream()
      .map(r -> new RecurringExpenseDto(r.description(), String.valueOf(r.categoryId()),
        categories.get(r.categoryId()).getName(), r.typicalAmount(), r.occurrences(), r.intervalDays(),
        r.lastDate(), r.nextExpectedDate()))
      .toList();
  }

  @Override
  public List<AnomalyDto> anomalies(Long userId, YearMonth month) {
    YearMonth target = resolve(month);
    List<AnomalyDetector.CategorySpend> spends = comparisonsByCategory(userId, target).entrySet().stream()
      .map(e -> new AnomalyDetector.CategorySpend(e.getKey(), e.getValue().current(), e.getValue().threeMonthAverage()))
      .toList();
    List<AnomalyDetector.BudgetCheck> budgets = budgetService.getStatuses(userId, target).stream()
      .map(AnalyticsService::toBudgetCheck)
      .toList();
    LocalDate monthStart = target.atDay(1);
    List<ExpenseItem> recent = expenseItems(userId, monthStart.minusDays(LARGE_TRANSACTION_LOOKBACK_DAYS),
      target.plusMonths(1).atDay(1));

    Map<Long, Category> categories = categoryService.getCategoriesById(userId);
    return AnomalyDetector.detect(spends, budgets, recent, monthStart).stream()
      .map(anomaly -> toDto(anomaly, categories))
      .toList();
  }

  /** Category expense per month for the target month and the 3 months before it — one query. */
  private Map<Long, SpendingComparison> comparisonsByCategory(Long userId, YearMonth target) {
    YearMonth first = target.minusMonths(ComparisonCalculator.AVERAGE_WINDOW_MONTHS);
    Map<Long, Map<YearMonth, BigDecimal>> byCategory = new HashMap<>();
    for (MonthCategoryTotalView row : analyticsRepository.monthlyExpenseByCategory(userId, first.atDay(1),
      target.plusMonths(1).atDay(1))) {
      byCategory.computeIfAbsent(row.getCategoryId(), id -> new HashMap<>())
        .put(YearMonth.of(row.getYear(), row.getMonth()), row.getTotal());
    }

    Map<Long, SpendingComparison> comparisons = new HashMap<>();
    byCategory.forEach((categoryId, months) -> {
      List<BigDecimal> prior = new ArrayList<>(ComparisonCalculator.AVERAGE_WINDOW_MONTHS);
      for (int back = 1; back <= ComparisonCalculator.AVERAGE_WINDOW_MONTHS; back++) {
        prior.add(months.get(target.minusMonths(back)));
      }
      comparisons.put(categoryId, ComparisonCalculator.compare(months.get(target), prior));
    });
    return comparisons;
  }

  private Map<YearMonth, Map<TransactionType, BigDecimal>> monthlyTotals(Long userId, YearMonth start, YearMonth end) {
    Map<YearMonth, Map<TransactionType, BigDecimal>> totals = new HashMap<>();
    for (MonthTypeTotalView row : analyticsRepository.monthlyTotalsByType(userId, start.atDay(1),
      end.plusMonths(1).atDay(1))) {
      totals.computeIfAbsent(YearMonth.of(row.getYear(), row.getMonth()), m -> new EnumMap<>(TransactionType.class))
        .put(TransactionType.valueOf(row.getType()), row.getTotal());
    }
    return totals;
  }

  private List<ExpenseItem> expenseItems(Long userId, LocalDate from, LocalDate toExclusive) {
    return analyticsRepository.expensesInRange(userId, from, toExclusive).stream()
      .map(t -> new ExpenseItem(t.getId(), t.getCategoryId(), t.getAmount(), t.getTransactionDate(), t.getDescription()))
      .toList();
  }

  private int lastDayToReport(YearMonth target) {
    YearMonth current = currentMonth();
    if (target.equals(current)) {
      return today().getDayOfMonth();
    }
    return target.isBefore(current) ? target.lengthOfMonth() : 0;
  }

  private static AnomalyDetector.BudgetCheck toBudgetCheck(BudgetDto budget) {
    return new AnomalyDetector.BudgetCheck(Long.valueOf(budget.getCategoryId()), new BudgetStatus(budget.getAmount(),
      budget.getSpent(), budget.getRemaining(), budget.getPercentUsed(), budget.getLevel()));
  }

  private static AnomalyDto toDto(Anomaly anomaly, Map<Long, Category> categories) {
    Category category = categories.get(anomaly.categoryId());
    return new AnomalyDto(anomaly.type(), anomaly.severity(), String.valueOf(anomaly.categoryId()),
      category != null ? category.getName() : null,
      anomaly.transactionId() != null ? String.valueOf(anomaly.transactionId()) : null,
      anomaly.actual(), anomaly.baseline(), anomaly.deviationPercent());
  }

  private YearMonth resolve(YearMonth month) {
    return month != null ? month : currentMonth();
  }

  private YearMonth currentMonth() {
    return YearMonth.now(clock);
  }

  private LocalDate today() {
    return LocalDate.now(clock);
  }
}
