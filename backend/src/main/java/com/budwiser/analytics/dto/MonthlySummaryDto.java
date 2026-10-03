package com.budwiser.analytics.dto;

import com.budwiser.analytics.engine.MonthForecast;
import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * @param savingsRate          null when the month has no income
 * @param expenseChangePercent vs previous month; null when last month had no expenses
 * @param forecast             month-end projection — only for the current month, otherwise null
 */
public record MonthlySummaryDto(YearMonth month, BigDecimal income, BigDecimal expense, BigDecimal savings,
                                BigDecimal savingsRate, BigDecimal previousMonthExpense,
                                BigDecimal expenseChangePercent, MonthForecast forecast) {}
