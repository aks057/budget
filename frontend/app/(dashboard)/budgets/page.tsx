"use client";

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
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Progress } from "@/components/ui/progress";
import { ApiError } from "@/lib/api/client";
import { deleteBudget, getBudgets } from "@/lib/api/endpoints";
import type { BudgetDto, BudgetLevel } from "@/lib/api/types";
import { GetFormatterForCurrency } from "@/lib/helpers";
import { cn } from "@/lib/utils";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { addMonths, format, isSameMonth } from "date-fns";
import { ChevronLeft, ChevronRight, MoreHorizontal, Pencil, PiggyBank, Plus, Sparkles, Trash2 } from "lucide-react";
import Link from "next/link";
import { useMemo, useState } from "react";
import { toast } from "sonner";
import { BudgetDialog } from "./_components/BudgetDialog";

const LEVEL_STYLES: Record<BudgetLevel, { label: string; badge: string; bar: string }> = {
  ON_TRACK: { label: "On track", badge: "bg-emerald-500/10 text-emerald-500", bar: "bg-emerald-500" },
  WARNING: { label: "Close to limit", badge: "bg-amber-500/10 text-amber-500", bar: "bg-amber-500" },
  EXCEEDED: { label: "Over budget", badge: "bg-red-500/10 text-red-500", bar: "bg-red-500" },
};

export default function BudgetsPage() {
  const user = useCurrentUser();
  const formatter = useMemo(() => GetFormatterForCurrency(user.currency), [user.currency]);
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
    return {
      budgeted: list.reduce((sum, budget) => sum + budget.amount, 0),
      spent: list.reduce((sum, budget) => sum + budget.spent, 0),
      over: list.filter((budget) => budget.level === "EXCEEDED").length,
    };
  }, [budgets.data]);

  return (
    <>
      <div className="border-b bg-card">
        <div className="container flex flex-wrap items-center justify-between gap-6 py-8">
          <div>
            <p className="text-3xl font-bold">Budgets</p>
            <p className="text-muted-foreground">Monthly spending limits per category</p>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <Button asChild variant="outline" className="gap-2">
              <Link href={`/ai?q=${encodeURIComponent("Suggest budgets for me based on my spending")}`}>
                <Sparkles className="h-4 w-4" /> Suggest with AI
              </Link>
            </Button>
            <Button className="gap-2" onClick={() => setDialog({ open: true })}>
              <Plus className="h-4 w-4" /> New budget
            </Button>
          </div>
        </div>
      </div>

      <div className="container flex flex-col gap-6 py-6">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <Button variant="outline" size="icon" aria-label="Previous month" onClick={() => setMonth((m) => addMonths(m, -1))}>
              <ChevronLeft className="h-4 w-4" />
            </Button>
            <p className="min-w-[9rem] text-center font-medium">{format(month, "MMMM yyyy")}</p>
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
          {(budgets.data?.length ?? 0) > 0 && (
            <p className="text-sm text-muted-foreground">
              Spent <span className="font-medium text-foreground">{formatter.format(totals.spent)}</span> of{" "}
              {formatter.format(totals.budgeted)} budgeted
              {totals.over > 0 && <span className="text-red-500"> · {totals.over} over budget</span>}
            </p>
          )}
        </div>

        {budgets.isError && <p className="text-sm text-red-500">Could not load budgets.</p>}

        <SkeletonWrapper isLoading={budgets.isLoading}>
          {budgets.data?.length === 0 ? (
            <Card className="flex flex-col items-center gap-3 py-12 text-center">
              <PiggyBank className="h-12 w-12 text-muted-foreground/50" />
              <p className="text-lg font-medium">No budgets yet</p>
              <p className="max-w-sm text-sm text-muted-foreground">
                Set a monthly limit for a category, or ask the assistant to suggest limits from your spending.
              </p>
              <Button onClick={() => setDialog({ open: true })}>Create your first budget</Button>
            </Card>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {budgets.data?.map((budget) => (
                <BudgetCard
                  key={budget.id}
                  budget={budget}
                  formatter={formatter}
                  onEdit={() => setDialog({ open: true, budget })}
                  onDelete={() => setToDelete(budget)}
                />
              ))}
            </div>
          )}
        </SkeletonWrapper>
      </div>

      <BudgetDialog
        open={dialog.open}
        budget={dialog.budget}
        onOpenChange={(open) => setDialog((prev) => ({ ...prev, open }))}
      />

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
    </>
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
    <Card>
      <CardHeader className="flex flex-row items-start justify-between space-y-0 pb-2">
        <CardTitle className="flex items-center gap-2 text-base">
          <span className="text-2xl" role="img" aria-hidden>
            {budget.categoryIcon}
          </span>
          {budget.categoryName}
        </CardTitle>
        <div className="flex items-center gap-1">
          <Badge variant="outline" className={cn("border-0", style.badge)}>
            {style.label}
          </Badge>
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
              <DropdownMenuItem onSelect={onDelete} className="gap-2 text-red-500">
                <Trash2 className="h-4 w-4" /> Delete
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </CardHeader>
      <CardContent className="space-y-2">
        <Progress
          value={Math.min(budget.percentUsed, 100)}
          indicator={style.bar}
          className="h-2"
          aria-label={`${budget.percentUsed}% of budget used`}
        />
        <div className="flex justify-between text-sm">
          <span>
            <span className="font-semibold">{formatter.format(budget.spent)}</span>
            <span className="text-muted-foreground"> of {formatter.format(budget.amount)}</span>
          </span>
          <span className="tabular-nums text-muted-foreground">{Math.round(budget.percentUsed)}%</span>
        </div>
        <p className={cn("text-sm", over ? "text-red-500" : "text-muted-foreground")}>
          {over
            ? `${formatter.format(-budget.remaining)} over budget`
            : `${formatter.format(budget.remaining)} left`}
        </p>
      </CardContent>
    </Card>
  );
}
