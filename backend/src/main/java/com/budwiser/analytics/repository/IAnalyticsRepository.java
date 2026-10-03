package com.budwiser.analytics.repository;

import com.budwiser.analytics.constant.AnalyticsQueries;
import com.budwiser.transaction.entity.Transaction;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Read-only aggregations backing the financial engine. Extends the marker {@link Repository} (not JpaRepository)
 * so no write methods are exposed. (Spring Data discovers it without a @Repository annotation.)
 */
public interface IAnalyticsRepository extends Repository<Transaction, Long> {

  @Query(value = AnalyticsQueries.TOTALS_BY_TYPE, nativeQuery = true)
  List<TypeTotalView> totalsByType(@Param("userId") Long userId,
                                   @Param("from") LocalDate from,
                                   @Param("toExclusive") LocalDate toExclusive);

  @Query(value = AnalyticsQueries.TOTALS_BY_CATEGORY, nativeQuery = true)
  List<CategoryTotalView> totalsByCategory(@Param("userId") Long userId,
                                           @Param("from") LocalDate from,
                                           @Param("toExclusive") LocalDate toExclusive,
                                           @Param("type") String type);

  @Query(value = AnalyticsQueries.MONTHLY_TOTALS_BY_TYPE, nativeQuery = true)
  List<MonthTypeTotalView> monthlyTotalsByType(@Param("userId") Long userId,
                                               @Param("from") LocalDate from,
                                               @Param("toExclusive") LocalDate toExclusive);

  @Query(value = AnalyticsQueries.MONTHLY_EXPENSE_BY_CATEGORY, nativeQuery = true)
  List<MonthCategoryTotalView> monthlyExpenseByCategory(@Param("userId") Long userId,
                                                        @Param("from") LocalDate from,
                                                        @Param("toExclusive") LocalDate toExclusive);

  @Query(value = AnalyticsQueries.DAILY_TOTALS_BY_TYPE, nativeQuery = true)
  List<DayTypeTotalView> dailyTotalsByType(@Param("userId") Long userId,
                                           @Param("from") LocalDate from,
                                           @Param("toExclusive") LocalDate toExclusive);

  @Query(value = AnalyticsQueries.DISTINCT_YEARS, nativeQuery = true)
  List<Integer> distinctYears(@Param("userId") Long userId);

  @Query(value = AnalyticsQueries.EXPENSES_IN_RANGE, nativeQuery = true)
  List<Transaction> expensesInRange(@Param("userId") Long userId,
                                    @Param("from") LocalDate from,
                                    @Param("toExclusive") LocalDate toExclusive);
}
