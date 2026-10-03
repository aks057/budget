"use client";

import { SectionHeading } from "@/components/landing/SectionHeading";
import { Reveal, SpotlightCard, Stagger, StaggerItem } from "@/components/motion";
import { motion, useReducedMotion } from "framer-motion";
import { Bot, Database, Fingerprint, KeyRound, ListChecks, ShieldCheck, Gauge, User, Wrench } from "lucide-react";

const PIPELINE = [
  { icon: User, label: "You", detail: "Ask or instruct" },
  { icon: Bot, label: "AI agent", detail: "Plans tool calls" },
  { icon: Wrench, label: "Validated tools", detail: "Schema + rules check" },
  { icon: Database, label: "Your data", detail: "Scoped to your account" },
];

const GUARANTEES = [
  {
    icon: ShieldCheck,
    title: "Confirm before write",
    body: "The agent can only propose changes. Nothing is saved until you press Confirm, and every proposal expires.",
  },
  {
    icon: Fingerprint,
    title: "The model never sees the database",
    body: "It calls a fixed set of typed tools. Your user id comes from your session, never from the model.",
  },
  {
    icon: KeyRound,
    title: "Short-lived sessions",
    body: "15-minute access tokens in memory, httpOnly refresh cookies with rotation and reuse detection.",
  },
  {
    icon: ListChecks,
    title: "Every step is audited",
    body: "Each tool call is logged with its arguments, result and latency, and shown to you as it happens.",
  },
  {
    icon: Gauge,
    title: "Fails gracefully",
    body: "Rate limits, a circuit breaker per AI provider and an automatic fallback model keep the app usable.",
  },
];

export function ArchitectureSection() {
  const reduceMotion = useReducedMotion();
  return (
    <section id="security" className="relative scroll-mt-24 py-24 md:py-32">
      <div className="container">
        <SectionHeading
          eyebrow="Under the hood"
          icon={ShieldCheck}
          title={
            <>
              AI that&apos;s <span className="gradient-text">powerful and safe</span>
            </>
          }
          subtitle="An agent with guard rails: it reasons with a language model, but every action goes through validated, audited tools."
        />

        {/* Pipeline */}
        <Reveal>
          <div className="glass-card relative mx-auto mb-12 max-w-5xl p-6 md:p-10">
            <div className="relative grid gap-6 md:grid-cols-4 md:gap-4">
              {/* Connector with a travelling pulse (desktop) */}
              <div aria-hidden className="absolute left-[12.5%] right-[12.5%] top-7 hidden h-px bg-gradient-to-r from-primary/10 via-primary/40 to-primary/10 md:block">
                {!reduceMotion && (
                  <motion.span
                    className="absolute -top-[3px] h-[7px] w-16 rounded-full bg-gradient-to-r from-transparent via-primary to-transparent shadow-glow"
                    animate={{ left: ["-5%", "100%"] }}
                    transition={{ duration: 2.6, repeat: Infinity, ease: "easeInOut" }}
                  />
                )}
              </div>
              {PIPELINE.map((node, index) => (
                <motion.div
                  key={node.label}
                  initial={{ opacity: 0, y: 16 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  viewport={{ once: true }}
                  transition={{ delay: index * 0.15, duration: 0.6 }}
                  className="relative flex flex-col items-center text-center"
                >
                  <div className="relative z-10 flex h-14 w-14 items-center justify-center rounded-2xl border border-primary/30 bg-card shadow-glow">
                    <node.icon className="h-6 w-6 text-primary" />
                  </div>
                  <p className="mt-3 font-display font-semibold">{node.label}</p>
                  <p className="text-xs text-muted-foreground">{node.detail}</p>
                </motion.div>
              ))}
            </div>
          </div>
        </Reveal>

        <Stagger className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
          {GUARANTEES.map((item) => (
            <StaggerItem key={item.title}>
              <SpotlightCard className="h-full p-5">
                <item.icon className="mb-3 h-5 w-5 text-primary" />
                <p className="mb-1.5 font-display font-semibold">{item.title}</p>
                <p className="text-sm leading-relaxed text-muted-foreground">{item.body}</p>
              </SpotlightCard>
            </StaggerItem>
          ))}
        </Stagger>
      </div>
    </section>
  );
}
