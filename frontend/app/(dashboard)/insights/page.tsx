"use client";

import SkeletonWrapper from "@/components/SkeletonWrapper";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ApiError } from "@/lib/api/client";
import { getInsights, markAllInsightsRead, markInsightRead, refreshInsights } from "@/lib/api/insights";
import type { InsightDto } from "@/lib/api/types";
import { askAiHref, INSIGHT_KEYS, insightDestination, SEVERITY_STYLE } from "@/lib/insights";
import { cn } from "@/lib/utils";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { format } from "date-fns";
import { BellRing, CheckCheck, Loader2, RefreshCw, Sparkles } from "lucide-react";
import Link from "next/link";
import { useMemo, useState } from "react";
import { toast } from "sonner";

type Filter = "all" | "unread";

export default function InsightsPage() {
  const [filter, setFilter] = useState<Filter>("all");
  const queryClient = useQueryClient();
  const insights = useQuery({ queryKey: INSIGHT_KEYS.list, queryFn: getInsights });
  const invalidate = () => queryClient.invalidateQueries({ queryKey: INSIGHT_KEYS.all });

  const refresh = useMutation({
    mutationFn: refreshInsights,
    onSuccess: (created) => {
      toast.success(created > 0 ? `${created} new insight${created === 1 ? "" : "s"}` : "No new insights");
      invalidate();
    },
    onError: (error) => toast.error(error instanceof ApiError ? error.userMessage : "Could not check for insights"),
  });
  const markRead = useMutation({ mutationFn: markInsightRead, onSuccess: invalidate });
  const markAll = useMutation({ mutationFn: markAllInsightsRead, onSuccess: invalidate });

  const visible = useMemo(
    () => (insights.data ?? []).filter((insight) => filter === "all" || !insight.read),
    [filter, insights.data]
  );
  const unreadCount = (insights.data ?? []).filter((insight) => !insight.read).length;

  return (
    <>
      <div className="border-b bg-card">
        <div className="container flex flex-wrap items-center justify-between gap-6 py-8">
          <div>
            <p className="text-3xl font-bold">Insights</p>
            <p className="text-muted-foreground">Alerts about your budgets, spending, bills and goals</p>
          </div>
          <div className="flex flex-wrap gap-2">
            <Button variant="outline" className="gap-2" onClick={() => refresh.mutate()} disabled={refresh.isPending}>
              {refresh.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <RefreshCw className="h-4 w-4" />}
              Check now
            </Button>
            <Button
              variant="outline"
              className="gap-2"
              onClick={() => markAll.mutate()}
              disabled={unreadCount === 0 || markAll.isPending}
            >
              <CheckCheck className="h-4 w-4" /> Mark all read
            </Button>
          </div>
        </div>
      </div>

      <div className="container space-y-4 py-6">
        <Tabs value={filter} onValueChange={(value) => setFilter(value as Filter)}>
          <TabsList>
            <TabsTrigger value="all">All</TabsTrigger>
            <TabsTrigger value="unread">Unread{unreadCount > 0 ? ` (${unreadCount})` : ""}</TabsTrigger>
          </TabsList>
        </Tabs>

        {insights.isError && <p className="text-sm text-red-500">Could not load insights.</p>}

        <SkeletonWrapper isLoading={insights.isLoading}>
          {visible.length === 0 ? (
            <Card className="flex flex-col items-center gap-3 py-12 text-center">
              <BellRing className="h-12 w-12 text-muted-foreground/50" />
              <p className="text-lg font-medium">{filter === "unread" ? "No unread insights" : "No insights yet"}</p>
              <p className="max-w-sm text-sm text-muted-foreground">
                Bud-Wiser checks your budgets, spending, recurring bills and goals every morning and alerts you here.
              </p>
            </Card>
          ) : (
            <div className="space-y-3">
              {visible.map((insight) => (
                <InsightCard key={insight.id} insight={insight} onRead={() => markRead.mutate(insight.id)} />
              ))}
            </div>
          )}
        </SkeletonWrapper>
      </div>
    </>
  );
}

function InsightCard({ insight, onRead }: { insight: InsightDto; onRead: () => void }) {
  const { icon: Icon, className, label } = SEVERITY_STYLE[insight.severity];
  const destination = insightDestination(insight.type);
  return (
    <Card className={cn(!insight.read && "border-l-4 border-l-brand")}>
      <CardContent className="flex gap-4 p-4">
        <Icon className={cn("mt-0.5 h-5 w-5 shrink-0", className)} aria-label={label} />
        <div className="min-w-0 flex-1 space-y-2">
          <div className="flex flex-wrap items-center gap-2">
            <p className="font-semibold">{insight.title}</p>
            {!insight.read && <Badge variant="secondary">New</Badge>}
          </div>
          <p className="text-sm text-muted-foreground">{insight.body}</p>
          <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs">
            <span className="text-muted-foreground">{format(insight.createdAt, "d MMM yyyy, HH:mm")}</span>
            <Link href={destination.href} onClick={onRead} className="font-medium hover:underline">
              {destination.label}
            </Link>
            <Link
              href={askAiHref(insight)}
              onClick={onRead}
              className="flex items-center gap-1 font-medium text-amber-600 hover:underline dark:text-amber-400"
            >
              <Sparkles className="h-3 w-3" /> Ask AI about this
            </Link>
            {!insight.read && (
              <button type="button" onClick={onRead} className="text-muted-foreground hover:text-foreground">
                Mark as read
              </button>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
