package com.budwiser.analytics.dto;

import java.math.BigDecimal;

public record CategorySpendingDto(String categoryId, String categoryName, String categoryIcon, BigDecimal total,
                                  BigDecimal percentOfTotal) {}
