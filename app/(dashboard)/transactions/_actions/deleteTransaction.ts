"use server";

import { createClient } from "@/lib/supabase/server";
import { requireUser } from "@/lib/supabase/auth";

export async function DeleteTransaction(id: string) {
  const user = await requireUser();
  const supabase = await createClient();

  const { data, error } = await supabase.rpc("delete_transaction_with_history", {
    p_transaction_id: id,
    p_user_id: user.id,
  });

  if (error) {
    throw new Error(error.message);
  }

  if (!data) {
    throw new Error("Transaction not found or unauthorized");
  }
}
