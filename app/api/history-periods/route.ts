import { createClient } from "@/lib/supabase/server";
import { requireUser } from "@/lib/supabase/auth";

export async function GET(request: Request) {
  const user = await requireUser();
  const supabase = await createClient();

  const { data: result, error } = await supabase
    .from("month_history")
    .select("year")
    .eq("user_id", user.id)
    .order("year", { ascending: true });

  if (error) {
    return Response.json({ error: error.message }, { status: 500 });
  }

  // Get distinct years
  const years = Array.from(new Set(result?.map((el) => el.year) || []));

  if (years.length === 0) {
    return Response.json([new Date().getFullYear()]);
  }

  return Response.json(years);
}

export type GetHistoryPeriodsResponseType = number[];
