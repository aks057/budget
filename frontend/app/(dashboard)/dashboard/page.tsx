"use client";

import { AskAiCard } from "@/app/(dashboard)/_components/AskAiCard";
import CreateTransactionDialog from "@/app/(dashboard)/_components/CreateTransactionDialog";
import { InsightsPanel } from "@/app/(dashboard)/_components/InsightsPanel";
import History from "@/app/(dashboard)/_components/History";
import Overview from "@/app/(dashboard)/_components/Overview";
import { useCurrentUser } from "@/components/providers/AuthProvider";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { TrendingUp, TrendingDown } from "lucide-react";
import React from "react";

function DashboardPage() {
  const user = useCurrentUser();
  const firstName = user.fullName.split(" ")[0];

  return (
    <div className="h-full bg-background">
      <div className="border-b bg-card">
        <div className="container flex flex-wrap items-center justify-between gap-6 py-8">
          <div>
            <h1 className="text-3xl font-bold">Welcome back, {firstName}!</h1>
            <p className="text-muted-foreground mt-1">Here&apos;s an overview of your finances</p>
          </div>

          <div className="flex items-center gap-3">
            <CreateTransactionDialog
              trigger={
                <Button className="gap-2 bg-emerald-600 text-white hover:bg-emerald-700">
                  <TrendingUp className="h-4 w-4" />
                  New Income
                </Button>
              }
              type="income"
            />

            <CreateTransactionDialog
              trigger={
                <Button
                  variant="outline"
                  className="gap-2 border-rose-500/50 text-rose-500 hover:bg-rose-500/10 hover:text-rose-500"
                >
                  <TrendingDown className="h-4 w-4" />
                  New Expense
                </Button>
              }
              type="expense"
            />
          </div>
        </div>
      </div>
      <div className="container space-y-4 pt-6">
        <AskAiCard />
        <InsightsPanel currency={user.currency} />
      </div>
      <Overview currency={user.currency} />
      <div className="container">
        <Separator className="my-6" />
      </div>
      <History currency={user.currency} />
    </div>
  );
}

export default DashboardPage;
