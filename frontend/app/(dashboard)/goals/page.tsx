"use client";

import { useAiPanel } from "@/components/agent/AiPanelProvider";
import { EmptyState } from "@/components/EmptyState";
import { AnimatedNumber, EASE_OUT, SpotlightCard, Stagger, StaggerItem } from "@/components/motion";
import { PageHeader } from "@/components/PageHeader";
import { useCurrentUser } from "@/components/providers/AuthProvider";
import SkeletonWrapper from "@/components/SkeletonWrapper";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { ApiError } from "@/lib/api/client";
import { deleteGoal, getGoals } from "@/lib/api/endpoints";
import type { GoalDto } from "@/lib/api/types";
import { GetFormatterForCurrency } from "@/lib/helpers";
import { cn } from "@/lib/utils";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { format, parseISO } from "date-fns";
import { motion } from "framer-motion";
import { CalendarDays, MoreHorizontal, Pencil, PartyPopper, Plus, Sparkles, Target, Trash2 } from "lucide-react";
import { useId, useMemo, useState } from "react";
import { toast } from "sonner";
import { GoalDialog } from "./_components/GoalDialog";

export default function GoalsPage() {
  const user = useCurrentUser();
  const { ask } = useAiPanel();
  const formatter = useMemo(() => GetFormatterForCurrency(user.currency), [user.currency]);
  const formatMoney = useMemo(() => (value: number) => formatter.format(value), [formatter]);
  const [dialog, setDialog] = useState<{ open: boolean; goal?: GoalDto }>({ open: false });
  const [toDelete, setToDelete] = useState<GoalDto | null>(null);

  const goals = useQuery({ queryKey: ["goals"], queryFn: getGoals });

  const queryClient = useQueryClient();
  const deleteMutation = useMutation({
    mutationFn: (id: string) => deleteGoal(id),
    onSuccess: () => {
      toast.success("Goal deleted");
      queryClient.invalidateQueries({ queryKey: ["goals"] });
    },
    onError: (error) => toast.error(error instanceof ApiError ? error.userMessage : "Could not delete the goal"),
  });

  const totals = useMemo(() => {
    const list = goals.data ?? [];
    return {
      saved: list.reduce((sum, goal) => sum + goal.currentAmount, 0),
      target: list.reduce((sum, goal) => sum + goal.targetAmount, 0),
      achieved: list.filter((goal) => goal.achieved).length,
    };
  }, [goals.data]);

  return (
    <div className="pb-16">
      <PageHeader
        icon={Target}
        title="Goals"
        subtitle={
          (goals.data?.length ?? 0) > 0 ? (
            <>
              <AnimatedNumber value={totals.saved} format={formatMoney} className="font-semibold text-foreground" /> saved of{" "}
              {formatter.format(totals.target)} across {goals.data?.length} goal{goals.data?.length === 1 ? "" : "s"}
              {totals.achieved > 0 && ` · ${totals.achieved} reached`}
            </>
          ) : (
            "Track what you're saving towards"
          )
        }
        actions={
          <>
            <Button variant="outline" className="gap-2" onClick={() => ask("Am I on track for my goals?")}>
              <Sparkles className="h-4 w-4 text-primary" /> Ask AI
            </Button>
            <Button className="gap-2" onClick={() => setDialog({ open: true })}>
              <Plus className="h-4 w-4" /> New goal
            </Button>
          </>
        }
      />

      <div className="container">
        {goals.isError && <p className="text-sm text-expense">Could not load goals.</p>}
        <SkeletonWrapper isLoading={goals.isLoading}>
          {goals.data?.length === 0 ? (
            <EmptyState
              icon={Target}
              title="No goals yet"
              description="Add a target like an emergency fund or a trip, and see how much to save each month."
              action={<Button onClick={() => setDialog({ open: true })}>Create your first goal</Button>}
            />
          ) : (
            <Stagger onMount className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {goals.data?.map((goal) => (
                <StaggerItem key={goal.id}>
                  <GoalCard
                    goal={goal}
                    formatter={formatter}
                    onEdit={() => setDialog({ open: true, goal })}
                    onDelete={() => setToDelete(goal)}
                  />
                </StaggerItem>
              ))}
            </Stagger>
          )}
        </SkeletonWrapper>
      </div>

      <GoalDialog open={dialog.open} goal={dialog.goal} onOpenChange={(open) => setDialog((prev) => ({ ...prev, open }))} />

      <AlertDialog open={toDelete !== null} onOpenChange={(open) => !open && setToDelete(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete &quot;{toDelete?.name}&quot;?</AlertDialogTitle>
            <AlertDialogDescription>This cannot be undone.</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction onClick={() => toDelete && deleteMutation.mutate(toDelete.id)}>Delete</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}

function ProgressRing({ percent, achieved }: { percent: number; achieved: boolean }) {
  const gradientId = useId().replace(/:/g, "");
  const radius = 40;
  const circumference = 2 * Math.PI * radius;
  return (
    <div className="relative h-28 w-28 shrink-0">
      <svg viewBox="0 0 100 100" className="h-28 w-28 -rotate-90">
        <circle cx="50" cy="50" r={radius} fill="none" stroke="hsl(var(--muted))" strokeWidth="8" />
        <motion.circle
          cx="50"
          cy="50"
          r={radius}
          fill="none"
          stroke={`url(#${gradientId})`}
          strokeWidth="8"
          strokeLinecap="round"
          strokeDasharray={circumference}
          initial={{ strokeDashoffset: circumference }}
          animate={{ strokeDashoffset: circumference * (1 - Math.min(percent, 100) / 100) }}
          transition={{ duration: 1.3, ease: EASE_OUT, delay: 0.1 }}
        />
        <defs>
          <linearGradient id={gradientId} x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="#34d399" />
            <stop offset="100%" stopColor={achieved ? "#34d399" : "#22d3ee"} />
          </linearGradient>
        </defs>
      </svg>
      <div className="absolute inset-0 flex flex-col items-center justify-center">
        {achieved ? (
          <PartyPopper className="h-7 w-7 text-income" />
        ) : (
          <>
            <AnimatedNumber value={percent} format={(v) => `${Math.round(v)}%`} className="font-display text-xl font-bold" />
            <span className="text-[10px] uppercase tracking-wider text-muted-foreground">saved</span>
          </>
        )}
      </div>
    </div>
  );
}

function GoalCard({
  goal,
  formatter,
  onEdit,
  onDelete,
}: {
  goal: GoalDto;
  formatter: Intl.NumberFormat;
  onEdit: () => void;
  onDelete: () => void;
}) {
  const status = goal.achieved
    ? { label: "Achieved", className: "bg-income/10 text-income" }
    : goal.overdue
      ? { label: "Overdue", className: "bg-expense/10 text-expense" }
      : { label: "In progress", className: "bg-primary/10 text-primary" };

  return (
    <SpotlightCard
      className={cn(
        "h-full p-5",
        goal.achieved && "border-income/40",
        goal.overdue && "border-expense/30"
      )}
    >
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="truncate font-display text-lg font-semibold">{goal.name}</p>
          <p className="flex items-center gap-1 text-xs text-muted-foreground">
            <CalendarDays className="h-3 w-3" /> by {format(parseISO(goal.targetDate), "d MMM yyyy")}
          </p>
        </div>
        <div className="flex items-center gap-1">
          <span className={cn("rounded-full px-2 py-0.5 text-[11px] font-semibold", status.className)}>{status.label}</span>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon" className="h-8 w-8" aria-label={`Actions for ${goal.name}`}>
                <MoreHorizontal className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onSelect={onEdit} className="gap-2">
                <Pencil className="h-4 w-4" /> Edit / update savings
              </DropdownMenuItem>
              <DropdownMenuItem onSelect={onDelete} className="gap-2 text-expense">
                <Trash2 className="h-4 w-4" /> Delete
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      <div className="mt-5 flex items-center gap-5">
        <ProgressRing percent={goal.percentComplete} achieved={goal.achieved} />
        <div className="space-y-1 text-sm">
          <p>
            <span className="font-display text-xl font-bold tabular-nums">{formatter.format(goal.currentAmount)}</span>
          </p>
          <p className="text-muted-foreground">of {formatter.format(goal.targetAmount)}</p>
          {!goal.achieved && (
            <p className="pt-1 text-muted-foreground">
              {goal.overdue ? (
                <span className="text-expense">{formatter.format(goal.remaining)} still to go</span>
              ) : (
                <>
                  <span className="font-semibold text-primary">{formatter.format(goal.requiredMonthlyContribution)}</span>/mo ·{" "}
                  {goal.monthsRemaining} {goal.monthsRemaining === 1 ? "month" : "months"}
                </>
              )}
            </p>
          )}
        </div>
      </div>
    </SpotlightCard>
  );
}
