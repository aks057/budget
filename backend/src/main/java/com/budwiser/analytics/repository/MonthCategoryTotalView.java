package com.budwiser.analytics.repository;

import java.math.BigDecimal;

public interface MonthCategoryTotalView {

  Integer getYear();

  Integer getMonth();

  Long getCategoryId();

  BigDecimal getTotal();
}
