package com.budwiser.common.util;

import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.exception.ValidationException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

/**
 * Inclusive date range with the API's guard rails (from ≤ to, at most {@link #MAX_DAYS} days).
 */
public record DateRange(LocalDate from, LocalDate to) {
  public static final long MAX_DAYS = 366;

  /** Missing bounds default to the given month; the result is validated. */
  public static DateRange resolve(LocalDate from, LocalDate to, YearMonth defaultMonth) {
    DateRange range = new DateRange(
      from != null ? from : defaultMonth.atDay(1),
      to != null ? to : defaultMonth.atEndOfMonth());
    range.validate();
    return range;
  }

  public static DateRange ofMonth(YearMonth month) {
    return new DateRange(month.atDay(1), month.atEndOfMonth());
  }

  /** Exclusive upper bound, for SQL "date < :toExclusive". */
  public LocalDate toExclusive() {
    return to.plusDays(1);
  }

  private void validate() {
    if (from.isAfter(to)) {
      throw new ValidationException(ErrorCode.INVALID_DATE_RANGE);
    }
    if (ChronoUnit.DAYS.between(from, to) + 1 > MAX_DAYS) {
      throw new ValidationException(ErrorCode.DATE_RANGE_TOO_LARGE);
    }
  }
}
