package com.budwiser.common.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;

/**
 * Currency formatting for human-readable text (confirmations, insights). Locales match frontend/lib/currencies.ts
 * so the server and the UI render the same amount identically.
 */
public final class MoneyFormat {
  private MoneyFormat() {}

  private static final Locale INDIA = Locale.of("en", "IN");
  private static final Map<String, Locale> LOCALES = Map.of(
    "INR", INDIA,
    "USD", Locale.of("en", "US"),
    "EUR", Locale.of("de", "DE"),
    "GBP", Locale.of("en", "GB"));

  /** Indian-grouped rupees (₹3,00,000.00). NumberFormat is not thread-safe, so a fresh instance per call. */
  public static String inr(BigDecimal amount) {
    return format(amount, "INR");
  }

  /** Formats in the user's currency; unknown codes fall back to INR formatting. */
  public static String format(BigDecimal amount, String currencyCode) {
    String code = currencyCode != null && LOCALES.containsKey(currencyCode) ? currencyCode : "INR";
    NumberFormat format = NumberFormat.getCurrencyInstance(LOCALES.get(code));
    format.setCurrency(Currency.getInstance(code));
    return format.format(MoneyMath.money(amount));
  }
}
