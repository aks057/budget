package com.budwiser.analytics.repository;

import java.math.BigDecimal;

public interface DayTypeTotalView {

  Integer getDay();

  String getType();

  BigDecimal getTotal();
}
