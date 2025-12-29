"use client";

import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { ArrowRight, Sparkles, TrendingUp, Shield, Zap } from "lucide-react";
import Link from "next/link";
import { useEffect, useState } from "react";
import { createClient } from "@/lib/supabase/client";
import { User } from "@supabase/supabase-js";

export function HeroSection() {
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    const supabase = createClient();
    supabase.auth.getUser().then(({ data: { user } }) => {
      setUser(user);
    });
  }, []);

  return (
    <section className="relative min-h-[90vh] overflow-hidden">
      {/* Animated background gradients */}
      <div className="absolute inset-0 -z-10">
        {/* Primary gradient orb */}
        <div className="absolute left-1/2 top-0 -translate-x-1/2 -translate-y-1/2">
          <div className="h-[600px] w-[600px] rounded-full bg-gradient-to-r from-amber-500/20 via-orange-500/20 to-amber-500/20 blur-3xl animate-pulse" />
        </div>
        {/* Secondary gradient orbs */}
        <div className="absolute right-0 top-1/4">
          <div className="h-[400px] w-[400px] rounded-full bg-amber-500/10 blur-3xl animate-float" />
        </div>
        <div className="absolute left-0 bottom-0">
          <div className="h-[300px] w-[300px] rounded-full bg-orange-500/10 blur-3xl" />
        </div>
        {/* Grid pattern */}
        <div className="absolute inset-0 bg-grid opacity-50" />
      </div>

      <div className="container relative px-4 md:px-6 pt-20 pb-16 md:pt-32 md:pb-24">
        <div className="flex flex-col items-center space-y-8 text-center">
          {/* Announcement Badge */}
          <div className="animate-fade-up">
            <Badge
              variant="outline"
              className="gap-2 px-4 py-2 border-amber-500/30 bg-amber-500/5 backdrop-blur-sm hover:bg-amber-500/10 transition-colors cursor-default"
            >
              <Sparkles className="h-3.5 w-3.5 text-amber-500" />
              <span className="text-sm font-medium">Smart budgeting made simple</span>
              <ArrowRight className="h-3.5 w-3.5 text-muted-foreground" />
            </Badge>
          </div>

          {/* Main Headline */}
          <div className="space-y-4 max-w-4xl">
            <h1 className="animate-fade-up animation-delay-100 text-4xl font-bold tracking-tight sm:text-5xl md:text-6xl lg:text-7xl">
              Take Control of Your{" "}
              <span className="relative">
                <span className="gradient-text">Finances</span>
                <svg
                  className="absolute -bottom-2 left-0 w-full"
                  viewBox="0 0 300 12"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    d="M2 10C50 4 100 2 150 6C200 10 250 4 298 8"
                    stroke="url(#gradient)"
                    strokeWidth="3"
                    strokeLinecap="round"
                    className="animate-pulse"
                  />
                  <defs>
                    <linearGradient id="gradient" x1="0%" y1="0%" x2="100%" y2="0%">
                      <stop offset="0%" stopColor="#f59e0b" />
                      <stop offset="100%" stopColor="#f97316" />
                    </linearGradient>
                  </defs>
                </svg>
              </span>
            </h1>

            <p className="animate-fade-up animation-delay-200 mx-auto max-w-2xl text-lg text-muted-foreground md:text-xl leading-relaxed">
              Track expenses, manage categories, and visualize your spending habits.
              BudWiser helps you make smarter financial decisions with powerful
              insights and intuitive tools.
            </p>
          </div>

          {/* CTA Buttons */}
          <div className="animate-fade-up animation-delay-300 flex flex-col gap-4 sm:flex-row">
            {user ? (
              <Button
                size="lg"
                className="gap-2 bg-gradient-to-r from-amber-500 to-orange-500 hover:from-amber-600 hover:to-orange-600 text-white shadow-lg shadow-amber-500/25 hover:shadow-amber-500/40 transition-all duration-300 px-8"
                asChild
              >
                <Link href="/dashboard">
                  Go to Dashboard
                  <ArrowRight className="h-4 w-4" />
                </Link>
              </Button>
            ) : (
              <>
                <Button
                  size="lg"
                  className="gap-2 bg-gradient-to-r from-amber-500 to-orange-500 hover:from-amber-600 hover:to-orange-600 text-white shadow-lg shadow-amber-500/25 hover:shadow-amber-500/40 transition-all duration-300 px-8"
                  asChild
                >
                  <Link href="/sign-up">
                    Get Started Free
                    <ArrowRight className="h-4 w-4" />
                  </Link>
                </Button>
                <Button
                  size="lg"
                  variant="outline"
                  className="border-border/50 hover:bg-accent/50 backdrop-blur-sm px-8"
                  asChild
                >
                  <Link href="#features">Learn More</Link>
                </Button>
              </>
            )}
          </div>

          {/* Trust Indicators */}
          <div className="animate-fade-up animation-delay-400 flex flex-col items-center gap-4 pt-4">
            <p className="text-sm text-muted-foreground">
              No credit card required • Free forever for personal use
            </p>
            <div className="flex items-center gap-6 text-sm text-muted-foreground">
              <div className="flex items-center gap-2">
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-emerald-500/10">
                  <Shield className="h-4 w-4 text-emerald-500" />
                </div>
                <span>Secure</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-blue-500/10">
                  <Zap className="h-4 w-4 text-blue-500" />
                </div>
                <span>Fast</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-amber-500/10">
                  <TrendingUp className="h-4 w-4 text-amber-500" />
                </div>
                <span>Insightful</span>
              </div>
            </div>
          </div>

          {/* Hero Visual/Preview */}
          <div className="animate-fade-up animation-delay-500 relative mt-8 w-full max-w-5xl">
            <div className="relative rounded-xl border border-border/50 bg-gradient-to-b from-background to-muted/20 p-2 shadow-2xl backdrop-blur-sm">
              {/* Browser chrome */}
              <div className="flex items-center gap-2 border-b border-border/50 px-4 py-3">
                <div className="flex gap-1.5">
                  <div className="h-3 w-3 rounded-full bg-red-500/80" />
                  <div className="h-3 w-3 rounded-full bg-yellow-500/80" />
                  <div className="h-3 w-3 rounded-full bg-green-500/80" />
                </div>
                <div className="flex-1 flex justify-center">
                  <div className="flex items-center gap-2 rounded-md bg-muted/50 px-3 py-1 text-xs text-muted-foreground">
                    <Shield className="h-3 w-3" />
                    budwiser.app/dashboard
                  </div>
                </div>
              </div>
              {/* Dashboard preview */}
              <div className="relative aspect-[16/9] overflow-hidden rounded-b-lg bg-muted/30">
                <div className="absolute inset-0 bg-gradient-to-br from-amber-500/5 to-orange-500/5" />
                <div className="p-6 space-y-4">
                  {/* Stats row */}
                  <div className="grid grid-cols-3 gap-4">
                    {[
                      { label: "Total Balance", value: "₹1,24,500", color: "text-foreground" },
                      { label: "Income", value: "+₹85,000", color: "text-emerald-500" },
                      { label: "Expenses", value: "-₹42,300", color: "text-red-500" },
                    ].map((stat, i) => (
                      <div
                        key={i}
                        className="rounded-lg border border-border/30 bg-card/50 backdrop-blur-sm p-4"
                      >
                        <p className="text-xs text-muted-foreground">{stat.label}</p>
                        <p className={`text-xl font-bold ${stat.color}`}>{stat.value}</p>
                      </div>
                    ))}
                  </div>
                  {/* Chart placeholder */}
                  <div className="rounded-lg border border-border/30 bg-card/50 backdrop-blur-sm p-4 h-32">
                    <div className="flex items-end justify-between h-full gap-2">
                      {[40, 65, 45, 80, 55, 70, 90, 60, 75, 85, 50, 95].map((height, i) => (
                        <div
                          key={i}
                          className="flex-1 rounded-t bg-gradient-to-t from-amber-500/50 to-amber-500/80"
                          style={{ height: `${height}%` }}
                        />
                      ))}
                    </div>
                  </div>
                </div>
                {/* Overlay gradient */}
                <div className="absolute inset-x-0 bottom-0 h-24 bg-gradient-to-t from-background to-transparent" />
              </div>
            </div>
            {/* Glow effect behind preview */}
            <div className="absolute -inset-4 -z-10 rounded-2xl bg-gradient-to-r from-amber-500/20 via-orange-500/20 to-amber-500/20 blur-2xl opacity-50" />
          </div>
        </div>
      </div>
    </section>
  );
}
