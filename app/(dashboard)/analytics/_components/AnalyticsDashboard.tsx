"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import SkeletonWrapper from "@/components/SkeletonWrapper";
import { GetFormatterForCurrency } from "@/lib/helpers";
import { UserSettings } from "@/lib/supabase/database.types";
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
import { startOfMonth, endOfMonth, differenceInDays } from "date-fns";
import { toast } from "sonner";

const COLORS = [
  "#f59e0b",
  "#ef4444",
  "#10b981",
  "#3b82f6",
  "#8b5cf6",
  "#ec4899",
  "#06b6d4",
  "#84cc16",
];

export default function AnalyticsDashboard({
  userSettings,
}: {
  userSettings: UserSettings;
}) {
  const [dateRange, setDateRange] = useState<{ from: Date; to: Date }>({
    from: startOfMonth(new Date()),
    to: endOfMonth(new Date()),
  });

  const formatter = useMemo(() => {
    return GetFormatterForCurrency(userSettings.currency);
  }, [userSettings.currency]);

  // Fetch transactions for the date range
  const transactionsQuery = useQuery({
    queryKey: ["analytics", "transactions", dateRange.from, dateRange.to],
    queryFn: () =>
      fetch(
        `/api/transactions-history?from=${dateRange.from.toISOString()}&to=${dateRange.to.toISOString()}`
      ).then((res) => res.json()),
  });

  // Fetch category stats
  const categoryStatsQuery = useQuery({
    queryKey: ["analytics", "categories", dateRange.from, dateRange.to],
    queryFn: () =>
      fetch(
        `/api/stats/categories?from=${dateRange.from.toISOString()}&to=${dateRange.to.toISOString()}`
      ).then((res) => res.json()),
  });

  // Process data for charts
  const { dailyData, totalIncome, totalExpense, categoryData } = useMemo(() => {
    if (!transactionsQuery.data || !Array.isArray(transactionsQuery.data)) {
      return {
        dailyData: [],
        totalIncome: 0,
        totalExpense: 0,
        categoryData: { income: [], expense: [] },
      };
    }

    // Group transactions by date
    const dateMap = new Map<
      string,
      { date: string; income: number; expense: number }
    >();

    let income = 0;
    let expense = 0;

    transactionsQuery.data.forEach((transaction: any) => {
      const date = new Date(transaction.date).toLocaleDateString();
      const existing = dateMap.get(date) || { date, income: 0, expense: 0 };

      if (transaction.type === "income") {
        existing.income += transaction.amount;
        income += transaction.amount;
      } else {
        existing.expense += transaction.amount;
        expense += transaction.amount;
      }

      dateMap.set(date, existing);
    });

    // Sort by date
    const sortedData = Array.from(dateMap.values()).sort(
      (a, b) => new Date(a.date).getTime() - new Date(b.date).getTime()
    );

    // Process category data
    const incomeCategories: any[] = [];
    const expenseCategories: any[] = [];

    if (categoryStatsQuery.data && Array.isArray(categoryStatsQuery.data)) {
      categoryStatsQuery.data.forEach((cat: any) => {
        const item = {
          name: cat.category,
          value: cat._sum?.amount || 0,
          icon: cat.categoryIcon,
        };
        if (cat.type === "income") {
          incomeCategories.push(item);
        } else {
          expenseCategories.push(item);
        }
      });
    }

    return {
      dailyData: sortedData,
      totalIncome: income,
      totalExpense: expense,
      categoryData: {
        income: incomeCategories,
        expense: expenseCategories,
      },
    };
  }, [transactionsQuery.data, categoryStatsQuery.data]);

  const balance = totalIncome - totalExpense;
  const savingsRate = totalIncome > 0 ? ((totalIncome - totalExpense) / totalIncome) * 100 : 0;

  return (
    <div className="container py-8">
      {/* Date Range Picker */}
      <div className="mb-6 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Calendar className="h-5 w-5 text-muted-foreground" />
          <span className="text-sm text-muted-foreground">Select period:</span>
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

      {/* Summary Cards */}
      <div className="mb-8 grid gap-4 md:grid-cols-4">
        <SkeletonWrapper isLoading={transactionsQuery.isLoading}>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium">Total Income</CardTitle>
              <TrendingUp className="h-4 w-4 text-emerald-500" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-emerald-500">
                {formatter.format(totalIncome)}
              </div>
            </CardContent>
          </Card>
        </SkeletonWrapper>

        <SkeletonWrapper isLoading={transactionsQuery.isLoading}>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium">Total Expense</CardTitle>
              <TrendingDown className="h-4 w-4 text-red-500" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-red-500">
                {formatter.format(totalExpense)}
              </div>
            </CardContent>
          </Card>
        </SkeletonWrapper>

        <SkeletonWrapper isLoading={transactionsQuery.isLoading}>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium">Balance</CardTitle>
              <BarChart3 className="h-4 w-4 text-amber-500" />
            </CardHeader>
            <CardContent>
              <div
                className={`text-2xl font-bold ${
                  balance >= 0 ? "text-emerald-500" : "text-red-500"
                }`}
              >
                {formatter.format(balance)}
              </div>
            </CardContent>
          </Card>
        </SkeletonWrapper>

        <SkeletonWrapper isLoading={transactionsQuery.isLoading}>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium">Savings Rate</CardTitle>
              <PieChartIcon className="h-4 w-4 text-blue-500" />
            </CardHeader>
            <CardContent>
              <div
                className={`text-2xl font-bold ${
                  savingsRate >= 0 ? "text-emerald-500" : "text-red-500"
                }`}
              >
                {savingsRate.toFixed(1)}%
              </div>
            </CardContent>
          </Card>
        </SkeletonWrapper>
      </div>

      {/* Trend Chart */}
      <Card className="mb-8">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <BarChart3 className="h-5 w-5" />
            Income vs Expense Trend
          </CardTitle>
        </CardHeader>
        <CardContent>
          <SkeletonWrapper isLoading={transactionsQuery.isLoading}>
            {dailyData.length > 0 ? (
              <ResponsiveContainer width="100%" height={300}>
                <AreaChart data={dailyData}>
                  <defs>
                    <linearGradient id="incomeGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#10b981" stopOpacity={0.8} />
                      <stop offset="95%" stopColor="#10b981" stopOpacity={0} />
                    </linearGradient>
                    <linearGradient id="expenseGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#ef4444" stopOpacity={0.8} />
                      <stop offset="95%" stopColor="#ef4444" stopOpacity={0} />
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
                    stroke="#10b981"
                    fillOpacity={1}
                    fill="url(#incomeGradient)"
                    name="Income"
                  />
                  <Area
                    type="monotone"
                    dataKey="expense"
                    stroke="#ef4444"
                    fillOpacity={1}
                    fill="url(#expenseGradient)"
                    name="Expense"
                  />
                </AreaChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex h-[300px] flex-col items-center justify-center rounded-lg border border-dashed">
                <BarChart3 className="h-12 w-12 text-muted-foreground/50" />
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
              <SkeletonWrapper isLoading={categoryStatsQuery.isLoading}>
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
                          key={cat.name}
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
                    <PieChartIcon className="h-12 w-12 text-muted-foreground/50" />
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
              <SkeletonWrapper isLoading={categoryStatsQuery.isLoading}>
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
                          key={cat.name}
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
                    <PieChartIcon className="h-12 w-12 text-muted-foreground/50" />
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
