package com.budwiser.common.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Indian-grouped rupee formatting (₹3,00,000.00) for human-readable confirmations.
 */
public final class MoneyFormat {
  private MoneyFormat() {}

  private static final Locale INDIA = Locale.of("en", "IN");

  /** NumberFormat is not thread-safe, so a fresh instance per call. */
  public static String inr(BigDecimal amount) {
    NumberFormat format = NumberFormat.getCurrencyInstance(INDIA);
    return format.format(MoneyMath.money(amount));
  }
}
