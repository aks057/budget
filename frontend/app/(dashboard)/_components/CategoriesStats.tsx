"use client";

import { EmptyState } from "@/components/EmptyState";
import { EASE_OUT } from "@/components/motion";
import SkeletonWrapper from "@/components/SkeletonWrapper";
import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import { ScrollArea } from "@/components/ui/scroll-area";
import { CategoryStat, getCategoryStats, toApiDate } from "@/lib/api/endpoints";
import { GetFormatterForCurrency } from "@/lib/helpers";
import { TransactionType } from "@/lib/types";
import { cn } from "@/lib/utils";
import { useQuery } from "@tanstack/react-query";
import { motion } from "framer-motion";
import { PieChart, TrendingDown, TrendingUp } from "lucide-react";
import React, { useMemo } from "react";

interface Props {
  currency: string;
  from: Date;
  to: Date;
}

function CategoriesStats({ currency, from, to }: Props) {
  const formatter = useMemo(() => GetFormatterForCurrency(currency), [currency]);

  return (
    <div className="grid w-full gap-4 md:grid-cols-2">
      <CategoriesCard formatter={formatter} type="income" from={from} to={to} />
      <CategoriesCard formatter={formatter} type="expense" from={from} to={to} />
    </div>
  );
}

export default CategoriesStats;

function CategoriesCard({
  type,
  from,
  to,
  formatter,
}: {
  type: TransactionType;
  from: Date;
  to: Date;
  formatter: Intl.NumberFormat;
}) {
  const statsQuery = useQuery({
    queryKey: ["overview", "stats", "categories", type, toApiDate(from), toApiDate(to)],
    queryFn: () => getCategoryStats(from, to, type),
  });
  const data: CategoryStat[] = statsQuery.data ?? [];
  const income = type === "income";
  const Icon = income ? TrendingUp : TrendingDown;

  return (
    <SkeletonWrapper isLoading={statsQuery.isLoading}>
      <Card className="h-[22rem] w-full">
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="flex items-center gap-2 text-lg">
            <Icon className={cn("h-5 w-5", income ? "text-income" : "text-expense")} />
            {income ? "Income" : "Expenses"} by category
          </CardTitle>
          <span className="text-xs text-muted-foreground">{data.length} categories</span>
        </CardHeader>

        {data.length === 0 ? (
          <div className="px-6">
            <EmptyState compact icon={PieChart} title="No data for this period" description={`Add some ${income ? "income" : "expenses"} or pick another range.`} />
          </div>
        ) : (
          <ScrollArea className="h-64 px-6">
            <ul className="flex flex-col gap-4 pb-6 pt-2">
              {data.map((item, index) => (
                <motion.li
                  key={item.categoryId}
                  initial={{ opacity: 0, x: -8 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: index * 0.05, duration: 0.4, ease: EASE_OUT }}
                  className="flex flex-col gap-2"
                >
                  <div className="flex items-center justify-between gap-3">
                    <span className="flex min-w-0 items-center gap-2.5">
                      <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-muted text-base">
                        {item.categoryIcon}
                      </span>
                      <span className="truncate text-sm">{item.categoryName}</span>
                      <span className="text-xs text-muted-foreground">{item.percentOfTotal.toFixed(0)}%</span>
                    </span>
                    <span className="text-sm font-semibold tabular-nums">{formatter.format(item.total)}</span>
                  </div>
                  <div className="h-1.5 overflow-hidden rounded-full bg-muted">
                    <motion.div
                      className={cn("h-full rounded-full", income ? "bg-income" : "bg-expense")}
                      initial={{ width: 0 }}
                      animate={{ width: `${Math.max(item.percentOfTotal, 2)}%` }}
                      transition={{ delay: 0.15 + index * 0.05, duration: 0.8, ease: EASE_OUT }}
                    />
                  </div>
                </motion.li>
              ))}
            </ul>
          </ScrollArea>
        )}
      </Card>
    </SkeletonWrapper>
  );
}
