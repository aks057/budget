"use client";

import CreateTransactionDialog from "@/app/(dashboard)/_components/CreateTransactionDialog";
import TransactionTable from "@/app/(dashboard)/transactions/_components/TransactionTable";
import { Reveal } from "@/components/motion";
import { PageHeader } from "@/components/PageHeader";
import { Button } from "@/components/ui/button";
import { DateRangePicker } from "@/components/ui/date-range-picker";
import { MAX_DATE_RANGE_DAYS } from "@/lib/constants";
import { differenceInDays, startOfMonth } from "date-fns";
import { ArrowLeftRight, Minus, Plus } from "lucide-react";
import React, { useState } from "react";
import { toast } from "sonner";

function TransactionsPage() {
  const [dateRange, setDateRange] = useState<{ from: Date; to: Date }>({
    from: startOfMonth(new Date()),
    to: new Date(),
  });
  return (
    <div className="pb-16">
      <PageHeader
        icon={ArrowLeftRight}
        title="Transactions"
        subtitle="Every income and expense, filterable and exportable"
        actions={
          <>
            <DateRangePicker
              initialDateFrom={dateRange.from}
              initialDateTo={dateRange.to}
              showCompare={false}
              onUpdate={(values) => {
                const { from, to } = values.range;
                // Update only once both ends of the range are set.
                if (!from || !to) return;
                if (differenceInDays(to, from) > MAX_DATE_RANGE_DAYS) {
                  toast.error(`The selected date range is too big. Max allowed range is ${MAX_DATE_RANGE_DAYS} days!`);
                  return;
                }
                setDateRange({ from, to });
              }}
            />
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
                <Button variant="outline" className="gap-2 border-expense/40 text-expense hover:border-expense/60 hover:bg-expense/10 hover:text-expense">
                  <Minus className="h-4 w-4" /> Expense
                </Button>
              }
            />
          </>
        }
      />
      <Reveal onMount delay={0.1} className="container">
        <TransactionTable from={dateRange.from} to={dateRange.to} />
      </Reveal>
    </div>
  );
}

export default TransactionsPage;
