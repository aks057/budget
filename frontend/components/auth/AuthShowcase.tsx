"use client";

import Logo from "@/components/Logo";
import { Aurora, EASE_OUT } from "@/components/motion";
import { AnimatePresence, motion } from "framer-motion";
import { BellRing, CheckCircle2, Sparkles, Target } from "lucide-react";
import { useEffect, useState } from "react";

const CARDS = [
  { icon: Sparkles, tone: "text-primary", title: "Food is up 23% this month", body: "Most of it is 6 delivery orders over ₹600." },
  { icon: BellRing, tone: "text-warning", title: "Rent budget at 100%", body: "Paid in full, so nothing left to plan for." },
  { icon: Target, tone: "text-brand-2", title: "Trip goal on track", body: "₹8,000/month gets you there by December." },
  { icon: CheckCircle2, tone: "text-primary", title: "₹450 lunch logged", body: "Added after you confirmed. Nothing else changed." },
];

/** Left half of the auth screen: brand, promise, and a rotating stack of example insights. */
export function AuthShowcase() {
  const [index, setIndex] = useState(0);
  useEffect(() => {
    const timer = setInterval(() => setIndex((i) => (i + 1) % CARDS.length), 3200);
    return () => clearInterval(timer);
  }, []);

  return (
    <div className="relative hidden h-full flex-col justify-between overflow-hidden border-r border-border/60 p-10 lg:flex">
      <Aurora intensity="strong" />
      <div className="relative">
        <Logo />
      </div>

      <div className="relative">
        <h2 className="max-w-md text-balance font-display text-4xl font-bold leading-tight tracking-tight xl:text-5xl">
          Your money, <span className="gradient-text">finally explained.</span>
        </h2>
        <p className="mt-4 max-w-md text-muted-foreground">
          Track spending, set budgets and goals, and ask an AI assistant that answers from your real numbers.
        </p>

        {/* Stacked cards: the front one rotates out, the stack moves up. */}
        <div className="relative mt-12 h-44 max-w-md">
          <AnimatePresence initial={false}>
            {[0, 1, 2].map((depth) => {
              const card = CARDS[(index + depth) % CARDS.length];
              return (
                <motion.div
                  key={card.title}
                  className="glass-card absolute inset-x-0 top-0 p-5"
                  style={{ zIndex: 3 - depth }}
                  initial={{ opacity: 0, y: 40, scale: 0.9 }}
                  animate={{ opacity: 1 - depth * 0.3, y: depth * 14, scale: 1 - depth * 0.05 }}
                  exit={{ opacity: 0, y: -30, scale: 1.02, filter: "blur(4px)" }}
                  transition={{ duration: 0.6, ease: EASE_OUT }}
                >
                  <div className="flex items-start gap-3">
                    <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl border border-white/10 bg-white/5">
                      <card.icon className={`h-4 w-4 ${card.tone}`} />
                    </span>
                    <div>
                      <p className="font-semibold">{card.title}</p>
                      <p className="text-sm text-muted-foreground">{card.body}</p>
                    </div>
                  </div>
                </motion.div>
              );
            })}
          </AnimatePresence>
        </div>
      </div>

      <p className="relative text-xs text-muted-foreground">Example insights. Your own appear once you add transactions.</p>
    </div>
  );
}
