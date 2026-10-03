"use client";

import CreateTransactionDialog from "@/app/(dashboard)/_components/CreateTransactionDialog";
import { InsightsPanel } from "@/app/(dashboard)/_components/InsightsPanel";
import History from "@/app/(dashboard)/_components/History";
import Overview from "@/app/(dashboard)/_components/Overview";
import { PageHeader } from "@/components/PageHeader";
import { useCurrentUser } from "@/components/providers/AuthProvider";
import { Button } from "@/components/ui/button";
import { format } from "date-fns";
import { Minus, Plus } from "lucide-react";
import React from "react";

function greeting(hour: number) {
  if (hour < 12) return "Good morning";
  if (hour < 17) return "Good afternoon";
  return "Good evening";
}

/** Dashboard: greeting → overview (stats + categories) → this month / needs attention → history. */
function DashboardPage() {
  const user = useCurrentUser();
  const firstName = user.fullName.trim().split(/\s+/)[0];
  const now = new Date();

  return (
    <div className="pb-24">
      <PageHeader
        title={`${greeting(now.getHours())}, ${firstName}`}
        subtitle={format(now, "EEEE, d MMMM yyyy")}
        actions={
          <>
            <CreateTransactionDialog
              type="income"
              trigger={
                <Button className="gap-2">
                  <Plus className="h-4 w-4" /> Income
                </Button>
              }
            />
            <CreateTransactionDialog
              type="expense"
              trigger={
                <Button variant="outline" className="gap-2">
                  <Minus className="h-4 w-4 text-expense" /> Expense
                </Button>
              }
            />
          </>
        }
      />

      <div className="space-y-8">
        <Overview currency={user.currency} />
        <section className="container">
          <InsightsPanel currency={user.currency} />
        </section>
        <History currency={user.currency} />
      </div>
    </div>
  );
}

export default DashboardPage;
