package com.budwiser.analytics.repository;

import java.math.BigDecimal;

public interface CategoryTotalView {

  Long getCategoryId();

  BigDecimal getTotal();
}
