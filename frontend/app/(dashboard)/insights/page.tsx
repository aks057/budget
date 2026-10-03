"use client";

import { EmptyState } from "@/components/EmptyState";
import { PageHeader } from "@/components/PageHeader";
import SkeletonWrapper from "@/components/SkeletonWrapper";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ApiError } from "@/lib/api/client";
import { getInsights, markAllInsightsRead, markInsightRead, refreshInsights } from "@/lib/api/insights";
import type { InsightDto } from "@/lib/api/types";
import { useAiPanel } from "@/components/agent/AiPanelProvider";
import { askAiPrompt, INSIGHT_KEYS, insightDestination, SEVERITY_STYLE } from "@/lib/insights";
import { cn } from "@/lib/utils";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { format } from "date-fns";
import { AnimatePresence, motion } from "framer-motion";
import { BellRing, CheckCheck, RefreshCw, Sparkles } from "lucide-react";
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
    <div className="pb-16">
      <PageHeader
        icon={BellRing}
        title="Insights"
        subtitle="Alerts about your budgets, spending, bills and goals"
        actions={
          <>
            <Button variant="outline" className="gap-2" onClick={() => refresh.mutate()} disabled={refresh.isPending}>
              <RefreshCw className={cn("h-4 w-4", refresh.isPending && "animate-spin")} />
              Check now
            </Button>
            <Button variant="outline" className="gap-2" onClick={() => markAll.mutate()} disabled={unreadCount === 0 || markAll.isPending}>
              <CheckCheck className="h-4 w-4" /> Mark all read
            </Button>
          </>
        }
      />

      <div className="container max-w-4xl space-y-4">
        <Tabs value={filter} onValueChange={(value) => setFilter(value as Filter)}>
          <TabsList>
            <TabsTrigger value="all">All</TabsTrigger>
            <TabsTrigger value="unread">Unread{unreadCount > 0 ? ` (${unreadCount})` : ""}</TabsTrigger>
          </TabsList>
        </Tabs>

        {insights.isError && <p className="text-sm text-expense">Could not load insights.</p>}

        <SkeletonWrapper isLoading={insights.isLoading}>
          {visible.length === 0 ? (
            <EmptyState
              icon={BellRing}
              title={filter === "unread" ? "You're all caught up" : "No insights yet"}
              description="Bud-Wiser checks your budgets, spending, recurring bills and goals every morning and alerts you here."
            />
          ) : (
            <motion.div layout className="space-y-3">
              <AnimatePresence initial={false} mode="popLayout">
                {visible.map((insight, index) => (
                  <motion.div
                    key={insight.id}
                    layout
                    initial={{ opacity: 0, y: 12 }}
                    animate={{ opacity: 1, y: 0, transition: { delay: Math.min(index, 8) * 0.04 } }}
                    exit={{ opacity: 0, x: 40, transition: { duration: 0.25 } }}
                  >
                    <InsightCard insight={insight} onRead={() => markRead.mutate(insight.id)} />
                  </motion.div>
                ))}
              </AnimatePresence>
            </motion.div>
          )}
        </SkeletonWrapper>
      </div>
    </div>
  );
}

function InsightCard({ insight, onRead }: { insight: InsightDto; onRead: () => void }) {
  const { icon: Icon, className, label } = SEVERITY_STYLE[insight.severity];
  const destination = insightDestination(insight.type);
  const { ask } = useAiPanel();
  return (
    <Card className={cn("transition-colors hover:border-primary/30", !insight.read && "border-l-4 border-l-primary")}>
      <CardContent className="flex gap-4 p-4">
        <Icon className={cn("mt-0.5 h-5 w-5 shrink-0", className)} aria-label={label} />
        <div className="min-w-0 flex-1 space-y-2">
          <div className="flex flex-wrap items-center gap-2">
            <p className="font-semibold">{insight.title}</p>
            {!insight.read && <Badge className="border-0 bg-primary/15 text-primary hover:bg-primary/15">New</Badge>}
          </div>
          <p className="text-sm text-muted-foreground">{insight.body}</p>
          <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs">
            <span className="text-muted-foreground">{format(insight.createdAt, "d MMM yyyy, HH:mm")}</span>
            <Link href={destination.href} onClick={onRead} className="font-medium hover:underline">
              {destination.label}
            </Link>
            <button
              type="button"
              onClick={() => {
                onRead();
                ask(askAiPrompt(insight));
              }}
              className="flex items-center gap-1 font-medium text-primary hover:underline"
            >
              <Sparkles className="h-3 w-3" /> Ask AI about this
            </button>
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
