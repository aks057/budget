"use client";

import * as React from "react";
import { Bell, Loader2 } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { formatDistanceToNow } from "date-fns";
import { Button } from "@/components/ui/button";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip";
import { ScrollArea } from "@/components/ui/scroll-area";
import {
  getInsights,
  getUnreadInsightCount,
  markAllInsightsRead,
  markInsightRead,
  refreshInsights,
} from "@/lib/api/insights";
import type { InsightDto } from "@/lib/api/types";
import { INSIGHT_KEYS, insightDestination, SEVERITY_STYLE } from "@/lib/insights";
import { cn } from "@/lib/utils";

const PREVIEW_COUNT = 6;
const UNREAD_POLL_MS = 5 * 60 * 1000;
let refreshedThisLoad = false;

export function Notifications() {
  const [open, setOpen] = React.useState(false);
  const router = useRouter();
  const queryClient = useQueryClient();

  const unread = useQuery({
    queryKey: INSIGHT_KEYS.unread,
    queryFn: getUnreadInsightCount,
    refetchInterval: UNREAD_POLL_MS,
  });
  const insights = useQuery({ queryKey: INSIGHT_KEYS.list, queryFn: getInsights, enabled: open });

  // Evaluate the rules on demand (the daily job may not have run yet for today's activity). Idempotent server-side.
  const refresh = useMutation({
    mutationFn: refreshInsights,
    onSuccess: (created) => {
      if (created > 0) queryClient.invalidateQueries({ queryKey: INSIGHT_KEYS.all });
    },
  });
  const { mutate: runRefresh } = refresh;

  React.useEffect(() => {
    // The navbar renders this component twice (desktop + mobile): refresh once per page load.
    if (refreshedThisLoad) return;
    refreshedThisLoad = true;
    runRefresh();
  }, [runRefresh]);

  const markRead = useMutation({
    mutationFn: markInsightRead,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: INSIGHT_KEYS.all }),
  });
  const markAll = useMutation({
    mutationFn: markAllInsightsRead,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: INSIGHT_KEYS.all }),
  });

  const unreadCount = unread.data ?? 0;

  const openInsight = (insight: InsightDto) => {
    if (!insight.read) markRead.mutate(insight.id);
    setOpen(false);
    router.push(insightDestination(insight.type).href);
  };

  return (
    <TooltipProvider>
      <Popover
        open={open}
        onOpenChange={(next) => {
          setOpen(next);
          if (next) runRefresh();
        }}
      >
        <Tooltip>
          <TooltipTrigger asChild>
            <PopoverTrigger asChild>
              <Button
                variant="outline"
                size="icon"
                className="relative h-8 w-8 shrink-0 transition-transform active:scale-95 sm:h-9 sm:w-9"
                aria-label={`Notifications${unreadCount > 0 ? ` (${unreadCount} unread)` : ""}`}
              >
                <Bell className="h-4 w-4" />
                {unreadCount > 0 && (
                  <span className="absolute -right-1 -top-1 flex h-4 min-w-4 items-center justify-center rounded-full bg-brand px-0.5 text-[9px] font-medium text-brand-foreground">
                    {unreadCount > 9 ? "9+" : unreadCount}
                  </span>
                )}
              </Button>
            </PopoverTrigger>
          </TooltipTrigger>
          <TooltipContent>
            <p>Notifications</p>
          </TooltipContent>
        </Tooltip>
        <PopoverContent className="w-80 p-0" align="end">
          <div className="flex items-center justify-between border-b px-4 py-3">
            <h4 className="flex items-center gap-2 font-semibold">
              Notifications
              {refresh.isPending && <Loader2 className="h-3 w-3 animate-spin text-muted-foreground" />}
            </h4>
            {unreadCount > 0 && (
              <Button
                variant="ghost"
                size="sm"
                className="h-auto px-2 py-1 text-xs text-muted-foreground hover:text-foreground"
                onClick={() => markAll.mutate()}
                disabled={markAll.isPending}
              >
                Mark all as read
              </Button>
            )}
          </div>
          <ScrollArea className="h-[320px]">
            {insights.isLoading && (
              <div className="flex justify-center py-8">
                <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" />
              </div>
            )}
            {insights.isError && <p className="px-4 py-6 text-sm text-red-500">Could not load notifications.</p>}
            {insights.data?.length === 0 && (
              <div className="flex flex-col items-center justify-center px-4 py-8 text-center">
                <Bell className="mb-2 h-8 w-8 text-muted-foreground/50" />
                <p className="text-sm text-muted-foreground">You&apos;re all caught up</p>
                <p className="text-xs text-muted-foreground">Budget, spending and goal alerts will show up here.</p>
              </div>
            )}
            <div className="flex flex-col">
              {insights.data?.slice(0, PREVIEW_COUNT).map((insight) => {
                const { icon: Icon, className } = SEVERITY_STYLE[insight.severity];
                return (
                  <button
                    key={insight.id}
                    type="button"
                    onClick={() => openInsight(insight)}
                    className={cn(
                      "relative flex gap-3 border-b px-4 py-3 text-left transition-colors hover:bg-accent/50",
                      !insight.read && "bg-accent/30"
                    )}
                  >
                    <Icon className={cn("mt-0.5 h-4 w-4 shrink-0", className)} aria-hidden />
                    <span className="flex-1 space-y-1">
                      <span className="block text-sm font-medium leading-snug">{insight.title}</span>
                      <span className="line-clamp-2 block text-xs text-muted-foreground">{insight.body}</span>
                      <span className="block text-[10px] text-muted-foreground/70">
                        {formatDistanceToNow(insight.createdAt, { addSuffix: true })}
                      </span>
                    </span>
                    {!insight.read && (
                      <span className="absolute left-1.5 top-1/2 h-2 w-2 -translate-y-1/2 rounded-full bg-brand" aria-label="unread" />
                    )}
                  </button>
                );
              })}
            </div>
          </ScrollArea>
          <div className="border-t px-4 py-2 text-center">
            <Link href="/insights" onClick={() => setOpen(false)} className="text-xs font-medium hover:underline">
              View all insights
            </Link>
          </div>
        </PopoverContent>
      </Popover>
    </TooltipProvider>
  );
}
