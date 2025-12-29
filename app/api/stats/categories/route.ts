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
    throw new Error(queryParams.error.message);
  }

  const { data: transactions, error } = await supabase
    .from("transactions")
    .select("type, category, category_icon, amount")
    .eq("user_id", user.id)
    .gte("date", queryParams.data.from.toISOString())
    .lte("date", queryParams.data.to.toISOString());

  if (error) {
    return Response.json({ error: error.message }, { status: 500 });
  }

  // Group by type, category, categoryIcon and sum amounts
  const statsMap = new Map<
    string,
    {
      type: string;
      category: string;
      categoryIcon: string;
      _sum: { amount: number };
    }
  >();

  (transactions || []).forEach((t) => {
    const key = `${t.type}-${t.category}-${t.category_icon}`;
    if (statsMap.has(key)) {
      statsMap.get(key)!._sum.amount += t.amount;
    } else {
      statsMap.set(key, {
        type: t.type,
        category: t.category,
        categoryIcon: t.category_icon,
        _sum: { amount: t.amount },
      });
    }
  });

  const stats = Array.from(statsMap.values()).sort(
    (a, b) => b._sum.amount - a._sum.amount
  );

  return Response.json(stats);
}

export type GetCategoriesStatsResponseType = {
  type: string;
  category: string;
  categoryIcon: string;
  _sum: { amount: number };
}[];
