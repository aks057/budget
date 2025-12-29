"use server";

import { createClient } from "@/lib/supabase/server";
import { requireUser } from "@/lib/supabase/auth";
import {
  CreateTransactionSchema,
  CreateTransactionSchemaType,
} from "@/schema/transaction";

export async function CreateTransaction(form: CreateTransactionSchemaType) {
  const parsedBody = CreateTransactionSchema.safeParse(form);
  if (!parsedBody.success) {
    throw new Error(parsedBody.error.message);
  }

  const user = await requireUser();
  const supabase = await createClient();

  const { amount, category, date, description, type } = parsedBody.data;

  // Get category to verify it exists and get icon
  const { data: categoryRow, error: categoryError } = await supabase
    .from("categories")
    .select("*")
    .eq("user_id", user.id)
    .eq("name", category)
    .single();

  if (categoryError || !categoryRow) {
    throw new Error("category not found");
  }

  // Call the atomic function
  const { data, error } = await supabase.rpc("create_transaction_with_history", {
    p_user_id: user.id,
    p_amount: amount,
    p_description: description || "",
    p_date: date.toISOString(),
    p_type: type,
    p_category: categoryRow.name,
    p_category_icon: categoryRow.icon,
  });

  if (error) {
    throw new Error(error.message);
  }

  return data;
}
