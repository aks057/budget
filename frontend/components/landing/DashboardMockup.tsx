"use client";

import { AnimatedNumber } from "@/components/motion";
import { motion } from "framer-motion";
import { ArrowDownRight, ArrowUpRight, Lock, Sparkles } from "lucide-react";

const inr = new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 0 });
const formatInr = (value: number) => inr.format(value);

const BARS = [38, 52, 44, 63, 48, 71, 58, 76, 64, 83, 70, 91];
const CATEGORIES = [
  { name: "Food", pct: 34, color: "bg-emerald-400" },
  { name: "Rent", pct: 28, color: "bg-cyan-400" },
  { name: "Travel", pct: 18, color: "bg-indigo-400" },
  { name: "Shopping", pct: 12, color: "bg-violet-400" },
];

/** Product preview used in the hero: illustrative sample data (labelled as such), animated on entry. */
export function DashboardMockup() {
  return (
    <div className="relative overflow-hidden rounded-2xl border border-white/10 bg-card/80 shadow-[0_40px_120px_-30px_hsl(var(--glow)/0.45)] backdrop-blur-2xl">
      {/* Browser chrome */}
      <div className="flex items-center gap-2 border-b border-white/5 px-4 py-3">
        <div className="flex gap-1.5">
          <span className="h-2.5 w-2.5 rounded-full bg-white/15" />
          <span className="h-2.5 w-2.5 rounded-full bg-white/15" />
          <span className="h-2.5 w-2.5 rounded-full bg-white/15" />
        </div>
        <div className="mx-auto flex items-center gap-1.5 rounded-md bg-white/5 px-3 py-1 text-[11px] text-muted-foreground">
          <Lock className="h-3 w-3" /> budwiser.app/dashboard
        </div>
        <span className="rounded-md bg-white/5 px-2 py-0.5 text-[10px] uppercase tracking-wider text-muted-foreground">
          Sample data
        </span>
      </div>

      <div className="grid gap-3 p-4 sm:grid-cols-3 sm:p-5">
        {/* Stat tiles */}
        {[
          { label: "Balance", value: 124500, tone: "text-foreground", icon: null },
          { label: "Income", value: 85000, tone: "text-income", icon: ArrowUpRight },
          { label: "Expenses", value: 42300, tone: "text-expense", icon: ArrowDownRight },
        ].map((stat) => (
          <div key={stat.label} className="rounded-xl border border-white/5 bg-white/[0.03] p-3 sm:p-4">
            <p className="flex items-center gap-1 text-[11px] uppercase tracking-wider text-muted-foreground">
              {stat.label}
              {stat.icon && <stat.icon className={`h-3 w-3 ${stat.tone}`} />}
            </p>
            <AnimatedNumber value={stat.value} format={formatInr} className={`mt-1 block font-display text-lg font-bold sm:text-2xl ${stat.tone}`} />
          </div>
        ))}

        {/* Bar chart */}
        <div className="rounded-xl border border-white/5 bg-white/[0.03] p-4 sm:col-span-2">
          <div className="mb-3 flex items-center justify-between">
            <p className="text-xs font-medium text-muted-foreground">Spending, last 12 months</p>
            <span className="rounded-full bg-income/10 px-2 py-0.5 text-[10px] font-semibold text-income">−8% vs last year</span>
          </div>
          <div className="flex h-28 items-end gap-1.5 sm:h-36">
            {BARS.map((height, index) => (
              <motion.div
                key={index}
                className="flex-1 rounded-t-md bg-gradient-to-t from-brand/30 to-brand"
                initial={{ height: "8%" }}
                whileInView={{ height: `${height}%` }}
                viewport={{ once: true }}
                transition={{ delay: 0.3 + index * 0.05, duration: 0.8, ease: [0.22, 1, 0.36, 1] }}
              />
            ))}
          </div>
        </div>

        {/* Category breakdown */}
        <div className="rounded-xl border border-white/5 bg-white/[0.03] p-4">
          <p className="mb-3 text-xs font-medium text-muted-foreground">Top categories</p>
          <div className="space-y-2.5">
            {CATEGORIES.map((category, index) => (
              <div key={category.name}>
                <div className="mb-1 flex justify-between text-[11px]">
                  <span>{category.name}</span>
                  <span className="text-muted-foreground">{category.pct}%</span>
                </div>
                <div className="h-1.5 overflow-hidden rounded-full bg-white/5">
                  <motion.div
                    className={`h-full rounded-full ${category.color}`}
                    initial={{ width: 0 }}
                    whileInView={{ width: `${category.pct * 2.5}%` }}
                    viewport={{ once: true }}
                    transition={{ delay: 0.5 + index * 0.1, duration: 0.9, ease: [0.22, 1, 0.36, 1] }}
                  />
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* AI insight strip */}
        <div className="flex items-start gap-3 rounded-xl border border-primary/20 bg-primary/[0.07] p-3 sm:col-span-3">
          <span className="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-brand-gradient">
            <Sparkles className="h-3.5 w-3.5 text-brand-foreground" />
          </span>
          <p className="text-xs leading-relaxed text-muted-foreground sm:text-sm">
            <span className="font-semibold text-foreground">Food is up 23% this month.</span> At this pace you&apos;ll
            pass your ₹12,000 budget around the 24th. Want me to suggest a weekly limit?
          </p>
        </div>
      </div>
    </div>
  );
}
