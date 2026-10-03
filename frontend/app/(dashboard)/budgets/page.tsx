"use client";

import { useAiPanel } from "@/components/agent/AiPanelProvider";
import { EmptyState } from "@/components/EmptyState";
import { AnimatedNumber, EASE_OUT, Reveal, SpotlightCard, Stagger, StaggerItem } from "@/components/motion";
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
import { deleteBudget, getBudgets } from "@/lib/api/endpoints";
import type { BudgetDto, BudgetLevel } from "@/lib/api/types";
import { GetFormatterForCurrency } from "@/lib/helpers";
import { cn } from "@/lib/utils";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { AnimatePresence, motion } from "framer-motion";
import { addMonths, format, isSameMonth } from "date-fns";
import { ChevronLeft, ChevronRight, MoreHorizontal, Pencil, PiggyBank, Plus, Sparkles, Trash2 } from "lucide-react";
import { useMemo, useState } from "react";
import { toast } from "sonner";
import { BudgetDialog } from "./_components/BudgetDialog";

const LEVEL_STYLES: Record<BudgetLevel, { label: string; badge: string; bar: string }> = {
  ON_TRACK: { label: "On track", badge: "bg-income/10 text-income", bar: "bg-brand-gradient" },
  WARNING: { label: "Close to limit", badge: "bg-warning/10 text-warning", bar: "bg-warning" },
  EXCEEDED: { label: "Over budget", badge: "bg-expense/10 text-expense", bar: "bg-expense" },
};

export default function BudgetsPage() {
  const user = useCurrentUser();
  const { ask } = useAiPanel();
  const formatter = useMemo(() => GetFormatterForCurrency(user.currency), [user.currency]);
  const formatMoney = useMemo(() => (value: number) => formatter.format(value), [formatter]);
  const [month, setMonth] = useState(() => new Date());
  const monthKey = format(month, "yyyy-MM");
  const isCurrentMonth = isSameMonth(month, new Date());

  const [dialog, setDialog] = useState<{ open: boolean; budget?: BudgetDto }>({ open: false });
  const [toDelete, setToDelete] = useState<BudgetDto | null>(null);

  const budgets = useQuery({ queryKey: ["budgets", monthKey], queryFn: () => getBudgets(monthKey) });

  const queryClient = useQueryClient();
  const deleteMutation = useMutation({
    mutationFn: (id: string) => deleteBudget(id),
    onSuccess: () => {
      toast.success("Budget deleted");
      queryClient.invalidateQueries({ queryKey: ["budgets"] });
      queryClient.invalidateQueries({ queryKey: ["overview", "insights"] });
    },
    onError: (error) => toast.error(error instanceof ApiError ? error.userMessage : "Could not delete the budget"),
  });

  const totals = useMemo(() => {
    const list = budgets.data ?? [];
    const budgeted = list.reduce((sum, budget) => sum + budget.amount, 0);
    const spent = list.reduce((sum, budget) => sum + budget.spent, 0);
    return {
      budgeted,
      spent,
      usedPct: budgeted > 0 ? (spent / budgeted) * 100 : 0,
      over: list.filter((budget) => budget.level === "EXCEEDED").length,
      warning: list.filter((budget) => budget.level === "WARNING").length,
    };
  }, [budgets.data]);

  const hasBudgets = (budgets.data?.length ?? 0) > 0;

  return (
    <div className="pb-16">
      <PageHeader
        icon={PiggyBank}
        title="Budgets"
        subtitle="Monthly spending limits per category"
        actions={
          <>
            <Button variant="outline" className="gap-2" onClick={() => ask("Suggest budgets for me based on my spending")}>
              <Sparkles className="h-4 w-4 text-primary" /> Suggest with AI
            </Button>
            <Button className="gap-2" onClick={() => setDialog({ open: true })}>
              <Plus className="h-4 w-4" /> New budget
            </Button>
          </>
        }
      />

      <div className="container flex flex-col gap-6">
        {/* Month switcher + summary */}
        <Reveal onMount delay={0.1}>
          <SpotlightCard className="flex flex-wrap items-center justify-between gap-6 p-5">
            <div className="flex items-center gap-2">
              <Button variant="outline" size="icon" aria-label="Previous month" onClick={() => setMonth((m) => addMonths(m, -1))}>
                <ChevronLeft className="h-4 w-4" />
              </Button>
              <div className="relative h-7 min-w-[10rem] overflow-hidden text-center">
                <AnimatePresence mode="popLayout" initial={false}>
                  <motion.p
                    key={monthKey}
                    initial={{ y: 20, opacity: 0 }}
                    animate={{ y: 0, opacity: 1 }}
                    exit={{ y: -20, opacity: 0 }}
                    transition={{ duration: 0.3 }}
                    className="font-display text-lg font-semibold"
                  >
                    {format(month, "MMMM yyyy")}
                  </motion.p>
                </AnimatePresence>
              </div>
              <Button
                variant="outline"
                size="icon"
                aria-label="Next month"
                disabled={isCurrentMonth}
                onClick={() => setMonth((m) => addMonths(m, 1))}
              >
                <ChevronRight className="h-4 w-4" />
              </Button>
            </div>

            {hasBudgets && (
              <div className="flex flex-wrap items-center gap-6">
                <UsageRing percent={totals.usedPct} />
                <div>
                  <p className="text-xs uppercase tracking-wider text-muted-foreground">Spent of budgeted</p>
                  <p className="font-display text-xl font-bold">
                    <AnimatedNumber value={totals.spent} format={formatMoney} />{" "}
                    <span className="text-base font-normal text-muted-foreground">/ {formatter.format(totals.budgeted)}</span>
                  </p>
                  <p className="text-xs text-muted-foreground">
                    {totals.over > 0 && <span className="text-expense">{totals.over} over budget</span>}
                    {totals.over > 0 && totals.warning > 0 && " · "}
                    {totals.warning > 0 && <span className="text-warning">{totals.warning} close to limit</span>}
                    {totals.over === 0 && totals.warning === 0 && <span className="text-income">All on track</span>}
                  </p>
                </div>
              </div>
            )}
          </SpotlightCard>
        </Reveal>

        {budgets.isError && <p className="text-sm text-expense">Could not load budgets.</p>}

        <SkeletonWrapper isLoading={budgets.isLoading}>
          {budgets.data?.length === 0 ? (
            <EmptyState
              icon={PiggyBank}
              title="No budgets yet"
              description="Set a monthly limit for a category, or ask the assistant to suggest limits from your spending."
              action={<Button onClick={() => setDialog({ open: true })}>Create your first budget</Button>}
            />
          ) : (
            <Stagger key={monthKey} onMount className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {budgets.data?.map((budget) => (
                <StaggerItem key={budget.id}>
                  <BudgetCard
                    budget={budget}
                    formatter={formatter}
                    onEdit={() => setDialog({ open: true, budget })}
                    onDelete={() => setToDelete(budget)}
                  />
                </StaggerItem>
              ))}
            </Stagger>
          )}
        </SkeletonWrapper>
      </div>

      <BudgetDialog open={dialog.open} budget={dialog.budget} onOpenChange={(open) => setDialog((prev) => ({ ...prev, open }))} />

      <AlertDialog open={toDelete !== null} onOpenChange={(open) => !open && setToDelete(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete the {toDelete?.categoryName} budget?</AlertDialogTitle>
            <AlertDialogDescription>Your transactions are not affected.</AlertDialogDescription>
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

function UsageRing({ percent }: { percent: number }) {
  const radius = 26;
  const circumference = 2 * Math.PI * radius;
  const clamped = Math.min(percent, 100);
  const tone = percent > 100 ? "#fb7185" : percent >= 80 ? "#f5b94d" : "url(#usage-gradient)";
  return (
    <div className="relative h-16 w-16">
      <svg viewBox="0 0 64 64" className="h-16 w-16 -rotate-90">
        <circle cx="32" cy="32" r={radius} fill="none" stroke="hsl(var(--muted))" strokeWidth="6" />
        <motion.circle
          cx="32"
          cy="32"
          r={radius}
          fill="none"
          stroke={tone}
          strokeWidth="6"
          strokeLinecap="round"
          strokeDasharray={circumference}
          initial={{ strokeDashoffset: circumference }}
          animate={{ strokeDashoffset: circumference * (1 - clamped / 100) }}
          transition={{ duration: 1.1, ease: EASE_OUT }}
        />
        <defs>
          <linearGradient id="usage-gradient" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="#34d399" />
            <stop offset="100%" stopColor="#22d3ee" />
          </linearGradient>
        </defs>
      </svg>
      <span className="absolute inset-0 flex items-center justify-center text-xs font-bold tabular-nums">{Math.round(percent)}%</span>
    </div>
  );
}

function BudgetCard({
  budget,
  formatter,
  onEdit,
  onDelete,
}: {
  budget: BudgetDto;
  formatter: Intl.NumberFormat;
  onEdit: () => void;
  onDelete: () => void;
}) {
  const style = LEVEL_STYLES[budget.level];
  const over = budget.remaining < 0;
  return (
    <SpotlightCard className={cn("h-full p-5", over && "border-expense/40")}>
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-center gap-3">
          <span className="flex h-11 w-11 items-center justify-center rounded-xl bg-muted text-2xl" role="img" aria-hidden>
            {budget.categoryIcon}
          </span>
          <div>
            <p className="font-display font-semibold">{budget.categoryName}</p>
            <span className={cn("inline-flex rounded-full px-2 py-0.5 text-[11px] font-semibold", style.badge)}>{style.label}</span>
          </div>
        </div>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon" className="h-8 w-8" aria-label={`Actions for ${budget.categoryName}`}>
              <MoreHorizontal className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem onSelect={onEdit} className="gap-2">
              <Pencil className="h-4 w-4" /> Edit limit
            </DropdownMenuItem>
            <DropdownMenuItem onSelect={onDelete} className="gap-2 text-expense">
              <Trash2 className="h-4 w-4" /> Delete
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      <div className="mt-5 flex items-baseline justify-between">
        <p className="font-display text-2xl font-bold tabular-nums">{formatter.format(budget.spent)}</p>
        <p className="text-sm text-muted-foreground">of {formatter.format(budget.amount)}</p>
      </div>
      <div className="mt-2 h-2 overflow-hidden rounded-full bg-muted" aria-label={`${budget.percentUsed}% of budget used`}>
        <motion.div
          className={cn("h-full rounded-full", style.bar)}
          initial={{ width: 0 }}
          animate={{ width: `${Math.min(budget.percentUsed, 100)}%` }}
          transition={{ duration: 0.9, ease: EASE_OUT, delay: 0.15 }}
        />
      </div>
      <p className={cn("mt-2 flex justify-between text-sm", over ? "text-expense" : "text-muted-foreground")}>
        <span>{over ? `${formatter.format(-budget.remaining)} over` : `${formatter.format(budget.remaining)} left`}</span>
        <span className="tabular-nums">{Math.round(budget.percentUsed)}%</span>
      </p>
    </SpotlightCard>
  );
}
