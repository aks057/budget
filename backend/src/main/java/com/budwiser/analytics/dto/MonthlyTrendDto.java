package com.budwiser.analytics.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlyTrendDto(YearMonth month, BigDecimal income, BigDecimal expense, BigDecimal savings,
                              BigDecimal savingsRate) {}
