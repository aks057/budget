"use client";

import { SectionHeading } from "@/components/landing/SectionHeading";
import { EASE_OUT, Reveal } from "@/components/motion";
import { AnimatePresence, motion, useInView, useReducedMotion } from "framer-motion";
import { Bot, Check, CheckCircle2, Loader2, ShieldQuestion, Sparkles, X } from "lucide-react";
import { useEffect, useRef, useState } from "react";

/**
 * Scripted replay of a real agent turn (same tools and confirm flow as the app), so visitors see how it works
 * without an account. Plays when scrolled into view and loops; reduced-motion users see the final state.
 */
const QUESTION = "How much did I spend on food this month vs last month?";
const ANSWER =
  "You've spent ₹9,840 on Food so far in October, 23% more than September (₹8,000). Most of the jump comes from 6 delivery orders over ₹600. Want me to set a ₹10,000 monthly Food budget?";
const FOLLOW_UP = "Yes, set it";
const STEPS = ["Breaking down spending by category", "Comparing with last month"];

// Timeline (ms from start) of what is visible.
const T = { question: 300, step1: 1100, step1Done: 1900, step2: 2100, step2Done: 2900, answer: 3100, followUp: 7600, pending: 8400, confirm: 10200, done: 10900, restart: 15500 };

function useTimeline(active: boolean, reduceMotion: boolean) {
  const [elapsed, setElapsed] = useState(reduceMotion ? T.done : 0);
  useEffect(() => {
    if (!active || reduceMotion) return;
    let start = performance.now();
    let frame = 0;
    const tick = (now: number) => {
      let t = now - start;
      if (t > T.restart) {
        start = now;
        t = 0;
      }
      setElapsed(t);
      frame = requestAnimationFrame(tick);
    };
    frame = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(frame);
  }, [active, reduceMotion]);
  return elapsed;
}

export function AgentDemo() {
  const ref = useRef<HTMLDivElement>(null);
  const inView = useInView(ref, { margin: "-20% 0px" });
  const reduceMotion = !!useReducedMotion();
  const t = useTimeline(inView, reduceMotion);

  // Typewriter for the answer: ~55 characters per second.
  const typed = t < T.answer ? "" : ANSWER.slice(0, Math.floor(((t - T.answer) / 1000) * 55));
  const answerDone = typed.length >= ANSWER.length;

  return (
    <section id="ai-demo" className="relative scroll-mt-24 py-24 md:py-32">
      <div className="container">
        <SectionHeading
          eyebrow="The AI assistant"
          icon={Sparkles}
          title={
            <>
              Ask in plain English. <span className="gradient-text">Get answers from your data.</span>
            </>
          }
          subtitle="The assistant never guesses. It calls tools that query your actual transactions, shows every step, and asks before it changes anything."
        />

        <Reveal>
          <div ref={ref} className="relative mx-auto max-w-3xl">
            <div aria-hidden className="absolute -inset-8 -z-10 rounded-[2.5rem] bg-brand-gradient opacity-15 blur-3xl" />
            <div className="glass-card overflow-hidden">
              {/* Header */}
              <div className="flex items-center gap-3 border-b border-white/5 px-5 py-3.5">
                <span className="flex h-8 w-8 items-center justify-center rounded-full bg-brand-gradient shadow-glow">
                  <Bot className="h-4 w-4 text-brand-foreground" />
                </span>
                <div>
                  <p className="text-sm font-semibold">Bud-Wiser AI</p>
                  <p className="flex items-center gap-1.5 text-xs text-muted-foreground">
                    <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-primary" /> Live demo · sample data
                  </p>
                </div>
              </div>

              {/* Conversation */}
              <div className="min-h-[440px] space-y-5 p-5 sm:p-6" aria-live="off">
                <AnimatePresence>
                  {t >= T.question && <UserBubble key="q">{QUESTION}</UserBubble>}
                  {t >= T.step1 && (
                    <motion.div key="a1" initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="flex gap-3">
                      <AiAvatar />
                      <div className="min-w-0 flex-1 space-y-3 pt-1">
                        <ol className="space-y-1.5">
                          <Step label={STEPS[0]} done={t >= T.step1Done} />
                          {t >= T.step2 && <Step label={STEPS[1]} done={t >= T.step2Done} />}
                        </ol>
                        {typed && (
                          <p className="text-sm leading-relaxed">
                            {typed}
                            {!answerDone && <span className="ml-0.5 inline-block h-4 w-1.5 translate-y-0.5 animate-pulse bg-primary" />}
                          </p>
                        )}
                      </div>
                    </motion.div>
                  )}
                  {t >= T.followUp && <UserBubble key="f">{FOLLOW_UP}</UserBubble>}
                  {t >= T.pending && (
                    <motion.div key="a2" initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="flex gap-3">
                      <AiAvatar />
                      <div className="min-w-0 flex-1 space-y-3 pt-1">
                        <ol>
                          <Step label="Preparing a budget" done pending />
                        </ol>
                        <ConfirmCard confirming={t >= T.confirm && t < T.done} done={t >= T.done} />
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>
            </div>
          </div>
        </Reveal>
      </div>
    </section>
  );
}

function UserBubble({ children }: { children: React.ReactNode }) {
  return (
    <motion.div initial={{ opacity: 0, y: 10, scale: 0.98 }} animate={{ opacity: 1, y: 0, scale: 1 }} transition={{ ease: EASE_OUT }} className="flex justify-end">
      <div className="max-w-[85%] rounded-2xl rounded-br-sm bg-primary px-4 py-2.5 text-sm text-primary-foreground shadow-glow">{children}</div>
    </motion.div>
  );
}

function AiAvatar() {
  return (
    <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-brand-gradient">
      <Sparkles className="h-4 w-4 text-brand-foreground" />
    </span>
  );
}

function Step({ label, done, pending = false }: { label: string; done: boolean; pending?: boolean }) {
  return (
    <motion.li initial={{ opacity: 0, x: -6 }} animate={{ opacity: 1, x: 0 }} className="flex items-center gap-2 text-xs text-muted-foreground">
      {!done ? (
        <Loader2 className="h-3.5 w-3.5 animate-spin" />
      ) : pending ? (
        <ShieldQuestion className="h-3.5 w-3.5 text-warning" />
      ) : (
        <CheckCircle2 className="h-3.5 w-3.5 text-primary" />
      )}
      {label}
      {pending && done && <span className="text-warning">· needs your OK</span>}
    </motion.li>
  );
}

function ConfirmCard({ confirming, done }: { confirming: boolean; done: boolean }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      className={`rounded-xl border p-3.5 transition-colors ${done ? "border-primary/30 bg-primary/[0.07]" : "border-warning/40 bg-warning/[0.06]"}`}
    >
      <p className="text-sm font-semibold">Create budget</p>
      <p className="text-sm text-muted-foreground">Create a monthly budget of ₹10,000.00 for Food</p>
      <AnimatePresence mode="wait" initial={false}>
        {done ? (
          <motion.p key="done" initial={{ opacity: 0, y: 4 }} animate={{ opacity: 1, y: 0 }} className="mt-2.5 flex items-center gap-1.5 text-sm text-primary">
            <CheckCircle2 className="h-4 w-4" /> Budget created
          </motion.p>
        ) : (
          <motion.div key="buttons" exit={{ opacity: 0 }} className="mt-3 flex gap-2">
            <motion.span
              animate={confirming ? { scale: [1, 0.94, 1] } : { boxShadow: ["0 0 0 0 hsl(var(--glow)/0.0)", "0 0 0 6px hsl(var(--glow)/0.25)", "0 0 0 0 hsl(var(--glow)/0.0)"] }}
              transition={confirming ? { duration: 0.3 } : { duration: 1.6, repeat: Infinity }}
              className="inline-flex items-center gap-1 rounded-lg bg-primary px-3 py-1.5 text-xs font-semibold text-primary-foreground"
            >
              {confirming ? <Loader2 className="h-3.5 w-3.5 animate-spin" /> : <Check className="h-3.5 w-3.5" />} Confirm
            </motion.span>
            <span className="inline-flex items-center gap-1 rounded-lg border px-3 py-1.5 text-xs text-muted-foreground">
              <X className="h-3.5 w-3.5" /> Cancel
            </span>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.div>
  );
}
