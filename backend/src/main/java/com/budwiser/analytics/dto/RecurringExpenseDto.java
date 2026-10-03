package com.budwiser.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringExpenseDto(String description, String categoryId, String categoryName,
                                  BigDecimal typicalAmount, int occurrences, int intervalDays, LocalDate lastDate,
                                  LocalDate nextExpectedDate) {}
