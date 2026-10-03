import type { AnomalySeverity, InsightDto, InsightType } from "@/lib/api/types";
import { AlertTriangle, Info, OctagonAlert, type LucideIcon } from "lucide-react";

export const SEVERITY_STYLE: Record<AnomalySeverity, { icon: LucideIcon; className: string; label: string }> = {
  CRITICAL: { icon: OctagonAlert, className: "text-red-500", label: "Critical" },
  WARNING: { icon: AlertTriangle, className: "text-amber-500", label: "Warning" },
  INFO: { icon: Info, className: "text-blue-500", label: "Info" },
};

/** Where each kind of insight is acted on. */
const DESTINATIONS: Record<InsightType, { href: string; label: string }> = {
  BUDGET_WARNING: { href: "/budgets", label: "View budgets" },
  BUDGET_EXCEEDED: { href: "/budgets", label: "View budgets" },
  SPENDING_SPIKE: { href: "/analytics", label: "View analytics" },
  LARGE_TRANSACTION: { href: "/transactions", label: "View transactions" },
  BILL_DUE: { href: "/transactions", label: "View transactions" },
  GOAL_BEHIND: { href: "/goals", label: "View goals" },
  GOAL_OVERDUE: { href: "/goals", label: "View goals" },
  GOAL_ACHIEVED: { href: "/goals", label: "View goals" },
};

export const insightDestination = (type: InsightType) => DESTINATIONS[type];

/** Opens the assistant with the insight as context; the agent then pulls the real numbers through its tools. */
export const askAiHref = (insight: InsightDto) =>
  `/ai?q=${encodeURIComponent(`About this alert: "${insight.title}". ${insight.body} What should I do?`.slice(0, 2000))}`;

// Shared React Query keys (everything under "insights" is invalidated together).
export const INSIGHT_KEYS = {
  all: ["insights"] as const,
  list: ["insights", "list"] as const,
  unread: ["insights", "unread"] as const,
};
