"use client";

import { Badge } from "@/components/ui/badge";
import { UserPlus, Receipt, LineChart, ArrowRight } from "lucide-react";
import { cn } from "@/lib/utils";

const steps = [
  {
    icon: UserPlus,
    step: "01",
    title: "Create Account",
    description:
      "Sign up in seconds with just your email or Google account. No credit card required to get started.",
    color: "from-emerald-500 to-emerald-600",
    iconBg: "bg-emerald-500/10 group-hover:bg-emerald-500",
    iconColor: "text-emerald-500 group-hover:text-white",
  },
  {
    icon: Receipt,
    step: "02",
    title: "Add Transactions",
    description:
      "Log your income and expenses with our intuitive interface. Categorize them for better organization and tracking.",
    color: "from-amber-500 to-orange-500",
    iconBg: "bg-amber-500/10 group-hover:bg-amber-500",
    iconColor: "text-amber-500 group-hover:text-white",
  },
  {
    icon: LineChart,
    step: "03",
    title: "Get Insights",
    description:
      "View beautiful charts and analytics to understand your spending patterns and make smarter decisions.",
    color: "from-blue-500 to-blue-600",
    iconBg: "bg-blue-500/10 group-hover:bg-blue-500",
    iconColor: "text-blue-500 group-hover:text-white",
  },
];

export function HowItWorks() {
  return (
    <section className="relative py-20 md:py-28 overflow-hidden">
      {/* Background */}
      <div className="absolute inset-0 -z-10">
        <div className="absolute inset-0 bg-dots opacity-50" />
      </div>

      <div className="container px-4 md:px-6">
        {/* Header */}
        <div className="mx-auto mb-16 max-w-3xl text-center">
          <Badge
            variant="outline"
            className="mb-4 border-amber-500/30 bg-amber-500/5"
          >
            How It Works
          </Badge>
          <h2 className="mb-4 text-3xl font-bold tracking-tight sm:text-4xl md:text-5xl">
            Get started in{" "}
            <span className="gradient-text">three simple steps</span>
          </h2>
          <p className="text-lg text-muted-foreground md:text-xl">
            It takes less than a minute to start tracking your finances with
            BudWiser.
          </p>
        </div>

        {/* Steps */}
        <div className="relative max-w-5xl mx-auto">
          {/* Connection line - desktop */}
          <div className="absolute left-0 right-0 top-24 hidden h-0.5 bg-gradient-to-r from-emerald-500/50 via-amber-500/50 to-blue-500/50 lg:block" />

          {/* Connection line dots */}
          <div className="absolute left-0 right-0 top-24 hidden lg:flex justify-between px-[calc(16.67%-10px)]">
            {[0, 1].map((i) => (
              <ArrowRight key={i} className="h-4 w-4 text-muted-foreground/50" />
            ))}
          </div>

          <div className="grid gap-8 lg:grid-cols-3">
            {steps.map((item, index) => (
              <div
                key={item.title}
                className="group relative"
                style={{ animationDelay: `${index * 150}ms` }}
              >
                {/* Card */}
                <div className="relative overflow-hidden rounded-2xl border border-border/50 bg-gradient-to-br from-background to-muted/20 p-6 backdrop-blur-sm transition-all duration-300 hover:border-border hover:shadow-xl hover:-translate-y-1">
                  {/* Step number badge */}
                  <div className="absolute -top-3 left-6">
                    <div className={cn(
                      "flex h-7 w-12 items-center justify-center rounded-full bg-gradient-to-r text-xs font-bold text-white shadow-lg",
                      item.color
                    )}>
                      Step {item.step}
                    </div>
                  </div>

                  {/* Content */}
                  <div className="pt-4">
                    {/* Icon */}
                    <div
                      className={cn(
                        "mb-4 flex h-16 w-16 items-center justify-center rounded-2xl transition-all duration-300 group-hover:scale-110",
                        item.iconBg
                      )}
                    >
                      <item.icon className={cn("h-8 w-8 transition-colors", item.iconColor)} />
                    </div>

                    {/* Title */}
                    <h3 className="mb-2 text-xl font-semibold tracking-tight">
                      {item.title}
                    </h3>

                    {/* Description */}
                    <p className="text-muted-foreground leading-relaxed">
                      {item.description}
                    </p>
                  </div>

                  {/* Shimmer effect */}
                  <div className="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity duration-500">
                    <div className="absolute inset-0 shimmer" />
                  </div>

                  {/* Decorative gradient */}
                  <div className={cn(
                    "absolute -right-12 -top-12 h-32 w-32 rounded-full bg-gradient-to-br opacity-0 group-hover:opacity-20 transition-opacity blur-2xl",
                    item.color
                  )} />
                </div>

                {/* Mobile connector */}
                {index < steps.length - 1 && (
                  <div className="flex justify-center py-4 lg:hidden">
                    <div className="h-8 w-0.5 bg-gradient-to-b from-border to-transparent" />
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
