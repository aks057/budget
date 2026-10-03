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
import { deleteGoal, getGoals } from "@/lib/api/endpoints";
import type { GoalDto } from "@/lib/api/types";
import { GetFormatterForCurrency } from "@/lib/helpers";
import { cn } from "@/lib/utils";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { format, parseISO } from "date-fns";
import { MoreHorizontal, Pencil, Plus, Sparkles, Target, Trash2 } from "lucide-react";
import Link from "next/link";
import { useMemo, useState } from "react";
import { toast } from "sonner";
import { GoalDialog } from "./_components/GoalDialog";

export default function GoalsPage() {
  const user = useCurrentUser();
  const formatter = useMemo(() => GetFormatterForCurrency(user.currency), [user.currency]);
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

  return (
    <>
      <div className="border-b bg-card">
        <div className="container flex flex-wrap items-center justify-between gap-6 py-8">
          <div>
            <p className="text-3xl font-bold">Goals</p>
            <p className="text-muted-foreground">Track what you are saving towards</p>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <Button asChild variant="outline" className="gap-2">
              <Link href={`/ai?q=${encodeURIComponent("Am I on track for my goals?")}`}>
                <Sparkles className="h-4 w-4" /> Ask AI
              </Link>
            </Button>
            <Button className="gap-2" onClick={() => setDialog({ open: true })}>
              <Plus className="h-4 w-4" /> New goal
            </Button>
          </div>
        </div>
      </div>

      <div className="container py-6">
        {goals.isError && <p className="text-sm text-red-500">Could not load goals.</p>}
        <SkeletonWrapper isLoading={goals.isLoading}>
          {goals.data?.length === 0 ? (
            <Card className="flex flex-col items-center gap-3 py-12 text-center">
              <Target className="h-12 w-12 text-muted-foreground/50" />
              <p className="text-lg font-medium">No goals yet</p>
              <p className="max-w-sm text-sm text-muted-foreground">
                Add a target like an emergency fund or a trip, and see how much to save each month.
              </p>
              <Button onClick={() => setDialog({ open: true })}>Create your first goal</Button>
            </Card>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {goals.data?.map((goal) => (
                <GoalCard
                  key={goal.id}
                  goal={goal}
                  formatter={formatter}
                  onEdit={() => setDialog({ open: true, goal })}
                  onDelete={() => setToDelete(goal)}
                />
              ))}
            </div>
          )}
        </SkeletonWrapper>
      </div>

      <GoalDialog
        open={dialog.open}
        goal={dialog.goal}
        onOpenChange={(open) => setDialog((prev) => ({ ...prev, open }))}
      />

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
    </>
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
    ? { label: "Achieved", className: "bg-emerald-500/10 text-emerald-500" }
    : goal.overdue
      ? { label: "Overdue", className: "bg-red-500/10 text-red-500" }
      : null;

  return (
    <Card>
      <CardHeader className="flex flex-row items-start justify-between space-y-0 pb-2">
        <div className="min-w-0">
          <CardTitle className="truncate text-base">{goal.name}</CardTitle>
          <p className="text-xs text-muted-foreground">by {format(parseISO(goal.targetDate), "d MMM yyyy")}</p>
        </div>
        <div className="flex items-center gap-1">
          {status && (
            <Badge variant="outline" className={cn("border-0", status.className)}>
              {status.label}
            </Badge>
          )}
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
              <DropdownMenuItem onSelect={onDelete} className="gap-2 text-red-500">
                <Trash2 className="h-4 w-4" /> Delete
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </CardHeader>
      <CardContent className="space-y-2">
        <Progress
          value={Math.min(goal.percentComplete, 100)}
          indicator={goal.achieved ? "bg-emerald-500" : "bg-amber-500"}
          className="h-2"
          aria-label={`${goal.percentComplete}% saved`}
        />
        <div className="flex justify-between text-sm">
          <span>
            <span className="font-semibold">{formatter.format(goal.currentAmount)}</span>
            <span className="text-muted-foreground"> of {formatter.format(goal.targetAmount)}</span>
          </span>
          <span className="tabular-nums text-muted-foreground">{Math.round(goal.percentComplete)}%</span>
        </div>
        {!goal.achieved && (
          <p className="text-sm text-muted-foreground">
            {goal.overdue ? (
              <>Target date passed with {formatter.format(goal.remaining)} to go.</>
            ) : (
              <>
                Save <span className="font-medium text-foreground">{formatter.format(goal.requiredMonthlyContribution)}</span>
                /month for {goal.monthsRemaining} {goal.monthsRemaining === 1 ? "month" : "months"}
              </>
            )}
          </p>
        )}
      </CardContent>
    </Card>
  );
}
