"use client";

import CategoryPicker from "@/app/(dashboard)/_components/CategoryPicker";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { ApiError } from "@/lib/api/client";
import { createBudget, updateBudget } from "@/lib/api/endpoints";
import type { BudgetDto } from "@/lib/api/types";
import { BudgetFormSchema, BudgetFormValues } from "@/schema/budget";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Loader2 } from "lucide-react";
import { useCallback, useEffect } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  /** Edit mode when set; only the amount can change (a budget belongs to one category). */
  budget?: BudgetDto;
}

export function BudgetDialog({ open, onOpenChange, budget }: Props) {
  const queryClient = useQueryClient();
  const form = useForm<BudgetFormValues>({
    resolver: zodResolver(BudgetFormSchema),
    defaultValues: { categoryId: budget?.categoryId ?? "", amount: budget?.amount },
  });

  useEffect(() => {
    if (open) form.reset({ categoryId: budget?.categoryId ?? "", amount: budget?.amount });
  }, [budget, form, open]);

  const mutation = useMutation({
    mutationFn: (values: BudgetFormValues) =>
      budget ? updateBudget(budget.id, values.amount) : createBudget(values),
    onSuccess: (saved) => {
      toast.success(budget ? "Budget updated" : `Budget created for ${saved.categoryName}`);
      queryClient.invalidateQueries({ queryKey: ["budgets"] });
      queryClient.invalidateQueries({ queryKey: ["overview", "insights"] });
      onOpenChange(false);
    },
    onError: (error) => {
      // e.g. 409 when the category already has a budget.
      toast.error(error instanceof ApiError ? error.userMessage : "Could not save the budget");
    },
  });

  const handleCategoryChange = useCallback(
    (categoryId: string) => form.setValue("categoryId", categoryId, { shouldValidate: true }),
    [form]
  );

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{budget ? `Edit ${budget.categoryName} budget` : "New monthly budget"}</DialogTitle>
          <DialogDescription>A standing monthly limit for one expense category.</DialogDescription>
        </DialogHeader>
        <Form {...form}>
          <form className="space-y-4" onSubmit={form.handleSubmit((values) => mutation.mutate(values))}>
            {!budget && (
              <FormField
                control={form.control}
                name="categoryId"
                render={() => (
                  <FormItem className="flex flex-col">
                    <FormLabel>Category</FormLabel>
                    <FormControl>
                      <CategoryPicker type="expense" onChange={handleCategoryChange} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            )}
            <FormField
              control={form.control}
              name="amount"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Monthly limit</FormLabel>
                  <FormControl>
                    <Input type="number" inputMode="decimal" step="0.01" min="0" {...field} value={field.value ?? ""} />
                  </FormControl>
                  <FormDescription>You will see a warning at 80% and an alert when it is exceeded.</FormDescription>
                  <FormMessage />
                </FormItem>
              )}
            />
            <DialogFooter>
              <Button type="button" variant="secondary" onClick={() => onOpenChange(false)}>
                Cancel
              </Button>
              <Button type="submit" disabled={mutation.isPending}>
                {mutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                {budget ? "Save" : "Create"}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
