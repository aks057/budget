"use client";

import { Sparkline } from "@/components/charts/Sparkline";
import { AnimatedNumber, SpotlightCard, Stagger, StaggerItem } from "@/components/motion";
import SkeletonWrapper from "@/components/SkeletonWrapper";
import { getOverview, getTrends, toApiDate } from "@/lib/api/endpoints";
import { EXPENSE_COLOR, INCOME_COLOR } from "@/lib/chartColors";
import { GetFormatterForCurrency } from "@/lib/helpers";
import { cn } from "@/lib/utils";
import { useQuery } from "@tanstack/react-query";
import { TrendingDown, TrendingUp, Wallet, type LucideIcon } from "lucide-react";
import { useMemo } from "react";

const TREND_MONTHS = 6;
const BALANCE_COLOR = "#22d3ee"; // brand-2 (cyan)

interface Props {
  from: Date;
  to: Date;
  currency: string;
}

function StatsCards({ from, to, currency }: Props) {
  const statsQuery = useQuery({
    queryKey: ["overview", "stats", toApiDate(from), toApiDate(to)],
    queryFn: () => getOverview(from, to),
  });
  // Under "overview" so creating/deleting a transaction refreshes the sparklines too.
  const trendsQuery = useQuery({
    queryKey: ["overview", "trends", TREND_MONTHS],
    queryFn: () => getTrends(TREND_MONTHS),
  });

  const formatter = useMemo(() => GetFormatterForCurrency(currency), [currency]);
  const format = useMemo(() => (value: number) => formatter.format(value), [formatter]);

  const trends = trendsQuery.data ?? [];
  const income = statsQuery.data?.income ?? 0;
  const expense = statsQuery.data?.expense ?? 0;
  const balance = statsQuery.data?.balance ?? 0;

  return (
    <SkeletonWrapper isLoading={statsQuery.isLoading}>
      <Stagger onMount className="grid w-full gap-4 md:grid-cols-3">
        <StaggerItem>
          <StatCard
            title="Income"
            icon={TrendingUp}
            tone="text-income"
            tile="bg-income/10 border-income/20"
            value={income}
            format={format}
            trend={trends.map((month) => month.income)}
            color={INCOME_COLOR}
          />
        </StaggerItem>
        <StaggerItem>
          <StatCard
            title="Expenses"
            icon={TrendingDown}
            tone="text-expense"
            tile="bg-expense/10 border-expense/20"
            value={expense}
            format={format}
            trend={trends.map((month) => month.expense)}
            color={EXPENSE_COLOR}
          />
        </StaggerItem>
        <StaggerItem>
          <StatCard
            title="Balance"
            icon={Wallet}
            tone={balance >= 0 ? "text-foreground" : "text-expense"}
            tile="bg-brand-2/10 border-brand-2/20"
            iconTone="text-brand-2"
            value={balance}
            format={format}
            trend={trends.map((month) => month.savings)}
            color={BALANCE_COLOR}
          />
        </StaggerItem>
      </Stagger>
    </SkeletonWrapper>
  );
}

export default StatsCards;

function StatCard({
  title,
  icon: Icon,
  tone,
  tile,
  iconTone,
  value,
  format,
  trend,
  color,
}: {
  title: string;
  icon: LucideIcon;
  tone: string;
  tile: string;
  iconTone?: string;
  value: number;
  format: (value: number) => string;
  trend: number[];
  color: string;
}) {
  return (
    <SpotlightCard className="h-full p-5">
      <div className="flex items-center justify-between">
        <p className="text-sm font-medium text-muted-foreground">{title}</p>
        <span className={cn("flex h-9 w-9 items-center justify-center rounded-xl border", tile)}>
          <Icon className={cn("h-4 w-4", iconTone ?? tone)} />
        </span>
      </div>
      <AnimatedNumber value={value} format={format} className={cn("mt-3 block font-display text-3xl font-bold tracking-tight", tone)} />
      <div className="mt-3">
        <Sparkline values={trend} color={color} />
        <p className="mt-1 text-[11px] uppercase tracking-wider text-muted-foreground">Last {TREND_MONTHS} months</p>
      </div>
    </SpotlightCard>
  );
}
