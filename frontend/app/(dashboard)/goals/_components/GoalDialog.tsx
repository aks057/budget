"use client";

import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { ApiError } from "@/lib/api/client";
import { createGoal, updateGoal } from "@/lib/api/endpoints";
import type { GoalDto } from "@/lib/api/types";
import { cn } from "@/lib/utils";
import { GoalFormSchema, GoalFormValues } from "@/schema/goal";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { addMonths, format, parseISO, startOfToday } from "date-fns";
import { CalendarIcon, Loader2 } from "lucide-react";
import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  goal?: GoalDto;
}

const defaultsFor = (goal?: GoalDto): Partial<GoalFormValues> =>
  goal
    ? {
        name: goal.name,
        targetAmount: goal.targetAmount,
        currentAmount: goal.currentAmount,
        targetDate: parseISO(goal.targetDate),
      }
    : { name: "", currentAmount: 0, targetDate: addMonths(startOfToday(), 12) };

export function GoalDialog({ open, onOpenChange, goal }: Props) {
  const queryClient = useQueryClient();
  const form = useForm<GoalFormValues>({ resolver: zodResolver(GoalFormSchema), defaultValues: defaultsFor(goal) });

  useEffect(() => {
    if (open) form.reset(defaultsFor(goal));
  }, [form, goal, open]);

  const mutation = useMutation({
    mutationFn: (values: GoalFormValues) => (goal ? updateGoal(goal.id, values) : createGoal(values)),
    onSuccess: (saved) => {
      toast.success(goal ? "Goal updated" : `Goal "${saved.name}" created`);
      queryClient.invalidateQueries({ queryKey: ["goals"] });
      onOpenChange(false);
    },
    onError: (error) => toast.error(error instanceof ApiError ? error.userMessage : "Could not save the goal"),
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{goal ? "Edit goal" : "New savings goal"}</DialogTitle>
          <DialogDescription>Bud-Wiser works out how much you need to save each month.</DialogDescription>
        </DialogHeader>
        <Form {...form}>
          <form className="space-y-4" onSubmit={form.handleSubmit((values) => mutation.mutate(values))}>
            <FormField
              control={form.control}
              name="name"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Name</FormLabel>
                  <FormControl>
                    <Input placeholder="Emergency fund" maxLength={100} {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <div className="grid gap-4 sm:grid-cols-2">
              <FormField
                control={form.control}
                name="targetAmount"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Target amount</FormLabel>
                    <FormControl>
                      <Input type="number" inputMode="decimal" step="0.01" min="0" {...field} value={field.value ?? ""} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="currentAmount"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Saved so far</FormLabel>
                    <FormControl>
                      <Input type="number" inputMode="decimal" step="0.01" min="0" {...field} value={field.value ?? ""} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>
            <FormField
              control={form.control}
              name="targetDate"
              render={({ field }) => (
                <FormItem className="flex flex-col">
                  <FormLabel>Target date</FormLabel>
                  <Popover>
                    <PopoverTrigger asChild>
                      <FormControl>
                        <Button
                          variant="outline"
                          className={cn("w-full justify-start font-normal", !field.value && "text-muted-foreground")}
                        >
                          <CalendarIcon className="mr-2 h-4 w-4 opacity-50" />
                          {field.value ? format(field.value, "PPP") : "Pick a date"}
                        </Button>
                      </FormControl>
                    </PopoverTrigger>
                    <PopoverContent className="w-auto p-0" align="start">
                      <Calendar
                        mode="single"
                        selected={field.value}
                        defaultMonth={field.value}
                        onSelect={(value) => value && field.onChange(value)}
                        initialFocus
                      />
                    </PopoverContent>
                  </Popover>
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
                {goal ? "Save" : "Create"}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
