"use client";

import { Badge } from "@/components/ui/badge";
import {
  Wallet,
  PieChart,
  TrendingUp,
  Shield,
  Smartphone,
  Zap,
  ArrowUpRight,
} from "lucide-react";
import { cn } from "@/lib/utils";

const features = [
  {
    icon: Wallet,
    title: "Expense Tracking",
    description:
      "Log income and expenses effortlessly. Categorize transactions and keep track of every rupee with intuitive controls.",
    gradient: "from-emerald-500/20 to-emerald-500/5",
    iconColor: "text-emerald-500",
    iconBg: "bg-emerald-500/10 group-hover:bg-emerald-500",
    span: "md:col-span-2",
  },
  {
    icon: PieChart,
    title: "Visual Analytics",
    description:
      "Beautiful charts and graphs that help you understand your spending patterns at a glance.",
    gradient: "from-blue-500/20 to-blue-500/5",
    iconColor: "text-blue-500",
    iconBg: "bg-blue-500/10 group-hover:bg-blue-500",
    span: "md:col-span-1",
  },
  {
    icon: TrendingUp,
    title: "Budget Insights",
    description:
      "Get intelligent insights about your spending habits and discover ways to save more money.",
    gradient: "from-amber-500/20 to-amber-500/5",
    iconColor: "text-amber-500",
    iconBg: "bg-amber-500/10 group-hover:bg-amber-500",
    span: "md:col-span-1",
  },
  {
    icon: Shield,
    title: "Secure & Private",
    description:
      "Your financial data is encrypted and protected with industry-standard security. We never share your information with third parties.",
    gradient: "from-purple-500/20 to-purple-500/5",
    iconColor: "text-purple-500",
    iconBg: "bg-purple-500/10 group-hover:bg-purple-500",
    span: "md:col-span-2",
  },
  {
    icon: Smartphone,
    title: "Works Everywhere",
    description:
      "Access your budget from any device. Fully responsive design for seamless experience.",
    gradient: "from-pink-500/20 to-pink-500/5",
    iconColor: "text-pink-500",
    iconBg: "bg-pink-500/10 group-hover:bg-pink-500",
    span: "md:col-span-1",
  },
  {
    icon: Zap,
    title: "Lightning Fast",
    description:
      "Built with modern technology for a smooth, instant experience without any lag.",
    gradient: "from-orange-500/20 to-orange-500/5",
    iconColor: "text-orange-500",
    iconBg: "bg-orange-500/10 group-hover:bg-orange-500",
    span: "md:col-span-1",
  },
];

export function FeaturesSection() {
  return (
    <section id="features" className="relative py-20 md:py-28 overflow-hidden">
      {/* Background elements */}
      <div className="absolute inset-0 -z-10">
        <div className="absolute right-0 top-1/4 h-[500px] w-[500px] rounded-full bg-amber-500/5 blur-3xl" />
        <div className="absolute left-0 bottom-1/4 h-[400px] w-[400px] rounded-full bg-orange-500/5 blur-3xl" />
      </div>

      <div className="container px-4 md:px-6">
        {/* Section Header */}
        <div className="mx-auto mb-16 max-w-3xl text-center">
          <Badge
            variant="outline"
            className="mb-4 border-amber-500/30 bg-amber-500/5"
          >
            Features
          </Badge>
          <h2 className="mb-4 text-3xl font-bold tracking-tight sm:text-4xl md:text-5xl">
            Everything you need to manage your{" "}
            <span className="gradient-text">money</span>
          </h2>
          <p className="text-lg text-muted-foreground md:text-xl">
            Powerful features to help you take control of your finances and achieve
            your savings goals.
          </p>
        </div>

        {/* Bento Grid */}
        <div className="grid gap-4 md:grid-cols-3 lg:gap-6">
          {features.map((feature, index) => (
            <div
              key={feature.title}
              className={cn(
                "group relative overflow-hidden rounded-2xl border border-border/50 bg-gradient-to-br backdrop-blur-sm transition-all duration-300 hover:border-border hover:shadow-xl hover:-translate-y-1",
                feature.gradient,
                feature.span
              )}
              style={{
                animationDelay: `${index * 100}ms`,
              }}
            >
              {/* Shimmer effect on hover */}
              <div className="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity duration-500">
                <div className="absolute inset-0 shimmer" />
              </div>

              <div className="relative p-6 md:p-8">
                {/* Icon */}
                <div
                  className={cn(
                    "mb-4 flex h-12 w-12 items-center justify-center rounded-xl transition-all duration-300 group-hover:text-white group-hover:scale-110",
                    feature.iconBg
                  )}
                >
                  <feature.icon className={cn("h-6 w-6", feature.iconColor, "group-hover:text-white")} />
                </div>

                {/* Content */}
                <h3 className="mb-2 text-xl font-semibold tracking-tight">
                  {feature.title}
                </h3>
                <p className="text-muted-foreground leading-relaxed">
                  {feature.description}
                </p>

                {/* Hover arrow */}
                <div className="mt-4 flex items-center text-sm font-medium opacity-0 group-hover:opacity-100 transition-opacity">
                  <span className={feature.iconColor}>Learn more</span>
                  <ArrowUpRight className={cn("ml-1 h-4 w-4", feature.iconColor)} />
                </div>
              </div>

              {/* Corner decoration */}
              <div className="absolute -right-8 -top-8 h-24 w-24 rounded-full bg-gradient-to-br from-white/5 to-transparent" />
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
