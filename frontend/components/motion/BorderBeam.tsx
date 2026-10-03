"use client";

import { cn } from "@/lib/utils";
import { motion, useReducedMotion } from "framer-motion";
import type { CSSProperties } from "react";

interface Props {
  /** Length of the light segment in px. */
  size?: number;
  /** Seconds per lap. */
  duration?: number;
  /** Seconds to offset the start (use half the duration for a second beam chasing the first). */
  delay?: number;
  colorFrom?: string;
  colorTo?: string;
  borderWidth?: number;
  /** Travel counter-clockwise. */
  reverse?: boolean;
  className?: string;
}

/**
 * A short gradient light that travels around the parent's rounded border. Place inside a `relative` element with
 * a border radius; it inherits the radius. The element follows the border via CSS `offset-path: rect(... round)`
 * and is masked so only the border strip is visible, never the content. Hidden for reduced-motion users.
 */
export function BorderBeam({
  size = 120,
  duration = 8,
  delay = 0,
  colorFrom = "hsl(var(--brand))",
  colorTo = "hsl(var(--brand-2))",
  borderWidth = 1,
  reverse = false,
  className,
}: Props) {
  const reduceMotion = useReducedMotion();
  if (reduceMotion) return null;

  // Two mask layers: the padding box and the border box; intersecting them leaves only the border ring visible.
  const maskStyle: CSSProperties = {
    borderWidth,
    maskImage: "linear-gradient(transparent, transparent), linear-gradient(#000, #000)",
    WebkitMaskImage: "linear-gradient(transparent, transparent), linear-gradient(#000, #000)",
    maskClip: "padding-box, border-box",
    WebkitMaskClip: "padding-box, border-box",
    maskComposite: "intersect",
    WebkitMaskComposite: "source-in",
  };

  return (
    <div aria-hidden className="pointer-events-none absolute inset-0 rounded-[inherit] border border-transparent" style={maskStyle}>
      <motion.div
        className={cn("absolute aspect-square", className)}
        style={{
          width: size,
          offsetPath: `rect(0 auto auto 0 round ${size}px)`,
          background: `linear-gradient(to left, ${colorFrom}, ${colorTo}, transparent)`,
        }}
        initial={{ offsetDistance: reverse ? "100%" : "0%" }}
        animate={{ offsetDistance: reverse ? ["100%", "0%"] : ["0%", "100%"] }}
        transition={{ repeat: Infinity, ease: "linear", duration, delay }}
      />
    </div>
  );
}
