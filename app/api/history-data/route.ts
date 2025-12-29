import { createClient } from "@/lib/supabase/server";
import { requireUser } from "@/lib/supabase/auth";
import { Period, Timeframe } from "@/lib/types";
import { getDaysInMonth } from "date-fns";
import { z } from "zod";

const getHistoryDataSchema = z.object({
  timeframe: z.enum(["month", "year"]),
  month: z.coerce.number().min(0).max(11).default(0),
  year: z.coerce.number().min(2000).max(3000),
});

export async function GET(request: Request) {
  const user = await requireUser();
  const supabase = await createClient();

  const { searchParams } = new URL(request.url);
  const timeframe = searchParams.get("timeframe");
  const year = searchParams.get("year");
  const month = searchParams.get("month");

  const queryParams = getHistoryDataSchema.safeParse({
    timeframe,
    month,
    year,
  });

  if (!queryParams.success) {
    return Response.json(queryParams.error.message, {
      status: 400,
    });
  }

  const data = await getHistoryData(
    supabase,
    user.id,
    queryParams.data.timeframe,
    {
      month: queryParams.data.month,
      year: queryParams.data.year,
    }
  );

  return Response.json(data);
}

export type GetHistoryDataResponseType = HistoryData[];

type HistoryData = {
  expense: number;
  income: number;
  year: number;
  month: number;
  day?: number;
};

async function getHistoryData(
  supabase: Awaited<ReturnType<typeof createClient>>,
  userId: string,
  timeframe: Timeframe,
  period: Period
) {
  switch (timeframe) {
    case "year":
      return await getYearHistoryData(supabase, userId, period.year);
    case "month":
      return await getMonthHistoryData(
        supabase,
        userId,
        period.year,
        period.month
      );
  }
}

async function getYearHistoryData(
  supabase: Awaited<ReturnType<typeof createClient>>,
  userId: string,
  year: number
) {
  const { data: result, error } = await supabase
    .from("year_history")
    .select("month, income, expense")
    .eq("user_id", userId)
    .eq("year", year)
    .order("month", { ascending: true });

  if (error || !result || result.length === 0) return [];

  const history: HistoryData[] = [];

  for (let i = 0; i < 12; i++) {
    const month = result.find((row) => row.month === i);
    history.push({
      year,
      month: i,
      expense: month?.expense || 0,
      income: month?.income || 0,
    });
  }

  return history;
}

async function getMonthHistoryData(
  supabase: Awaited<ReturnType<typeof createClient>>,
  userId: string,
  year: number,
  month: number
) {
  const { data: result, error } = await supabase
    .from("month_history")
    .select("day, income, expense")
    .eq("user_id", userId)
    .eq("year", year)
    .eq("month", month)
    .order("day", { ascending: true });

  if (error || !result || result.length === 0) return [];

  const history: HistoryData[] = [];
  const daysInMonth = getDaysInMonth(new Date(year, month));

  for (let i = 1; i <= daysInMonth; i++) {
    const day = result.find((row) => row.day === i);
    history.push({
      expense: day?.expense || 0,
      income: day?.income || 0,
      year,
      month,
      day: i,
    });
  }

  return history;
}
