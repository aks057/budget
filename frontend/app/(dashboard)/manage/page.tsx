"use client";

import CreateCategoryDialog from "@/app/(dashboard)/_components/CreateCategoryDialog";
import DeleteCategoryDialog from "@/app/(dashboard)/_components/DeleteCategoryDialog";
import { CurrencyComboBox } from "@/components/CurrencyComboBox";
import { EmptyState } from "@/components/EmptyState";
import { Reveal, SpotlightCard, Stagger, StaggerItem } from "@/components/motion";
import { PageHeader } from "@/components/PageHeader";
import SkeletonWrapper from "@/components/SkeletonWrapper";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Category, getCategories } from "@/lib/api/endpoints";
import { TransactionType } from "@/lib/types";
import { cn } from "@/lib/utils";
import { useQuery } from "@tanstack/react-query";
import { Coins, Plus, Settings, Tags, Trash2, TrendingDown, TrendingUp } from "lucide-react";
import React from "react";

function ManagePage() {
  return (
    <div className="pb-16">
      <PageHeader icon={Settings} title="Manage" subtitle="Currency and the categories you track" />
      <div className="container flex flex-col gap-6">
        <Reveal onMount delay={0.05}>
          <SpotlightCard className="flex flex-wrap items-center justify-between gap-4 p-5">
            <div className="flex items-center gap-3">
              <span className="flex h-11 w-11 items-center justify-center rounded-xl border border-brand-2/20 bg-brand-2/10">
                <Coins className="h-5 w-5 text-brand-2" />
              </span>
              <div>
                <p className="font-display font-semibold">Currency</p>
                <p className="text-sm text-muted-foreground">Used to format every amount in the app</p>
              </div>
            </div>
            <div className="w-full sm:w-64">
              <CurrencyComboBox />
            </div>
          </SpotlightCard>
        </Reveal>
        <CategoryList type="income" />
        <CategoryList type="expense" />
      </div>
    </div>
  );
}

export default ManagePage;

function CategoryList({ type }: { type: TransactionType }) {
  const categoriesQuery = useQuery({
    queryKey: ["categories", type],
    queryFn: () => getCategories(type),
  });
  const income = type === "income";
  const Icon = income ? TrendingUp : TrendingDown;
  const dataAvailable = (categoriesQuery.data?.length ?? 0) > 0;

  return (
    <SkeletonWrapper isLoading={categoriesQuery.isLoading}>
      <Card>
        <CardHeader className="flex flex-row flex-wrap items-center justify-between gap-3 space-y-0">
          <div className="flex items-center gap-3">
            <span
              className={cn(
                "flex h-11 w-11 items-center justify-center rounded-xl border",
                income ? "border-income/20 bg-income/10" : "border-expense/20 bg-expense/10"
              )}
            >
              <Icon className={cn("h-5 w-5", income ? "text-income" : "text-expense")} />
            </span>
            <div>
              <CardTitle className="text-lg">{income ? "Income" : "Expense"} categories</CardTitle>
              <CardDescription>{categoriesQuery.data?.length ?? 0} categories · sorted by name</CardDescription>
            </div>
          </div>
          <CreateCategoryDialog
            type={type}
            successCallback={() => categoriesQuery.refetch()}
            trigger={
              <Button size="sm" className="gap-2">
                <Plus className="h-4 w-4" /> New category
              </Button>
            }
          />
        </CardHeader>
        <CardContent>
          {!dataAvailable ? (
            <EmptyState compact icon={Tags} title={`No ${type} categories yet`} description="Create one to get started." />
          ) : (
            <Stagger onMount className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6" stagger={0.03}>
              {categoriesQuery.data?.map((category) => (
                <StaggerItem key={category.id}>
                  <CategoryTile category={category} />
                </StaggerItem>
              ))}
            </Stagger>
          )}
        </CardContent>
      </Card>
    </SkeletonWrapper>
  );
}

function CategoryTile({ category }: { category: Category }) {
  return (
    <div className="group relative flex flex-col items-center gap-2 overflow-hidden rounded-xl border bg-background px-3 pb-3 pt-4 text-center transition-all duration-300 hover:border-primary/30 hover:bg-accent/50">
      <span className="text-3xl transition-transform duration-300 group-hover:scale-110" role="img" aria-hidden>
        {category.icon}
      </span>
      <span className="truncate text-sm font-medium">{category.name}</span>
      {/* Delete action slides up from the bottom edge on hover (always visible on touch). */}
      <DeleteCategoryDialog
        category={category}
        trigger={
          <button
            type="button"
            aria-label={`Remove ${category.name}`}
            className="flex w-full items-center justify-center gap-1.5 rounded-lg py-1 text-xs text-muted-foreground transition-all hover:bg-expense/15 hover:text-expense md:translate-y-8 md:opacity-0 md:group-hover:translate-y-0 md:group-hover:opacity-100 md:focus-visible:translate-y-0 md:focus-visible:opacity-100"
          >
            <Trash2 className="h-3.5 w-3.5" /> Remove
          </button>
        }
      />
    </div>
  );
}
