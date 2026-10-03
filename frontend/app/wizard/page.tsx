"use client";

import { CurrencyComboBox } from "@/components/CurrencyComboBox";
import Logo from "@/components/Logo";
import { Reveal, Stagger, StaggerItem } from "@/components/motion";
import { useCurrentUser } from "@/components/providers/AuthProvider";
import { Button } from "@/components/ui/button";
import { motion } from "framer-motion";
import { ArrowRight, Coins, MessageSquareText, PiggyBank, Wallet } from "lucide-react";
import Link from "next/link";
import React from "react";

const NEXT_STEPS = [
  { icon: Wallet, text: "Add your first income and expense" },
  { icon: PiggyBank, text: "Set a monthly budget for a category" },
  { icon: MessageSquareText, text: "Ask the assistant anything about your money" },
];

function WizardPage() {
  const user = useCurrentUser();
  const firstName = user.fullName.trim().split(/\s+/)[0];

  return (
    <div className="mx-auto flex w-full max-w-xl flex-col items-center gap-8">
      <Reveal onMount>
        <Logo />
      </Reveal>

      <Reveal onMount delay={0.1} className="text-center">
        <h1 className="font-display text-4xl font-bold tracking-tight md:text-5xl">
          Welcome, <span className="gradient-text">{firstName}</span>{" "}
          <motion.span
            className="inline-block origin-[70%_70%]"
            animate={{ rotate: [0, 18, -8, 18, -4, 10, 0] }}
            transition={{ duration: 1.8, delay: 0.6, ease: "easeInOut" }}
            aria-hidden
          >
            👋
          </motion.span>
        </h1>
        <p className="mt-3 text-muted-foreground">One quick setting and you&apos;re ready. You can change it any time.</p>
      </Reveal>

      {/* Two-step indicator */}
      <Reveal onMount delay={0.2} className="flex w-full max-w-xs items-center gap-2 text-xs text-muted-foreground">
        <span className="flex h-6 w-6 items-center justify-center rounded-full bg-primary font-semibold text-primary-foreground shadow-glow">1</span>
        <span className="font-medium text-foreground">Currency</span>
        <span className="h-px flex-1 bg-gradient-to-r from-primary/60 to-border" />
        <span className="flex h-6 w-6 items-center justify-center rounded-full border border-border font-semibold">2</span>
        <span>Start</span>
      </Reveal>

      <Reveal onMount delay={0.3} className="w-full">
        <div className="glass-card p-6">
          <div className="mb-4 flex items-center gap-3">
            <span className="flex h-10 w-10 items-center justify-center rounded-xl border border-brand-2/20 bg-brand-2/10">
              <Coins className="h-5 w-5 text-brand-2" />
            </span>
            <div>
              <p className="font-display font-semibold">Your currency</p>
              <p className="text-sm text-muted-foreground">Every amount in the app is shown in it</p>
            </div>
          </div>
          <CurrencyComboBox />
        </div>
      </Reveal>

      <div className="w-full">
        <p className="mb-3 text-center text-xs font-semibold uppercase tracking-widest text-muted-foreground">What&apos;s next</p>
        <Stagger onMount delay={0.45} className="grid gap-2">
          {NEXT_STEPS.map((step) => (
            <StaggerItem key={step.text}>
              <div className="flex items-center gap-3 rounded-xl border border-border/60 bg-card/50 px-4 py-3 text-sm backdrop-blur">
                <step.icon className="h-4 w-4 text-primary" />
                {step.text}
              </div>
            </StaggerItem>
          ))}
        </Stagger>
      </div>

      <Reveal onMount delay={0.7} className="w-full">
        <Button size="lg" variant="gradient" className="w-full gap-2" asChild>
          <Link href="/dashboard">
            Take me to my dashboard <ArrowRight className="h-4 w-4" />
          </Link>
        </Button>
      </Reveal>
    </div>
  );
}

export default WizardPage;
