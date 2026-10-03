"use client";

import { SectionHeading } from "@/components/landing/SectionHeading";
import { EASE_OUT } from "@/components/motion";
import { motion, useReducedMotion, useScroll, useSpring } from "framer-motion";
import { MessageSquareText, Rocket, UserPlus, Wallet } from "lucide-react";
import { useRef } from "react";

const STEPS = [
  { icon: UserPlus, title: "Create your account", body: "Sign up with email or Google and pick your currency. Default categories are ready instantly." },
  { icon: Wallet, title: "Add what you earn and spend", body: "Log transactions in seconds, or just tell the assistant: \"spent 450 on lunch\"." },
  { icon: MessageSquareText, title: "Ask anything", body: "\"Why is this month higher?\", \"Can I afford a trip?\" Answers come from your real data." },
  { icon: Rocket, title: "Let it watch your back", body: "Budgets, goals and a daily check send you alerts before small leaks become big ones." },
];

export function HowItWorks() {
  const listRef = useRef<HTMLOListElement>(null);
  const reduceMotion = useReducedMotion();
  // The connecting line draws itself as the steps scroll through the viewport.
  const { scrollYProgress } = useScroll({ target: listRef, offset: ["start 75%", "end 55%"] });
  const lineScale = useSpring(scrollYProgress, { stiffness: 120, damping: 30 });

  return (
    <section id="how" className="relative scroll-mt-24 py-24 md:py-32">
      <div className="container max-w-4xl">
        <SectionHeading eyebrow="How it works" icon={Rocket} title={<>Up and running in <span className="gradient-text">two minutes</span></>} />

        <div className="relative">
          <div aria-hidden className="absolute bottom-6 left-6 top-6 w-px bg-border md:left-1/2" />
          <motion.div
            aria-hidden
            className="absolute bottom-6 left-6 top-6 w-px origin-top bg-gradient-to-b from-brand to-brand-2 shadow-glow md:left-1/2"
            style={reduceMotion ? undefined : { scaleY: lineScale }}
          />
        <ol ref={listRef} className="relative space-y-10 md:space-y-14">
          {STEPS.map((step, index) => {
            const right = index % 2 === 1;
            return (
              <motion.li
                key={step.title}
                initial={{ opacity: 0, x: right ? 40 : -40 }}
                whileInView={{ opacity: 1, x: 0 }}
                viewport={{ once: true, margin: "-15% 0px" }}
                transition={{ duration: 0.7, ease: EASE_OUT }}
                className="relative grid grid-cols-[3rem_1fr] items-start gap-5 md:grid-cols-2 md:gap-16"
              >
                <div className="relative z-10 flex h-12 w-12 items-center justify-center rounded-2xl border border-primary/30 bg-card shadow-glow md:absolute md:left-1/2 md:-translate-x-1/2">
                  <step.icon className="h-5 w-5 text-primary" />
                </div>
                <div className={`glass-card p-5 md:p-6 ${right ? "md:col-start-2 md:ml-8" : "md:col-start-1 md:mr-8 md:text-right"}`}>
                  <p className="mb-1 text-xs font-semibold uppercase tracking-widest text-primary">Step {index + 1}</p>
                  <h3 className="mb-2 font-display text-xl font-semibold">{step.title}</h3>
                  <p className="text-sm leading-relaxed text-muted-foreground">{step.body}</p>
                </div>
              </motion.li>
            );
          })}
        </ol>
        </div>
      </div>
    </section>
  );
}
