package com.budwiser.category.constant;

import com.budwiser.common.constant.TransactionType;
import java.util.List;

public final class CategoryConstants {
  private CategoryConstants() {}

  /** Keeps the (unpaginated) category list bounded. */
  public static final int MAX_CATEGORIES_PER_USER = 100;

  public record DefaultCategory(String name, String icon, TransactionType type) {}

  /** Seeded for every new user so the app (and the agent) is useful from the first transaction. */
  public static final List<DefaultCategory> DEFAULT_CATEGORIES = List.of(
    new DefaultCategory("Food", "🍔", TransactionType.EXPENSE),
    new DefaultCategory("Groceries", "🛒", TransactionType.EXPENSE),
    new DefaultCategory("Transport", "🚕", TransactionType.EXPENSE),
    new DefaultCategory("Shopping", "🛍️", TransactionType.EXPENSE),
    new DefaultCategory("Bills", "💡", TransactionType.EXPENSE),
    new DefaultCategory("Rent", "🏠", TransactionType.EXPENSE),
    new DefaultCategory("Entertainment", "🎬", TransactionType.EXPENSE),
    new DefaultCategory("Health", "💊", TransactionType.EXPENSE),
    new DefaultCategory("Education", "📚", TransactionType.EXPENSE),
    new DefaultCategory("Other", "📦", TransactionType.EXPENSE),
    new DefaultCategory("Salary", "💼", TransactionType.INCOME),
    new DefaultCategory("Freelance", "💻", TransactionType.INCOME),
    new DefaultCategory("Investments", "📈", TransactionType.INCOME),
    new DefaultCategory("Other Income", "💰", TransactionType.INCOME)
  );
}
