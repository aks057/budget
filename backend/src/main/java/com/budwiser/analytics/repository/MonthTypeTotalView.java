package com.budwiser.analytics.repository;

import java.math.BigDecimal;

public interface MonthTypeTotalView {

  Integer getYear();

  Integer getMonth();

  String getType();

  BigDecimal getTotal();
}
