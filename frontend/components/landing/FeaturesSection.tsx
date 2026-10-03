"use client";

import { SectionHeading } from "@/components/landing/SectionHeading";
import { SpotlightCard, Stagger, StaggerItem } from "@/components/motion";
import { cn } from "@/lib/utils";
import { motion } from "framer-motion";
import { BellRing, LayoutGrid, LineChart, MessageSquareText, PiggyBank, Repeat, Target, Wallet, type LucideIcon } from "lucide-react";
import type { ReactNode } from "react";

interface Feature {
  icon: LucideIcon;
  title: string;
  description: string;
  className?: string;
  visual?: ReactNode;
}

const features: Feature[] = [
  {
    icon: MessageSquareText,
    title: "An assistant that knows your numbers",
    description:
      "Ask why spending went up, what you can afford, or tell it what you spent. Answers come from tool calls on your own data, so nothing is invented.",
    className: "md:col-span-2",
    visual: <ChatVisual />,
  },
  {
    icon: BellRing,
    title: "Alerts before it's too late",
    description: "A daily check flags budgets near their limit, unusual spikes, big one-off expenses and bills due soon.",
    visual: <AlertsVisual />,
  },
  {
    icon: PiggyBank,
    title: "Budgets that warn early",
    description: "Monthly limits per category with live progress, a warning at 80% and a projection of where the month will land.",
    visual: <BudgetVisual />,
  },
  {
    icon: Target,
    title: "Goals with a real plan",
    description: "Set a target and date; Bud-Wiser works out the monthly amount and tells you if your savings pace falls behind.",
    visual: <GoalVisual />,
  },
  {
    icon: LineChart,
    title: "Analytics that explain",
    description: "Trends, category breakdowns, month-over-month comparisons and forecasts, all computed on the server.",
  },
  {
    icon: Repeat,
    title: "Recurring expense detection",
    description: "Subscriptions and bills are detected from your history, with the next expected date.",
  },
  {
    icon: Wallet,
    title: "Fast manual tracking",
    description: "Log income and expenses in seconds, with categories, filters and CSV export. No bank linking needed.",
  },
];

export function FeaturesSection() {
  return (
    <section id="features" className="relative scroll-mt-24 py-24 md:py-32">
      <div className="container">
        <SectionHeading
          eyebrow="Features"
          icon={LayoutGrid}
          title={
            <>
              Everything you need to <span className="gradient-text">stay ahead of your money</span>
            </>
          }
          subtitle="Tracking, budgets, goals and analytics, plus an AI layer that turns them into answers and timely nudges."
        />

        <Stagger className="grid gap-4 md:grid-cols-3" stagger={0.07}>
          {features.map((feature) => (
            <StaggerItem key={feature.title} className={cn(feature.className)}>
              <SpotlightCard className="h-full p-6">
                <div className="mb-4 flex h-11 w-11 items-center justify-center rounded-xl border border-primary/20 bg-primary/10">
                  <feature.icon className="h-5 w-5 text-primary" />
                </div>
                <h3 className="mb-2 font-display text-lg font-semibold">{feature.title}</h3>
                <p className="text-sm leading-relaxed text-muted-foreground">{feature.description}</p>
                {feature.visual && <div className="mt-6">{feature.visual}</div>}
              </SpotlightCard>
            </StaggerItem>
          ))}
        </Stagger>
      </div>
    </section>
  );
}

function ChatVisual() {
  return (
    <div className="space-y-2 rounded-xl border border-white/5 bg-background/40 p-4 text-xs">
      <div className="ml-auto w-fit rounded-xl rounded-br-sm bg-primary/90 px-3 py-1.5 text-primary-foreground">Can I afford a ₹40k trip in December?</div>
      <div className="flex flex-wrap gap-1.5 text-[11px] text-muted-foreground">
        {["get_monthly_summary", "get_goals", "get_spending_trends"].map((tool, index) => (
          <motion.span
            key={tool}
            initial={{ opacity: 0, y: 4 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ delay: 0.3 + index * 0.2 }}
            className="rounded-md border border-primary/20 bg-primary/10 px-1.5 py-0.5 font-mono text-primary"
          >
            {tool}
          </motion.span>
        ))}
      </div>
      <div className="w-fit max-w-[90%] rounded-xl rounded-bl-sm bg-white/5 px-3 py-1.5 text-foreground">
        Yes, if you save ₹13,400/month from now. You&apos;re averaging ₹15,200, so you&apos;re on track.
      </div>
    </div>
  );
}

function AlertsVisual() {
  const alerts = [
    { text: "Food budget at 85%", tone: "bg-warning" },
    { text: "Netflix due tomorrow", tone: "bg-sky-400" },
    { text: "Shopping up 41%", tone: "bg-expense" },
  ];
  return (
    <div className="space-y-2">
      {alerts.map((alert, index) => (
        <motion.div
          key={alert.text}
          initial={{ opacity: 0, x: 12 }}
          whileInView={{ opacity: 1, x: 0 }}
          viewport={{ once: true }}
          transition={{ delay: 0.2 + index * 0.15 }}
          className="flex items-center gap-2 rounded-lg border border-white/5 bg-background/40 px-3 py-2 text-xs"
        >
          <span className={`h-1.5 w-1.5 rounded-full ${alert.tone}`} />
          {alert.text}
        </motion.div>
      ))}
    </div>
  );
}

function BudgetVisual() {
  const rows = [
    { name: "Food", pct: 85, tone: "bg-warning" },
    { name: "Travel", pct: 42, tone: "bg-primary" },
    { name: "Rent", pct: 100, tone: "bg-cyan-400" },
  ];
  return (
    <div className="space-y-3">
      {rows.map((row, index) => (
        <div key={row.name} className="text-xs">
          <div className="mb-1 flex justify-between">
            <span>{row.name}</span>
            <span className="text-muted-foreground">{row.pct}%</span>
          </div>
          <div className="h-1.5 overflow-hidden rounded-full bg-white/5">
            <motion.div
              className={`h-full rounded-full ${row.tone}`}
              initial={{ width: 0 }}
              whileInView={{ width: `${row.pct}%` }}
              viewport={{ once: true }}
              transition={{ delay: 0.2 + index * 0.12, duration: 0.9, ease: [0.22, 1, 0.36, 1] }}
            />
          </div>
        </div>
      ))}
    </div>
  );
}

function GoalVisual() {
  const circumference = 2 * Math.PI * 34;
  return (
    <div className="flex items-center gap-4">
      <svg viewBox="0 0 80 80" className="h-20 w-20 -rotate-90">
        <circle cx="40" cy="40" r="34" fill="none" stroke="hsl(var(--foreground) / 0.08)" strokeWidth="7" />
        <motion.circle
          cx="40"
          cy="40"
          r="34"
          fill="none"
          stroke="url(#goal-gradient)"
          strokeWidth="7"
          strokeLinecap="round"
          strokeDasharray={circumference}
          initial={{ strokeDashoffset: circumference }}
          whileInView={{ strokeDashoffset: circumference * 0.32 }}
          viewport={{ once: true }}
          transition={{ duration: 1.4, ease: [0.22, 1, 0.36, 1] }}
        />
        <defs>
          <linearGradient id="goal-gradient" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="hsl(var(--brand))" />
            <stop offset="100%" stopColor="hsl(var(--brand-2))" />
          </linearGradient>
        </defs>
      </svg>
      <div className="text-xs">
        <p className="font-semibold">Emergency fund</p>
        <p className="text-muted-foreground">₹2,04,000 of ₹3,00,000</p>
        <p className="mt-1 text-primary">₹8,000/month to finish on time</p>
      </div>
    </div>
  );
}
