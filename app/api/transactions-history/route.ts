import { GetFormatterForCurrency } from "@/lib/helpers";
import { createClient } from "@/lib/supabase/server";
import { requireUser } from "@/lib/supabase/auth";
import { OverviewQuerySchema } from "@/schema/overview";
import { Transaction } from "@/lib/supabase/database.types";

export async function GET(request: Request) {
  const user = await requireUser();
  const supabase = await createClient();

  const { searchParams } = new URL(request.url);
  const from = searchParams.get("from");
  const to = searchParams.get("to");

  const queryParams = OverviewQuerySchema.safeParse({
    from,
    to,
  });

  if (!queryParams.success) {
    return Response.json(queryParams.error.message, {
      status: 400,
    });
  }

  // Get user settings for currency formatting
  const { data: userSettings, error: settingsError } = await supabase
    .from("user_settings")
    .select("currency")
    .eq("user_id", user.id)
    .single();

  if (settingsError || !userSettings) {
    throw new Error("user settings not found");
  }

  const formatter = GetFormatterForCurrency(userSettings.currency);

  // Get transactions
  const { data: transactions, error } = await supabase
    .from("transactions")
    .select("*")
    .eq("user_id", user.id)
    .gte("date", queryParams.data.from.toISOString())
    .lte("date", queryParams.data.to.toISOString())
    .order("date", { ascending: false });

  if (error) {
    return Response.json({ error: error.message }, { status: 500 });
  }

  return Response.json(
    (transactions || []).map((transaction) => ({
      ...transaction,
      formattedAmount: formatter.format(transaction.amount),
    }))
  );
}

export type GetTransactionHistoryResponseType = (Transaction & {
  formattedAmount: string;
})[];
