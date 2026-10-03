package com.budwiser.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Totals for an arbitrary date range (the dashboard's date-range picker). balance = income − expense.
 */
public record OverviewDto(LocalDate from, LocalDate to, BigDecimal income, BigDecimal expense, BigDecimal balance) {}
