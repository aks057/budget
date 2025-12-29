"use server";

import { createClient } from "@/lib/supabase/server";
import { requireUser } from "@/lib/supabase/auth";
import { UpdateUserCurrencySchema } from "@/schema/userSettings";

export async function UpdateUserCurrency(currency: string) {
  const parsedBody = UpdateUserCurrencySchema.safeParse({ currency });

  if (!parsedBody.success) {
    throw parsedBody.error;
  }

  const user = await requireUser();
  const supabase = await createClient();

  const { data, error } = await supabase
    .from("user_settings")
    .upsert({
      user_id: user.id,
      currency,
    })
    .select()
    .single();

  if (error) {
    throw new Error(error.message);
  }

  return data;
}
