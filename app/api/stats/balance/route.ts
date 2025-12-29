import { createClient } from "@/lib/supabase/server";
import { requireUser } from "@/lib/supabase/auth";
import { OverviewQuerySchema } from "@/schema/overview";

export async function GET(request: Request) {
  const user = await requireUser();
  const supabase = await createClient();

  const { searchParams } = new URL(request.url);
  const from = searchParams.get("from");
  const to = searchParams.get("to");

  const queryParams = OverviewQuerySchema.safeParse({ from, to });

  if (!queryParams.success) {
    return Response.json(queryParams.error.message, {
      status: 400,
    });
  }

  const { data: transactions, error } = await supabase
    .from("transactions")
    .select("type, amount")
    .eq("user_id", user.id)
    .gte("date", queryParams.data.from.toISOString())
    .lte("date", queryParams.data.to.toISOString());

  if (error) {
    return Response.json({ error: error.message }, { status: 500 });
  }

  // Aggregate in JavaScript (Supabase doesn't support groupBy like Prisma)
  const totals = (transactions || []).reduce(
    (acc, t) => {
      if (t.type === "expense") acc.expense += t.amount;
      if (t.type === "income") acc.income += t.amount;
      return acc;
    },
    { expense: 0, income: 0 }
  );

  return Response.json(totals);
}

export type GetBalanceStatsResponseType = {
  expense: number;
  income: number;
};
