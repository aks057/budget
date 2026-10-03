package com.budwiser.insight.engine;

import com.budwiser.analytics.dto.AnomalyDto;
import com.budwiser.analytics.dto.RecurringExpenseDto;
import com.budwiser.analytics.engine.AnomalySeverity;
import com.budwiser.common.util.MoneyFormat;
import com.budwiser.insight.constant.InsightConstants;
import com.budwiser.insight.constant.InsightSeverity;
import com.budwiser.insight.constant.InsightType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Pure rules: turn analytics facts into template-phrased insights. No LLM is involved, so every number is exactly
 * what the engine computed and the text is deterministic and testable.
 *
 * <p>Dedupe keys decide how often a fact may be raised: once per month for budget/spike/goal-pace facts, once per
 * transaction, once per expected bill date, and once ever for an achieved goal.
 */
public final class InsightRules {
  private InsightRules() {}

  private static final DateTimeFormatter DAY_MONTH = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);
  private static final DateTimeFormatter DAY_MONTH_YEAR = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
  private static final DateTimeFormatter MONTH_NAME = DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH);

  public static List<InsightCandidate> evaluate(InsightFacts facts) {
    List<InsightCandidate> candidates = new ArrayList<>();
    YearMonth month = YearMonth.from(facts.today());
    facts.anomalies().forEach(anomaly -> fromAnomaly(anomaly, month, facts.currency()).ifPresent(candidates::add));
    facts.recurringExpenses().stream()
      .filter(bill -> isDueSoon(bill, facts.today()))
      .map(bill -> billDue(bill, facts.today(), facts.currency()))
      .forEach(candidates::add);
    facts.goals().forEach(goal -> fromGoal(goal, facts.today(), facts.averageMonthlySavings(), facts.currency())
      .ifPresent(candidates::add));
    return candidates;
  }

  private static Optional<InsightCandidate> fromAnomaly(AnomalyDto anomaly, YearMonth month, String currency) {
    String category = anomaly.categoryName() != null ? anomaly.categoryName() : "A category";
    String actual = MoneyFormat.format(anomaly.actual(), currency);
    String baseline = MoneyFormat.format(anomaly.baseline(), currency);
    String pct = wholePercent(anomaly.deviationPercent());
    return Optional.of(switch (anomaly.type()) {
      case BUDGET_THRESHOLD -> anomaly.actual().compareTo(anomaly.baseline()) > 0
        ? candidate(InsightType.BUDGET_EXCEEDED, InsightSeverity.CRITICAL,
            category + " budget exceeded",
            "You've spent " + actual + " of your " + baseline + " " + category + " budget this month ("
              + pct + "% used).",
            "budget-exceeded:" + anomaly.categoryId() + ":" + month)
        : candidate(InsightType.BUDGET_WARNING, InsightSeverity.WARNING,
            category + " budget at " + pct + "%",
            "You've used " + actual + " of your " + baseline + " " + category + " budget, with "
              + MoneyFormat.format(anomaly.baseline().subtract(anomaly.actual()), currency) + " left for the rest of "
              + month.format(MONTH_NAME) + ".",
            "budget-warning:" + anomaly.categoryId() + ":" + month);
      case SPENDING_SPIKE -> candidate(InsightType.SPENDING_SPIKE, severity(anomaly.severity()),
        category + " spending is up " + pct + "%",
        "You've spent " + actual + " on " + category + " so far this month, against a 3-month average of "
          + baseline + ".",
        "spike:" + anomaly.categoryId() + ":" + month);
      case LARGE_TRANSACTION -> candidate(InsightType.LARGE_TRANSACTION, InsightSeverity.WARNING,
        "Unusually large " + category + " expense",
        "A " + actual + " " + category + " expense is well above your usual " + baseline + ". Worth a quick check.",
        "large-tx:" + anomaly.transactionId());
    });
  }

  private static boolean isDueSoon(RecurringExpenseDto bill, LocalDate today) {
    LocalDate next = bill.nextExpectedDate();
    return next != null && !next.isBefore(today)
      && !next.isAfter(today.plusDays(InsightConstants.BILL_DUE_WINDOW_DAYS));
  }

  private static InsightCandidate billDue(RecurringExpenseDto bill, LocalDate today, String currency) {
    String name = bill.description() != null && !bill.description().isBlank() ? bill.description().trim()
      : bill.categoryName();
    long days = ChronoUnit.DAYS.between(today, bill.nextExpectedDate());
    String when = days == 0 ? "today" : days == 1 ? "tomorrow" : "on " + bill.nextExpectedDate().format(DAY_MONTH);
    return candidate(InsightType.BILL_DUE, InsightSeverity.INFO,
      name + " is due " + when,
      "Based on your history, " + name + " (usually " + MoneyFormat.format(bill.typicalAmount(), currency)
        + ") is expected " + when + ".",
      "bill:" + bill.categoryId() + ":" + name.toLowerCase(Locale.ROOT) + ":" + bill.nextExpectedDate());
  }

  private static Optional<InsightCandidate> fromGoal(InsightFacts.GoalFact goal, LocalDate today,
                                                              BigDecimal averageSavings, String currency) {
    var progress = goal.progress();
    YearMonth month = YearMonth.from(today);
    if (progress.achieved()) {
      // Announce only right after it was reached: the once-ever key would otherwise re-fire after retention purges it.
      boolean justReached = !goal.updatedOn().isBefore(today.minusDays(InsightConstants.GOAL_ACHIEVED_WINDOW_DAYS));
      return !justReached ? Optional.empty() : Optional.of(candidate(InsightType.GOAL_ACHIEVED, InsightSeverity.INFO,
        "Goal reached: " + goal.name(),
        "You've saved the full amount for " + goal.name() + ". Nice work!",
        "goal-achieved:" + goal.id()));
    }
    if (progress.overdue()) {
      return Optional.of(candidate(InsightType.GOAL_OVERDUE, InsightSeverity.CRITICAL,
        goal.name() + " is past its target date",
        "The target date (" + goal.targetDate().format(DAY_MONTH_YEAR) + ") has passed with "
          + MoneyFormat.format(progress.remaining(), currency) + " still to save. Consider a new date.",
        "goal-overdue:" + goal.id() + ":" + month));
    }
    // Behind pace: the contribution needed per month is more than the user has actually been saving.
    if (averageSavings != null && progress.requiredMonthlyContribution().compareTo(averageSavings.max(BigDecimal.ZERO)) > 0) {
      return Optional.of(candidate(InsightType.GOAL_BEHIND, InsightSeverity.WARNING,
        goal.name() + " is behind schedule",
        "You need " + MoneyFormat.format(progress.requiredMonthlyContribution(), currency) + "/month to reach it by "
          + goal.targetDate().format(DAY_MONTH_YEAR) + ", but you've saved about "
          + MoneyFormat.format(averageSavings.max(BigDecimal.ZERO), currency) + "/month recently.",
        "goal-behind:" + goal.id() + ":" + month));
    }
    return Optional.empty();
  }

  private static InsightSeverity severity(AnomalySeverity severity) {
    return switch (severity) {
      case INFO -> InsightSeverity.INFO;
      case WARNING -> InsightSeverity.WARNING;
      case CRITICAL -> InsightSeverity.CRITICAL;
    };
  }

  private static String wholePercent(BigDecimal value) {
    return value == null ? "0" : value.setScale(0, RoundingMode.HALF_UP).toPlainString();
  }

  private static InsightCandidate candidate(InsightType type, InsightSeverity severity, String title, String body,
                                            String dedupeKey) {
    return new InsightCandidate(type, severity, truncate(title, InsightConstants.TITLE_MAX_LENGTH),
      truncate(body, InsightConstants.BODY_MAX_LENGTH), truncate(dedupeKey, InsightConstants.DEDUPE_KEY_MAX_LENGTH));
  }

  private static String truncate(String value, int max) {
    return value.length() <= max ? value : value.substring(0, max - 1) + "…";
  }
}
