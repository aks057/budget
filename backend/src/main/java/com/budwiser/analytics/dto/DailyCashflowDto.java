package com.budwiser.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @param runningBalance cumulative net from the 1st of the month
 */
public record DailyCashflowDto(LocalDate date, BigDecimal income, BigDecimal expense, BigDecimal net,
                               BigDecimal runningBalance) {}
