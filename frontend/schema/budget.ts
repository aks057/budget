import { z } from "zod";

/** Money input: positive, 2 decimals, within the backend's @Digits(integer = 12, fraction = 2). */
export const moneyAmount = z.coerce
  .number({ invalid_type_error: "Enter an amount" })
  .positive("Amount must be greater than 0")
  .multipleOf(0.01, "At most 2 decimals")
  .max(999_999_999_999.99, "Amount is too large");

/** Form validation for the budget dialog; mirrors CreateBudgetRequest / UpdateBudgetRequest. */
export const BudgetFormSchema = z.object({
  categoryId: z.string({ required_error: "Pick a category" }).min(1, "Pick a category"),
  amount: moneyAmount,
});

export type BudgetFormValues = z.infer<typeof BudgetFormSchema>;
