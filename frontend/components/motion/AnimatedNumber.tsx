"use client";

import { animate, useInView, useMotionValue, useReducedMotion } from "framer-motion";
import { useEffect, useRef, useState } from "react";

interface Props {
  value: number;
  /** Formats the in-between values too (e.g. a currency formatter). */
  format?: (value: number) => string;
  duration?: number;
  className?: string;
}

const defaultFormat = (value: number) => Math.round(value).toLocaleString();

/**
 * Counts up/down to `value` when visible, and animates between values when it changes (e.g. after a refetch).
 * Renders the final value immediately for users who prefer reduced motion.
 */
export function AnimatedNumber({ value, format = defaultFormat, duration = 1.1, className }: Props) {
  const ref = useRef<HTMLSpanElement>(null);
  const inView = useInView(ref, { once: true, margin: "-5% 0px" });
  const reduceMotion = useReducedMotion();
  const motionValue = useMotionValue(0);
  // A ref, so an inline `format` prop (new function every render) never restarts the animation.
  const formatRef = useRef(format);
  formatRef.current = format;
  const [display, setDisplay] = useState(() => format(reduceMotion ? value : 0));

  useEffect(() => motionValue.on("change", (latest) => setDisplay(formatRef.current(latest))), [motionValue]);

  useEffect(() => {
    if (reduceMotion) {
      motionValue.set(value);
      setDisplay(formatRef.current(value));
      return;
    }
    if (!inView) return;
    const controls = animate(motionValue, value, { duration, ease: [0.22, 1, 0.36, 1] });
    return () => controls.stop();
  }, [duration, inView, motionValue, reduceMotion, value]);

  return (
    <span ref={ref} className={className} style={{ fontVariantNumeric: "tabular-nums" }}>
      {display}
    </span>
  );
}
