"use client";

import SkeletonWrapper from "@/components/SkeletonWrapper";
import { Card, CardHeader, CardTitle } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { ScrollArea } from "@/components/ui/scroll-area";
import { CategoryStat, getCategoryStats, toApiDate } from "@/lib/api/endpoints";
import { GetFormatterForCurrency } from "@/lib/helpers";
import { TransactionType } from "@/lib/types";
import { useQuery } from "@tanstack/react-query";
import React, { useMemo } from "react";

interface Props {
  currency: string;
  from: Date;
  to: Date;
}

function CategoriesStats({ currency, from, to }: Props) {
  const formatter = useMemo(() => GetFormatterForCurrency(currency), [currency]);

  return (
    <div className="flex w-full flex-wrap gap-2 md:flex-nowrap">
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

  return (
    <SkeletonWrapper isLoading={statsQuery.isFetching}>
      <Card className="h-80 w-full col-span-6">
        <CardHeader>
          <CardTitle className="grid grid-flow-row justify-between gap-2 text-muted-foreground md:grid-flow-col">
            {type === "income" ? "Incomes" : "Expenses"} by category
          </CardTitle>
        </CardHeader>

        <div className="flex items-center justify-between gap-2">
          {data.length === 0 && (
            <div className="flex h-60 w-full flex-col items-center justify-center">
              No data for the selected period
              <p className="text-sm text-muted-foreground">
                Try selecting a different period or try adding new {type === "income" ? "incomes" : "expenses"}
              </p>
            </div>
          )}

          {data.length > 0 && (
            <ScrollArea className="h-60 w-full px-4">
              <div className="flex w-full flex-col gap-4 p-4">
                {data.map((item) => (
                  <div key={item.categoryId} className="flex flex-col gap-2">
                    <div className="flex items-center justify-between">
                      <span className="flex items-center text-gray-400">
                        {item.categoryIcon} {item.categoryName}
                        <span className="ml-2 text-xs text-muted-foreground">
                          ({item.percentOfTotal.toFixed(0)}%)
                        </span>
                      </span>

                      <span className="text-sm text-gray-400">{formatter.format(item.total)}</span>
                    </div>

                    <Progress
                      value={item.percentOfTotal}
                      indicator={type === "income" ? "bg-emerald-500" : "bg-red-500"}
                    />
                  </div>
                ))}
              </div>
            </ScrollArea>
          )}
        </div>
      </Card>
    </SkeletonWrapper>
  );
}
