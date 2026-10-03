package com.budwiser.analytics.engine;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @param description         the most recent occurrence's description (as the user typed it)
 * @param typicalAmount       median amount across occurrences
 * @param intervalDays        median gap between occurrences
 * @param nextExpectedDate    last occurrence + intervalDays
 */
public record RecurringExpense(String description, Long categoryId, BigDecimal typicalAmount, int occurrences,
                               int intervalDays, LocalDate lastDate, LocalDate nextExpectedDate) {}
