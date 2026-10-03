import { moneyAmount } from "@/schema/budget";
import { z } from "zod";

/** Form validation for the goal dialog; mirrors GoalRequest. */
export const GoalFormSchema = z.object({
  name: z.string().trim().min(1, "Name is required").max(100, "At most 100 characters"),
  targetAmount: moneyAmount,
  currentAmount: z.coerce
    .number({ invalid_type_error: "Enter an amount" })
    .min(0, "Cannot be negative")
    .multipleOf(0.01, "At most 2 decimals")
    .max(999_999_999_999.99, "Amount is too large"),
  targetDate: z.date({ required_error: "Pick a target date" }),
});

export type GoalFormValues = z.infer<typeof GoalFormSchema>;
