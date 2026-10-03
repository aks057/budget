"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import SkeletonWrapper from "@/components/SkeletonWrapper";
import { AnimatedNumber, SpotlightCard, Stagger, StaggerItem } from "@/components/motion";
import { GetFormatterForCurrency } from "@/lib/helpers";
import { CATEGORY_COLORS, EXPENSE_COLOR, INCOME_COLOR } from "@/lib/chartColors";
import { CategoryStat, getCategoryStats, getOverview, getTrends, toApiDate } from "@/lib/api/endpoints";
import { useQuery } from "@tanstack/react-query";
import {
  TrendingUp,
  TrendingDown,
  PieChart as PieChartIcon,
  BarChart3,
  Calendar,
} from "lucide-react";
import { useMemo, useState } from "react";
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
} from "recharts";
import { DateRangePicker } from "@/components/ui/date-range-picker";
import { MAX_DATE_RANGE_DAYS } from "@/lib/constants";
import { startOfMonth, endOfMonth, differenceInDays, format, parse } from "date-fns";
import { toast } from "sonner";

const TREND_MONTHS = 6;

const toSlices = (stats: CategoryStat[] | undefined) =>
  (stats ?? []).map((stat) => ({ id: stat.categoryId, name: stat.categoryName, icon: stat.categoryIcon, value: stat.total }));

const COLORS = CATEGORY_COLORS;

export default function AnalyticsDashboard({ currency }: { currency: string }) {
  const [dateRange, setDateRange] = useState<{ from: Date; to: Date }>({
    from: startOfMonth(new Date()),
    to: endOfMonth(new Date()),
  });

  const formatter = useMemo(() => GetFormatterForCurrency(currency), [currency]);
  const formatMoney = useMemo(() => (value: number) => formatter.format(value), [formatter]);
  const formatPercent = useMemo(() => (value: number) => `${value.toFixed(1)}%`, []);

  const fromKey = toApiDate(dateRange.from);
  const toKey = toApiDate(dateRange.to);

  // Totals for the selected range are aggregated server-side.
  const overviewQuery = useQuery({
    queryKey: ["analytics", "overview", fromKey, toKey],
    queryFn: () => getOverview(dateRange.from, dateRange.to),
  });

  const incomeStatsQuery = useQuery({
    queryKey: ["analytics", "categories", "income", fromKey, toKey],
    queryFn: () => getCategoryStats(dateRange.from, dateRange.to, "income"),
  });

  const expenseStatsQuery = useQuery({
    queryKey: ["analytics", "categories", "expense", fromKey, toKey],
    queryFn: () => getCategoryStats(dateRange.from, dateRange.to, "expense"),
  });

  // The trend is month-by-month over a fixed window ending this month, independent of the range picker.
  const trendsQuery = useQuery({
    queryKey: ["analytics", "trends", TREND_MONTHS],
    queryFn: () => getTrends(TREND_MONTHS),
  });

  const trendData = useMemo(
    () =>
      (trendsQuery.data ?? []).map((row) => ({
        date: format(parse(row.month, "yyyy-MM", new Date()), "MMM yyyy"),
        income: row.income,
        expense: row.expense,
      })),
    [trendsQuery.data]
  );
  const hasTrendData = trendData.some((point) => point.income > 0 || point.expense > 0);

  const categoryData = useMemo(
    () => ({ income: toSlices(incomeStatsQuery.data), expense: toSlices(expenseStatsQuery.data) }),
    [incomeStatsQuery.data, expenseStatsQuery.data]
  );

  const totalIncome = overviewQuery.data?.income ?? 0;
  const totalExpense = overviewQuery.data?.expense ?? 0;
  const balance = overviewQuery.data?.balance ?? 0;
  const savingsRate = totalIncome > 0 ? (balance / totalIncome) * 100 : 0;

  return (
    <div className="container pb-16">
      {/* Date Range Picker */}
      <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <Calendar className="h-5 w-5 text-primary" />
          <span className="text-sm text-muted-foreground">Period</span>
        </div>
        <DateRangePicker
          initialDateFrom={dateRange.from}
          initialDateTo={dateRange.to}
          showCompare={false}
          onUpdate={(values) => {
            const { from, to } = values.range;
            if (!from || !to) return;
            if (differenceInDays(to, from) > MAX_DATE_RANGE_DAYS) {
              toast.error(
                `Date range cannot exceed ${MAX_DATE_RANGE_DAYS} days`
              );
              return;
            }
            setDateRange({ from, to });
          }}
        />
      </div>

      {/* Summary metrics */}
      <SkeletonWrapper isLoading={overviewQuery.isLoading}>
        <Stagger onMount className="mb-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {[
            { label: "Total income", value: totalIncome, icon: TrendingUp, tone: "text-income", format: formatMoney },
            { label: "Total expense", value: totalExpense, icon: TrendingDown, tone: "text-expense", format: formatMoney },
            { label: "Balance", value: balance, icon: BarChart3, tone: balance >= 0 ? "text-foreground" : "text-expense", iconTone: "text-brand-2", format: formatMoney },
            { label: "Savings rate", value: savingsRate, icon: PieChartIcon, tone: savingsRate >= 0 ? "text-income" : "text-expense", format: formatPercent },
          ].map((metric) => (
            <StaggerItem key={metric.label}>
              <SpotlightCard className="h-full p-5">
                <div className="flex items-center justify-between">
                  <p className="text-sm font-medium text-muted-foreground">{metric.label}</p>
                  <metric.icon className={`h-4 w-4 ${metric.iconTone ?? metric.tone}`} />
                </div>
                <AnimatedNumber value={metric.value} format={metric.format} className={`mt-3 block font-display text-2xl font-bold tracking-tight ${metric.tone}`} />
              </SpotlightCard>
            </StaggerItem>
          ))}
        </Stagger>
      </SkeletonWrapper>

      {/* Trend Chart */}
      <Card className="mb-8">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <BarChart3 className="h-5 w-5" />
            Income vs Expense (last {TREND_MONTHS} months)
          </CardTitle>
        </CardHeader>
        <CardContent>
          <SkeletonWrapper isLoading={trendsQuery.isLoading}>
            {hasTrendData ? (
              <ResponsiveContainer width="100%" height={300}>
                <AreaChart data={trendData}>
                  <defs>
                    <linearGradient id="incomeGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor={INCOME_COLOR} stopOpacity={0.8} />
                      <stop offset="95%" stopColor={INCOME_COLOR} stopOpacity={0} />
                    </linearGradient>
                    <linearGradient id="expenseGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor={EXPENSE_COLOR} stopOpacity={0.8} />
                      <stop offset="95%" stopColor={EXPENSE_COLOR} stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" strokeOpacity={0.2} />
                  <XAxis
                    dataKey="date"
                    stroke="#888888"
                    fontSize={12}
                    tickLine={false}
                    axisLine={false}
                  />
                  <YAxis
                    stroke="#888888"
                    fontSize={12}
                    tickLine={false}
                    axisLine={false}
                    tickFormatter={(value) => formatter.format(value)}
                  />
                  <Tooltip
                    formatter={(value: number) => formatter.format(value)}
                    contentStyle={{
                      backgroundColor: "hsl(var(--background))",
                      border: "1px solid hsl(var(--border))",
                      borderRadius: "8px",
                    }}
                  />
                  <Area
                    type="monotone"
                    dataKey="income"
                    stroke={INCOME_COLOR}
                    fillOpacity={1}
                    fill="url(#incomeGradient)"
                    name="Income"
                  />
                  <Area
                    type="monotone"
                    dataKey="expense"
                    stroke={EXPENSE_COLOR}
                    fillOpacity={1}
                    fill="url(#expenseGradient)"
                    name="Expense"
                  />
                </AreaChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex h-[300px] flex-col items-center justify-center rounded-lg border border-dashed">
                <BarChart3 className="h-12 w-12 text-primary/40" />
                <p className="mt-4 text-lg font-medium">No data available</p>
                <p className="text-sm text-muted-foreground">
                  Add some transactions to see your trends
                </p>
              </div>
            )}
          </SkeletonWrapper>
        </CardContent>
      </Card>

      {/* Category Breakdown */}
      <Tabs defaultValue="expense" className="w-full">
        <TabsList className="mb-4">
          <TabsTrigger value="expense" className="gap-2">
            <TrendingDown className="h-4 w-4" />
            Expense Categories
          </TabsTrigger>
          <TabsTrigger value="income" className="gap-2">
            <TrendingUp className="h-4 w-4" />
            Income Categories
          </TabsTrigger>
        </TabsList>

        <TabsContent value="expense">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <PieChartIcon className="h-5 w-5" />
                Expense Distribution
              </CardTitle>
            </CardHeader>
            <CardContent>
              <SkeletonWrapper isLoading={expenseStatsQuery.isLoading}>
                {categoryData.expense.length > 0 ? (
                  <div className="grid gap-8 md:grid-cols-2">
                    <ResponsiveContainer width="100%" height={300}>
                      <PieChart>
                        <Pie
                          data={categoryData.expense}
                          cx="50%"
                          cy="50%"
                          innerRadius={60}
                          outerRadius={100}
                          paddingAngle={2}
                          dataKey="value"
                        >
                          {categoryData.expense.map((_, index) => (
                            <Cell
                              key={`cell-${index}`}
                              fill={COLORS[index % COLORS.length]}
                            />
                          ))}
                        </Pie>
                        <Tooltip
                          formatter={(value: number) => formatter.format(value)}
                        />
                        <Legend />
                      </PieChart>
                    </ResponsiveContainer>
                    <div className="space-y-3">
                      {categoryData.expense.map((cat, index) => (
                        <div
                          key={cat.id}
                          className="flex items-center justify-between"
                        >
                          <div className="flex items-center gap-2">
                            <div
                              className="h-3 w-3 rounded-full"
                              style={{
                                backgroundColor: COLORS[index % COLORS.length],
                              }}
                            />
                            <span className="text-sm">
                              {cat.icon} {cat.name}
                            </span>
                          </div>
                          <span className="font-medium">
                            {formatter.format(cat.value)}
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>
                ) : (
                  <div className="flex h-[300px] flex-col items-center justify-center rounded-lg border border-dashed">
                    <PieChartIcon className="h-12 w-12 text-primary/40" />
                    <p className="mt-4 text-lg font-medium">No expenses yet</p>
                    <p className="text-sm text-muted-foreground">
                      Add expense transactions to see the breakdown
                    </p>
                  </div>
                )}
              </SkeletonWrapper>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="income">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <PieChartIcon className="h-5 w-5" />
                Income Distribution
              </CardTitle>
            </CardHeader>
            <CardContent>
              <SkeletonWrapper isLoading={incomeStatsQuery.isLoading}>
                {categoryData.income.length > 0 ? (
                  <div className="grid gap-8 md:grid-cols-2">
                    <ResponsiveContainer width="100%" height={300}>
                      <PieChart>
                        <Pie
                          data={categoryData.income}
                          cx="50%"
                          cy="50%"
                          innerRadius={60}
                          outerRadius={100}
                          paddingAngle={2}
                          dataKey="value"
                        >
                          {categoryData.income.map((_, index) => (
                            <Cell
                              key={`cell-${index}`}
                              fill={COLORS[index % COLORS.length]}
                            />
                          ))}
                        </Pie>
                        <Tooltip
                          formatter={(value: number) => formatter.format(value)}
                        />
                        <Legend />
                      </PieChart>
                    </ResponsiveContainer>
                    <div className="space-y-3">
                      {categoryData.income.map((cat, index) => (
                        <div
                          key={cat.id}
                          className="flex items-center justify-between"
                        >
                          <div className="flex items-center gap-2">
                            <div
                              className="h-3 w-3 rounded-full"
                              style={{
                                backgroundColor: COLORS[index % COLORS.length],
                              }}
                            />
                            <span className="text-sm">
                              {cat.icon} {cat.name}
                            </span>
                          </div>
                          <span className="font-medium">
                            {formatter.format(cat.value)}
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>
                ) : (
                  <div className="flex h-[300px] flex-col items-center justify-center rounded-lg border border-dashed">
                    <PieChartIcon className="h-12 w-12 text-primary/40" />
                    <p className="mt-4 text-lg font-medium">No income yet</p>
                    <p className="text-sm text-muted-foreground">
                      Add income transactions to see the breakdown
                    </p>
                  </div>
                )}
              </SkeletonWrapper>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
