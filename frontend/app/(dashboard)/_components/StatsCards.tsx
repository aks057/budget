"use client";

import SkeletonWrapper from "@/components/SkeletonWrapper";
import { Card } from "@/components/ui/card";
import { getOverview, toApiDate } from "@/lib/api/endpoints";
import { GetFormatterForCurrency } from "@/lib/helpers";
import { cn } from "@/lib/utils";
import { useQuery } from "@tanstack/react-query";
import { TrendingDown, TrendingUp, Wallet } from "lucide-react";
import React, { ReactNode, useCallback, useMemo } from "react";
import CountUp from "react-countup";

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

  const formatter = useMemo(() => GetFormatterForCurrency(currency), [currency]);

  const income = statsQuery.data?.income ?? 0;
  const expense = statsQuery.data?.expense ?? 0;
  const balance = statsQuery.data?.balance ?? 0;

  return (
    <div className="relative flex w-full flex-wrap gap-4 md:flex-nowrap">
      <SkeletonWrapper isLoading={statsQuery.isFetching}>
        <StatCard
          formatter={formatter}
          value={income}
          title="Income"
          description="Total earnings"
          icon={<TrendingUp className="h-5 w-5 text-emerald-500" />}
          iconBg="bg-emerald-500/10"
          valueColor="text-emerald-500"
        />
      </SkeletonWrapper>

      <SkeletonWrapper isLoading={statsQuery.isFetching}>
        <StatCard
          formatter={formatter}
          value={expense}
          title="Expense"
          description="Total spending"
          icon={<TrendingDown className="h-5 w-5 text-red-500" />}
          iconBg="bg-red-500/10"
          valueColor="text-red-500"
        />
      </SkeletonWrapper>

      <SkeletonWrapper isLoading={statsQuery.isFetching}>
        <StatCard
          formatter={formatter}
          value={balance}
          title="Balance"
          description="Net savings"
          icon={<Wallet className="h-5 w-5 text-amber-500" />}
          iconBg="bg-amber-500/10"
          valueColor={balance >= 0 ? "text-emerald-500" : "text-red-500"}
        />
      </SkeletonWrapper>
    </div>
  );
}

export default StatsCards;

function StatCard({
  formatter,
  value,
  title,
  description,
  icon,
  iconBg,
  valueColor,
}: {
  formatter: Intl.NumberFormat;
  icon: ReactNode;
  iconBg: string;
  title: string;
  description: string;
  value: number;
  valueColor?: string;
}) {
  const formatFn = useCallback((value: number) => formatter.format(value), [formatter]);

  return (
    <Card className="flex h-auto w-full flex-col gap-4 p-6 transition-all hover:shadow-lg hover:shadow-black/5 dark:hover:shadow-white/5">
      <div className="flex items-center justify-between">
        <p className="text-sm font-medium text-muted-foreground">{title}</p>
        <div className={cn("rounded-lg p-2", iconBg)}>{icon}</div>
      </div>
      <div className="flex flex-col gap-1">
        <CountUp
          preserveValue
          redraw={false}
          end={value}
          decimals={2}
          formattingFn={formatFn}
          className={cn("text-2xl font-bold", valueColor)}
        />
        <p className="text-xs text-muted-foreground">{description}</p>
      </div>
    </Card>
  );
}
