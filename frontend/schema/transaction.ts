import { z } from "zod";

/** Form validation for the create-transaction dialog. The transaction type is derived from the category server-side. */
export const CreateTransactionSchema = z.object({
  amount: z.coerce.number().positive("Amount must be greater than 0").multipleOf(0.01),
  description: z.string().max(255).optional(),
  date: z.coerce.date(),
  categoryId: z.string({ required_error: "Pick a category" }).min(1, "Pick a category"),
});

export type CreateTransactionSchemaType = z.infer<typeof CreateTransactionSchema>;
