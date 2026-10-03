"use client";

import { LogoMark } from "@/components/Logo";
import { Reveal } from "@/components/motion";
import { useAuth } from "@/components/providers/AuthProvider";
import { Button } from "@/components/ui/button";
import { ArrowRight } from "lucide-react";
import Link from "next/link";

export function CTASection() {
  const { user } = useAuth();

  return (
    <section className="relative py-24 md:py-32">
      <div className="container">
        <Reveal>
          <div className="relative isolate overflow-hidden rounded-[2rem] border border-primary/20 bg-card/60 px-6 py-16 text-center backdrop-blur-xl md:px-12 md:py-24">
            {/* Rotating conic glow behind the content */}
            <div aria-hidden className="absolute left-1/2 top-1/2 -z-10 h-[140%] w-[140%] -translate-x-1/2 -translate-y-1/2">
              <div className="h-full w-full animate-border-spin bg-[conic-gradient(from_0deg,transparent_0deg,hsl(var(--brand)/0.25)_60deg,transparent_120deg,hsl(var(--brand-2)/0.2)_220deg,transparent_300deg)] blur-2xl" />
            </div>
            <div aria-hidden className="absolute inset-0 -z-10 bg-grid mask-radial opacity-60" />

            <LogoMark className="mx-auto mb-8 h-14 w-14 rounded-2xl" />
            <h2 className="mx-auto max-w-3xl text-balance font-display text-4xl font-bold tracking-tight md:text-6xl">
              Stop guessing. <span className="gradient-text">Start knowing.</span>
            </h2>
            <p className="mx-auto mt-5 max-w-xl text-lg text-muted-foreground">
              Set up in two minutes. Free for personal use, and nothing changes without your OK.
            </p>
            <div className="mt-10 flex flex-col justify-center gap-3 sm:flex-row">
              <Button size="lg" variant="gradient" className="gap-2 px-8" asChild>
                <Link href={user ? "/dashboard" : "/sign-up"}>
                  {user ? "Open dashboard" : "Create free account"}
                  <ArrowRight className="h-4 w-4" />
                </Link>
              </Button>
              {!user && (
                <Button size="lg" variant="outline" className="px-8" asChild>
                  <Link href="/sign-in">Sign in</Link>
                </Button>
              )}
            </div>
          </div>
        </Reveal>
      </div>
    </section>
  );
}
