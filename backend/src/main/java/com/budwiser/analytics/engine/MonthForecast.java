package com.budwiser.analytics.engine;

import java.math.BigDecimal;

public record MonthForecast(BigDecimal spentSoFar, BigDecimal dailyRunRate, BigDecimal projectedTotal,
                            int daysElapsed, int daysInMonth) {}
