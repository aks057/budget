"use client";

import { Button } from "@/components/ui/button";
import { ArrowRight, PiggyBank, Sparkles } from "lucide-react";
import Link from "next/link";
import { useEffect, useState } from "react";
import { createClient } from "@/lib/supabase/client";
import { User } from "@supabase/supabase-js";

export function CTASection() {
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    const supabase = createClient();
    supabase.auth.getUser().then(({ data: { user } }) => {
      setUser(user);
    });
  }, []);

  return (
    <section className="relative py-20 md:py-28 overflow-hidden">
      <div className="container px-4 md:px-6">
        <div className="relative overflow-hidden rounded-3xl">
          {/* Animated gradient background */}
          <div className="absolute inset-0 bg-gradient-to-br from-amber-500 via-orange-500 to-amber-600 animate-gradient" />

          {/* Pattern overlay */}
          <div className="absolute inset-0 bg-grid opacity-10" />

          {/* Glow orbs */}
          <div className="absolute -left-20 -top-20 h-60 w-60 rounded-full bg-white/20 blur-3xl" />
          <div className="absolute -right-20 -bottom-20 h-60 w-60 rounded-full bg-white/20 blur-3xl" />
          <div className="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 h-40 w-40 rounded-full bg-white/10 blur-2xl animate-pulse" />

          {/* Content */}
          <div className="relative flex flex-col items-center py-16 px-6 text-center md:py-24 md:px-12">
            {/* Floating icon */}
            <div className="mb-6 animate-float">
              <div className="flex h-20 w-20 items-center justify-center rounded-2xl bg-white/20 backdrop-blur-sm shadow-lg">
                <PiggyBank className="h-10 w-10 text-white" />
              </div>
            </div>

            {/* Badge */}
            <div className="mb-6 flex items-center gap-2 rounded-full bg-white/20 px-4 py-2 text-sm font-medium text-white backdrop-blur-sm">
              <Sparkles className="h-4 w-4" />
              Start your journey today
            </div>

            {/* Headline */}
            <h2 className="mb-4 max-w-3xl text-3xl font-bold tracking-tight text-white sm:text-4xl md:text-5xl">
              Ready to take control of your finances?
            </h2>

            {/* Description */}
            <p className="mb-8 max-w-xl text-lg text-white/80 md:text-xl">
              Join thousands of users who are already making smarter financial
              decisions with BudWiser. It&apos;s free, secure, and takes just a minute to start.
            </p>

            {/* CTA Buttons */}
            {user ? (
              <Button
                size="lg"
                className="gap-2 bg-white text-amber-600 hover:bg-white/90 shadow-lg shadow-black/20 px-8"
                asChild
              >
                <Link href="/dashboard">
                  Go to Dashboard
                  <ArrowRight className="h-4 w-4" />
                </Link>
              </Button>
            ) : (
              <div className="flex flex-col gap-4 sm:flex-row">
                <Button
                  size="lg"
                  className="gap-2 bg-white text-amber-600 hover:bg-white/90 shadow-lg shadow-black/20 px-8"
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
                  className="border-white/30 bg-white/10 text-white hover:bg-white/20 backdrop-blur-sm px-8"
                  asChild
                >
                  <Link href="/sign-in">Sign In</Link>
                </Button>
              </div>
            )}

            {/* Trust text */}
            <p className="mt-6 text-sm text-white/60">
              No credit card required • Free forever • Cancel anytime
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}
