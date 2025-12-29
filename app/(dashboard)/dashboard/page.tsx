import CreateTransactionDialog from "@/app/(dashboard)/_components/CreateTransactionDialog";
import History from "@/app/(dashboard)/_components/History";
import Overview from "@/app/(dashboard)/_components/Overview";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { createClient } from "@/lib/supabase/server";
import { requireUser, getUserMetadata } from "@/lib/supabase/auth";
import { redirect } from "next/navigation";
import { TrendingUp, TrendingDown } from "lucide-react";
import React from "react";

async function DashboardPage() {
  const user = await requireUser();
  const userMetadata = await getUserMetadata();
  const supabase = await createClient();

  const { data: userSettings } = await supabase
    .from("user_settings")
    .select("*")
    .eq("user_id", user.id)
    .single();

  if (!userSettings) {
    redirect("/wizard");
  }

  return (
    <div className="h-full bg-background">
      <div className="border-b bg-card">
        <div className="container flex flex-wrap items-center justify-between gap-6 py-8">
          <div>
            <h1 className="text-3xl font-bold">
              Welcome back, {userMetadata?.firstName}!
            </h1>
            <p className="text-muted-foreground mt-1">
              Here&apos;s an overview of your finances
            </p>
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
                <Button variant="outline" className="gap-2 border-rose-500/50 text-rose-500 hover:bg-rose-500/10 hover:text-rose-500">
                  <TrendingDown className="h-4 w-4" />
                  New Expense
                </Button>
              }
              type="expense"
            />
          </div>
        </div>
      </div>
      <Overview userSettings={userSettings} />
      <div className="container">
        <Separator className="my-6" />
      </div>
      <History userSettings={userSettings} />
    </div>
  );
}

export default DashboardPage;
