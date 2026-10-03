"use client";

import { DashboardMockup } from "@/components/landing/DashboardMockup";
import { Aurora, EASE_OUT, TiltCard } from "@/components/motion";
import { useAuth } from "@/components/providers/AuthProvider";
import { Button } from "@/components/ui/button";
import { AnimatePresence, motion, useReducedMotion, useScroll, useTransform } from "framer-motion";
import { ArrowRight, BellRing, CheckCircle2, ShieldCheck, Sparkles } from "lucide-react";
import Link from "next/link";
import { useEffect, useRef, useState } from "react";

const ROTATING_WORDS = ["explained.", "on budget.", "on autopilot.", "growing."];

function RotatingWord() {
  const [index, setIndex] = useState(0);
  useEffect(() => {
    const timer = setInterval(() => setIndex((i) => (i + 1) % ROTATING_WORDS.length), 2600);
    return () => clearInterval(timer);
  }, []);
  return (
    <span className="relative inline-grid">
      {/* Invisible longest word reserves the width, so the line never jumps. */}
      <span className="invisible col-start-1 row-start-1">on autopilot.</span>
      <AnimatePresence mode="popLayout" initial={false}>
        <motion.span
          key={ROTATING_WORDS[index]}
          className="gradient-text col-start-1 row-start-1"
          initial={{ y: "70%", opacity: 0, filter: "blur(8px)" }}
          animate={{ y: "0%", opacity: 1, filter: "blur(0px)" }}
          exit={{ y: "-70%", opacity: 0, filter: "blur(8px)" }}
          transition={{ duration: 0.55, ease: EASE_OUT }}
        >
          {ROTATING_WORDS[index]}
        </motion.span>
      </AnimatePresence>
    </span>
  );
}

const fadeUp = (delay: number) => ({
  initial: { opacity: 0, y: 24, filter: "blur(8px)" },
  animate: { opacity: 1, y: 0, filter: "blur(0px)" },
  transition: { duration: 0.8, delay, ease: EASE_OUT },
});

export function HeroSection() {
  const { user } = useAuth();
  const reduceMotion = useReducedMotion();
  const sectionRef = useRef<HTMLElement>(null);

  // Scroll-driven: the copy drifts up and fades while the product preview tilts flat and comes forward.
  const { scrollYProgress } = useScroll({ target: sectionRef, offset: ["start start", "end start"] });
  const copyY = useTransform(scrollYProgress, [0, 0.5], [0, -90]);
  const copyOpacity = useTransform(scrollYProgress, [0, 0.4], [1, 0]);
  const previewRotate = useTransform(scrollYProgress, [0, 0.35], [22, 0]);
  const previewScale = useTransform(scrollYProgress, [0, 0.35], [0.92, 1.02]);
  const previewY = useTransform(scrollYProgress, [0, 0.35], [0, -40]);

  return (
    <section ref={sectionRef} className="relative overflow-hidden pb-24 pt-16 md:pb-36 md:pt-24">
      <Aurora intensity="strong" />

      <motion.div
        className="container relative flex flex-col items-center text-center"
        style={reduceMotion ? undefined : { y: copyY, opacity: copyOpacity }}
      >
        <motion.div {...fadeUp(0)}>
          <Link
            href="#ai-demo"
            className="group inline-flex items-center gap-2 rounded-full border border-primary/25 bg-primary/10 py-1 pl-1 pr-3 text-sm text-primary backdrop-blur transition-colors hover:bg-primary/15"
          >
            <span className="rounded-full bg-brand-gradient px-2 py-0.5 text-xs font-semibold text-brand-foreground">New</span>
            An AI agent that works on your real numbers
            <ArrowRight className="h-3.5 w-3.5 transition-transform group-hover:translate-x-0.5" />
          </Link>
        </motion.div>

        <motion.h1
          {...fadeUp(0.1)}
          className="mt-8 max-w-5xl text-balance font-display text-5xl font-bold leading-[1.05] tracking-tight sm:text-6xl md:text-7xl lg:text-8xl"
        >
          Your money, <RotatingWord />
        </motion.h1>

        <motion.p {...fadeUp(0.2)} className="mt-6 max-w-2xl text-pretty text-lg text-muted-foreground md:text-xl">
          Bud-Wiser tracks your spending, keeps budgets honest and answers questions about your finances in plain
          English. It even logs expenses for you, but only after you confirm.
        </motion.p>

        <motion.div {...fadeUp(0.3)} className="mt-10 flex flex-col gap-3 sm:flex-row">
          <Button size="lg" variant="gradient" className="gap-2 px-8" asChild>
            <Link href={user ? "/dashboard" : "/sign-up"}>
              {user ? "Open dashboard" : "Start free"}
              <ArrowRight className="h-4 w-4" />
            </Link>
          </Button>
          <Button size="lg" variant="outline" className="gap-2 px-8" asChild>
            <Link href="#ai-demo">
              <Sparkles className="h-4 w-4 text-primary" /> See the AI in action
            </Link>
          </Button>
        </motion.div>

        <motion.ul {...fadeUp(0.4)} className="mt-8 flex flex-wrap justify-center gap-x-6 gap-y-2 text-sm text-muted-foreground">
          {["Free for personal use", "No bank linking required", "Nothing changes without your OK"].map((item) => (
            <li key={item} className="flex items-center gap-1.5">
              <CheckCircle2 className="h-4 w-4 text-primary" /> {item}
            </li>
          ))}
        </motion.ul>
      </motion.div>

      {/* Product preview */}
      <motion.div
        initial={{ opacity: 0, y: 60 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 1, delay: 0.45, ease: EASE_OUT }}
        className="container relative mt-16 max-w-6xl md:mt-20"
        style={{ perspective: 1600 }}
      >
        <motion.div
          style={reduceMotion ? undefined : { rotateX: previewRotate, scale: previewScale, y: previewY, transformOrigin: "50% 0%" }}
          className="relative"
        >
          <div aria-hidden className="absolute -inset-x-10 -top-10 bottom-0 -z-10 rounded-[3rem] bg-brand-gradient opacity-20 blur-3xl" />
          <TiltCard maxTilt={4} className="rounded-2xl">
            <DashboardMockup />
          </TiltCard>

          {/* Floating chips */}
          <FloatingChip className="-left-4 top-1/4 hidden md:flex" delay={1.1} icon={BellRing} tone="text-warning">
            Food budget at 85%
          </FloatingChip>
          <FloatingChip className="-right-6 top-[55%] hidden md:flex" delay={1.35} icon={ShieldCheck} tone="text-primary">
            ₹450 lunch logged. Confirmed.
          </FloatingChip>
        </motion.div>
      </motion.div>
    </section>
  );
}

function FloatingChip({
  className,
  delay,
  icon: Icon,
  tone,
  children,
}: {
  className: string;
  delay: number;
  icon: typeof BellRing;
  tone: string;
  children: React.ReactNode;
}) {
  return (
    <motion.div
      className={`absolute z-10 items-center gap-2 rounded-xl border border-white/10 bg-card/80 px-3.5 py-2.5 text-sm shadow-glass backdrop-blur-xl ${className}`}
      initial={{ opacity: 0, scale: 0.8 }}
      animate={{ opacity: 1, scale: 1, y: [0, -8, 0] }}
      transition={{
        opacity: { delay, duration: 0.5 },
        scale: { delay, duration: 0.5, ease: EASE_OUT },
        y: { delay: delay + 0.5, duration: 4, repeat: Infinity, ease: "easeInOut" },
      }}
    >
      <Icon className={`h-4 w-4 ${tone}`} />
      {children}
    </motion.div>
  );
}
