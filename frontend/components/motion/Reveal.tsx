"use client";

import { cn } from "@/lib/utils";
import { motion, type HTMLMotionProps, type Variants } from "framer-motion";

/** Shared easing: a soft "expo out" that feels quick but settles gently. */
export const EASE_OUT = [0.22, 1, 0.36, 1] as const;

type Direction = "up" | "down" | "left" | "right" | "none";

const OFFSET = 24;

function hiddenFor(direction: Direction, blur: boolean) {
  const base = { opacity: 0, filter: blur ? "blur(8px)" : "blur(0px)" };
  switch (direction) {
    case "up":
      return { ...base, y: OFFSET };
    case "down":
      return { ...base, y: -OFFSET };
    case "left":
      return { ...base, x: OFFSET };
    case "right":
      return { ...base, x: -OFFSET };
    default:
      return base;
  }
}

interface RevealProps extends HTMLMotionProps<"div"> {
  direction?: Direction;
  delay?: number;
  duration?: number;
  blur?: boolean;
  /** Animate once when scrolled into view (default) or immediately on mount. */
  onMount?: boolean;
}

/** Fades and slides its content in when it scrolls into view. */
export function Reveal({
  direction = "up",
  delay = 0,
  duration = 0.7,
  blur = false,
  onMount = false,
  className,
  children,
  ...props
}: RevealProps) {
  const hidden = hiddenFor(direction, blur);
  const visible = { opacity: 1, x: 0, y: 0, filter: "blur(0px)", transition: { duration, delay, ease: EASE_OUT } };
  return (
    <motion.div
      initial={hidden}
      {...(onMount ? { animate: visible } : { whileInView: visible, viewport: { once: true, margin: "-10% 0px" } })}
      className={className}
      {...props}
    >
      {children}
    </motion.div>
  );
}

const staggerContainer = (stagger: number, delay: number): Variants => ({
  hidden: {},
  visible: { transition: { staggerChildren: stagger, delayChildren: delay } },
});

export const staggerItem: Variants = {
  hidden: { opacity: 0, y: 18, filter: "blur(6px)" },
  visible: { opacity: 1, y: 0, filter: "blur(0px)", transition: { duration: 0.6, ease: EASE_OUT } },
};

interface StaggerProps extends HTMLMotionProps<"div"> {
  stagger?: number;
  delay?: number;
  onMount?: boolean;
}

/** Children wrapped in <StaggerItem> animate in one after another. */
export function Stagger({ stagger = 0.08, delay = 0, onMount = false, className, children, ...props }: StaggerProps) {
  return (
    <motion.div
      variants={staggerContainer(stagger, delay)}
      initial="hidden"
      {...(onMount ? { animate: "visible" } : { whileInView: "visible", viewport: { once: true, margin: "-10% 0px" } })}
      className={className}
      {...props}
    >
      {children}
    </motion.div>
  );
}

export function StaggerItem({ className, ...props }: HTMLMotionProps<"div">) {
  return <motion.div variants={staggerItem} className={cn(className)} {...props} />;
}
