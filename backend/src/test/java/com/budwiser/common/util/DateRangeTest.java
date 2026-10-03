package com.budwiser.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.budwiser.common.constant.ErrorCode;
import com.budwiser.common.exception.ValidationException;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DateRangeTest {
  private static final YearMonth FEB_2028 = YearMonth.of(2028, 2);

  @Test
  @DisplayName("missing bounds default to the whole month (leap-year February)")
  void defaultsToMonth() {
    DateRange actual = DateRange.resolve(null, null, FEB_2028);

    assertThat(actual.from()).isEqualTo(LocalDate.of(2028, 2, 1));
    assertThat(actual.to()).isEqualTo(LocalDate.of(2028, 2, 29));
    assertThat(actual.toExclusive()).isEqualTo(LocalDate.of(2028, 3, 1));
  }

  @Test
  @DisplayName("from after to is rejected")
  void invertedRange() {
    assertThatThrownBy(() -> DateRange.resolve(LocalDate.of(2026, 5, 2), LocalDate.of(2026, 5, 1), FEB_2028))
      .isInstanceOf(ValidationException.class)
      .hasMessage(ErrorCode.INVALID_DATE_RANGE.getDescription());
  }

  @Test
  @DisplayName("366 days is allowed, 367 is not")
  void maxSpan() {
    LocalDate from = LocalDate.of(2027, 1, 1);
    assertThat(DateRange.resolve(from, from.plusDays(365), FEB_2028).to()).isEqualTo(from.plusDays(365));
    assertThatThrownBy(() -> DateRange.resolve(from, from.plusDays(366), FEB_2028))
      .isInstanceOf(ValidationException.class)
      .hasMessage(ErrorCode.DATE_RANGE_TOO_LARGE.getDescription());
  }
}
