import { apiFetch } from "@/lib/api/client";
import type { InsightDto } from "@/lib/api/types";

export const getInsights = () => apiFetch<InsightDto[]>("/api/v1/insights");

export const getUnreadInsightCount = async () =>
  (await apiFetch<{ count: number }>("/api/v1/insights/unread-count")).count;

export const markInsightRead = (id: string) => apiFetch<void>(`/api/v1/insights/${id}/read`, { method: "POST" });

export const markAllInsightsRead = () =>
  apiFetch<{ updated: number }>("/api/v1/insights/read-all", { method: "POST" });

/** Evaluates the rules for the current user now. Idempotent, so it is safe to call whenever fresh data matters. */
export const refreshInsights = async () =>
  (await apiFetch<{ created: number }>("/api/v1/insights/refresh", { method: "POST" })).created;
