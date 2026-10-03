package com.budwiser.analytics.engine;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Engine-side view of one expense — keeps the detectors free of JPA entities.
 */
public record ExpenseItem(Long transactionId, Long categoryId, BigDecimal amount, LocalDate date, String description) {}
