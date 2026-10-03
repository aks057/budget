"use client";

import CategoriesStats from "@/app/(dashboard)/_components/CategoriesStats";
import StatsCards from "@/app/(dashboard)/_components/StatsCards";
import { Reveal } from "@/components/motion";
import { DateRangePicker } from "@/components/ui/date-range-picker";
import { MAX_DATE_RANGE_DAYS } from "@/lib/constants";
import { differenceInDays, startOfMonth } from "date-fns";
import React, { useState } from "react";
import { toast } from "sonner";

function Overview({ currency }: { currency: string }) {
  const [dateRange, setDateRange] = useState<{ from: Date; to: Date }>({
    from: startOfMonth(new Date()),
    to: new Date(),
  });

  return (
    <section className="container">
      <Reveal className="mb-5 flex flex-wrap items-end justify-between gap-4">
        <div>
          <h2 className="font-display text-lg font-semibold tracking-tight">Overview</h2>
          <p className="text-sm text-muted-foreground">Income, expenses and where the money went</p>
        </div>
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
      </Reveal>
      <div className="flex w-full flex-col gap-4">
        <StatsCards currency={currency} from={dateRange.from} to={dateRange.to} />
        <CategoriesStats currency={currency} from={dateRange.from} to={dateRange.to} />
      </div>
    </section>
  );
}

export default Overview;
